package webView.actionplan

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import onboard.page.OtpPage
import org.junit.jupiter.api.*
import utils.json.json
import model.UsersResponse
import com.microsoft.playwright.options.RequestOptions
import utils.logger.logger
import utils.report.StepHelper
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ActionPlanTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
        context = browser.newContext()
        page = context.newPage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    @Test
    @Order(1)
    fun `generate and verify action plan`() {
        val name = "Gowthaman"
        logger.info { "Starting ActionPlan flow..." }
        StepHelper.step("Starting ActionPlan flow")

        // 1. Login
        logger.info { "Logging in with mobile: ${TestConfig.TestUsers.EXISTING_USER.mobileNumber}" }
        StepHelper.step("Logging in")
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(TestConfig.TestUsers.EXISTING_USER)
        
        val otpPage = OtpPage(page)
        
        // Wait for the verify-otp response to be processed and tokens stored
        page.waitForResponse({ response -> 
            response.url().contains(TestConfig.APIs.API_VERIFY_OTP) && response.status() == 200 
        }) {
            otpPage.enterOtp(TestConfig.TestUsers.EXISTING_USER.otp, TestConfig.TestUsers.EXISTING_USER.mobileNumber, TestConfig.TestUsers.EXISTING_USER.countryCode)
            page.keyboard().press("Enter") // Trigger submission if no button is present
        }
        
        // Brief wait to ensure TestConfig is updated by the response listener
        page.waitForTimeout(1000.0)
        
        logger.info { "Tokens captured. ACCESS_TOKEN length: ${TestConfig.ACCESS_TOKEN.length}, USER_ID: ${TestConfig.USER_ID}, USER_NAME: ${TestConfig.USER_NAME}" }
        assert(TestConfig.ACCESS_TOKEN.isNotEmpty()) { "Access token was not captured after login" }

        // 1b. Fetch all users to find Gowthaman's ID
        StepHelper.step("Fetching users list to find target user ID")
        val usersResponse = page.context().request().get(
            TestConfig.APIs.API_USERS,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", "Asia/Kolkata")
        )
        
        if (usersResponse.status() != 200) {
            logger.error { "Failed to fetch users list. Status: ${usersResponse.status()}, Body: ${usersResponse.text()}" }
        }
        assert(usersResponse.status() == 200) { "Failed to fetch users list: ${usersResponse.status()}" }
        
        val usersList = json.decodeFromString<UsersResponse>(usersResponse.text())
        val targetUser = usersList.data.users.find { it.name.contains(name, ignoreCase = true) }
            ?: throw AssertionError("User '$name' not found in users list")
        
        val targetUserId = targetUser.id ?: throw AssertionError("User '$name' does not have an ID")
        logger.info { "Found target user: ${targetUser.name} with ID: $targetUserId" }

        // Wait for the home page to load after login
//        page.waitForURL("${TestConfig.Urls.BASE_URL}")

        // 2. Navigation steps provided by user
        StepHelper.step("Navigating to Health Data and through dashboard steps")
        
        page.navigate(TestConfig.Urls.HEALTH_DATA_URL)
        page.waitForLoadState()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Switch to Admin")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("User Management")).click()
        
        val searchBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search for user..."))
        searchBox.click()
        searchBox.press("ArrowDown")
        searchBox.fill(name)
        
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(name)).click()
//        page.getByText("Viewing data for: Gowthaman").click()
        
        // The user's nth(5) locator
        page.locator("div").filter(Locator.FilterOptions().setHasText("Administrator Account-")).nth(5).click()


        //app.stg.deepholistics.com/recommendations
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Action Plan")).click()

        // 3. Click "Go to PDF tool" and capture popup/redirect
        StepHelper.step("Clicking 'Go to PDF tool' and verifying final URL")


        
        val page1 = context.waitForPage {
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Go to PDF tool")).click()
        }

        page1.waitForLoadState()
        val finalUrl = page1.url()
        logger.info { "Final URL: $finalUrl" }

        val expectedBase = "https://dh-stg-action-plan-generator.replit.app/"
        logger.info { "Verifying final URL components..." }
        assert(finalUrl.contains(expectedBase)) { "Final URL does not contain expected base: $expectedBase. Actual: $finalUrl" }
        assert(finalUrl.contains("user_id=$targetUserId")) { "Final URL missing correct user_id. Expected: $targetUserId, Actual: $finalUrl" }
        assert(finalUrl.contains("user_name=${targetUser.name}")) { "Final URL missing correct user_name. Expected: ${targetUser.name}, Actual: $finalUrl" }
        assert(finalUrl.contains("access_token=${TestConfig.ACCESS_TOKEN}")) { "Final URL missing correct access_token. Actual: $finalUrl" }
        
        // 4. Call user-data API on the replit app
        StepHelper.step("Calling user-data API on replit app and verifying response")
        
        val requestBody = buildJsonObject {
            put("userId", targetUserId)
            put("accessToken", TestConfig.ACCESS_TOKEN)
        }.toString()

        val userDataResponse = page1.context().request().post(
            TestConfig.APIs.API_ACTION_PLAN_USER_DATA,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(requestBody)
        )
        
        logger.info { "User Data API Response Status: ${userDataResponse.status()}" }
        assert(userDataResponse.status() == 200) { "User data API failed: ${userDataResponse.status()}. Body: ${userDataResponse.text()}" }
        
        val userData = userDataResponse.text()
        assert(userData.contains("\"success\":true")) { "User data API response unsuccessful: $userData" }
        logger.info { "User data API successfully verified." }

        logger.info { "ActionPlan flow completed and verified successfully." }
    }
}
