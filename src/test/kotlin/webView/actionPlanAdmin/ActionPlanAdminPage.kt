package webView.actionPlanAdmin

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import model.UsersResponse
import onboard.page.LoginPage
import onboard.page.OtpPage
import utils.logger.logger
import utils.report.StepHelper
import java.util.regex.Pattern
import utils.Normalize.refactorTimeZone
import utils.json.json as jsonParser

@OptIn(ExperimentalSerializationApi::class)
class ActionPlanAdminPage(page: Page) : BasePage(page) {

    override val pageUrl: String = TestConfig.Urls.HEALTH_DATA_URL

    companion object {
        const val TARGET_USER_NAME = "Rethinavel  natarajan stg"
        const val EXPECTED_BASE_URL = "https://dh-stg-action-plan-generator.replit.app/"
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 1: Login
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Performs the full login flow (login → OTP → wait for token) and returns
     * this page object for chaining.
     */
    fun login(): ActionPlanAdminPage {
        logger.info { "Logging in with mobile: ${TestConfig.TestUsers.EXISTING_USER.mobileNumber}" }
        StepHelper.step(StepHelper.AP_ADMIN_LOGIN)

        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(TestConfig.TestUsers.EXISTING_USER)

        val otpPage = OtpPage(page)

        page.waitForResponse({ response ->
            response.url().contains(TestConfig.APIs.API_VERIFY_OTP) && response.status() == 200
        }) {
            otpPage.enterOtp(
                TestConfig.TestUsers.EXISTING_USER.otp,
                TestConfig.TestUsers.EXISTING_USER.mobileNumber,
                TestConfig.TestUsers.EXISTING_USER.countryCode
            )
            page.keyboard().press("Enter")
        }

        page.waitForTimeout(1000.0)

        logger.info { "Tokens captured. ACCESS_TOKEN length: ${TestConfig.ACCESS_TOKEN.length}, USER_ID: ${TestConfig.USER_ID}" }
        assert(TestConfig.ACCESS_TOKEN.isNotEmpty()) { "Access token was not captured after login" }

        return this
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 2: Fetch target user ID
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Calls the users list API and returns the ID of [TARGET_USER_NAME].
     */
    fun fetchTargetUserId(): String {
        StepHelper.step(StepHelper.AP_ADMIN_FETCH_TARGET_USER)

        val usersResponse = page.context().request().get(
            TestConfig.APIs.API_USERS,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", refactorTimeZone(java.util.TimeZone.getDefault().id))
        )

        if (usersResponse.status() != 200) {
            logger.error { "Failed to fetch users list. Status: ${usersResponse.status()}, Body: ${usersResponse.text()}" }
        }
        assert(usersResponse.status() == 200) { "Failed to fetch users list: ${usersResponse.status()}" }

        val usersList = jsonParser.decodeFromString<UsersResponse>(usersResponse.text())
        val targetUser = usersList.data.users.find { it.name.contains(TARGET_USER_NAME, ignoreCase = true) }
            ?: throw AssertionError("User '$TARGET_USER_NAME' not found in users list")

        val targetUserId = targetUser.id
            ?: throw AssertionError("User '$TARGET_USER_NAME' does not have an ID")

        logger.info { "Found target user: ${targetUser.name} with ID: $targetUserId" }
        return targetUserId
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 3: Navigate to admin, search user, click Action Plan
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Navigates to Health Data, switches to Admin mode, searches for
     * [TARGET_USER_NAME], selects the user, and clicks the Action Plan link.
     */
    fun navigateToActionPlan() {
        StepHelper.step(StepHelper.AP_ADMIN_NAVIGATE)

        page.navigate(TestConfig.Urls.HEALTH_DATA_URL)
        page.waitForLoadState()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Switch to Admin")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("User Management")).click()

        val searchBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search for user..."))
        searchBox.click()
        page.waitForTimeout(5000.0)
        searchBox.pressSequentially(TARGET_USER_NAME, Locator.PressSequentiallyOptions().setDelay(200.0))
        searchBox.press("Enter")

        page.waitForTimeout(2000.0)

        try {
            val userBtn = page.locator("button, a, div[role='button']").filter(
                Locator.FilterOptions().setHasText(Pattern.compile(".*$TARGET_USER_NAME.*", Pattern.CASE_INSENSITIVE))
            ).first()
            userBtn.waitFor(Locator.WaitForOptions().setTimeout(10000.0))
            userBtn.click()
            logger.info { "Successfully selected user $TARGET_USER_NAME" }
        } catch (e: Exception) {
            logger.warn { "Search result click failed for $TARGET_USER_NAME: ${e.message}" }
            page.locator("tr, div[role='row']").nth(1).click()
        }

        val apLink = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Action Plan"))
        apLink.waitFor()
        apLink.click()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 4: Open PDF tool and verify URL
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Clicks "Go to PDF tool", waits for the new page to load, verifies the
     * final URL, and returns the PDF tool [Page] object.
     */
    fun openPdfToolAndVerifyUrl(context: BrowserContext, targetUserId: String): Page {
        StepHelper.step(StepHelper.AP_ADMIN_OPEN_PDF_TOOL)

        val pdfBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Go to PDF tool"))
        pdfBtn.waitFor()

        val pdfPage = context.waitForPage {
            pdfBtn.click()
        }

        pdfPage.waitForLoadState()
        logger.info { "Waiting for Action Plan APIs to settle..." }
        pdfPage.waitForLoadState(LoadState.NETWORKIDLE)
        pdfPage.waitForTimeout(5000.0)

        val finalUrl = pdfPage.url()
        logger.info { "Final URL: $finalUrl" }

        StepHelper.step(StepHelper.AP_ADMIN_VERIFY_URL)
        assert(finalUrl.contains(EXPECTED_BASE_URL)) {
            "Final URL does not contain expected base: $EXPECTED_BASE_URL. Actual: $finalUrl"
        }
        assert(finalUrl.contains("user_id=$targetUserId")) {
            "Final URL missing correct user_id. Expected: $targetUserId, Actual: $finalUrl"
        }
        assert(finalUrl.contains("access_token=${TestConfig.ACCESS_TOKEN}")) {
            "Final URL missing correct access_token. Actual: $finalUrl"
        }

        return pdfPage
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 5: Call user-data API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Posts to the user-data replit API and returns the response body text.
     * Asserts HTTP 200 and `"success":true`.
     */
    fun fetchUserData(pdfPage: Page, targetUserId: String): String {
        StepHelper.step(StepHelper.AP_ADMIN_FETCH_USER_DATA)

        val requestBody = buildJsonObject {
            put("userId", targetUserId)
            put("accessToken", TestConfig.ACCESS_TOKEN)
        }.toString()

        val response = pdfPage.context().request().post(
            TestConfig.APIs.API_ACTION_PLAN_USER_DATA,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(requestBody)
        )

        logger.info { "User Data API Response Status: ${response.status()}" }
        assert(response.status() == 200) {
            "User data API failed: ${response.status()}. Body: ${response.text()}"
        }

        val userData = response.text()
        assert(userData.contains("\"success\":true")) {
            "User data API response unsuccessful: $userData"
        }
        logger.info { "User data API successfully verified." }

        return userData
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 6: Call user-recommendations API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Posts to the user-recommendations replit API and returns the response
     * body text. Asserts HTTP 200 and `"success":true`.
     */
    fun fetchUserRecommendations(pdfPage: Page, targetUserId: String): String {
        StepHelper.step(StepHelper.AP_ADMIN_FETCH_RECOMMENDATIONS)

        val requestBody = buildJsonObject {
            put("userId", targetUserId)
            put("accessToken", TestConfig.ACCESS_TOKEN)
        }.toString()

        val response = pdfPage.context().request().post(
            TestConfig.APIs.API_ACTION_PLAN_USER_RECOMMENDATIONS,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(requestBody)
        )

        logger.info { "User Recommendations API Response Status: ${response.status()}" }
        assert(response.status() == 200) {
            "User recommendations API failed: ${response.status()}. Body: ${response.text()}"
        }

        val recommendationsData = response.text()
        assert(recommendationsData.contains("\"success\":true")) {
            "User recommendations API response unsuccessful: $recommendationsData"
        }
        logger.info { "User recommendations API successfully verified." }

        return recommendationsData
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Combined setup: login → fetch user ID → navigate → open PDF tool → fetch APIs
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Convenience method that runs the full common setup for every test:
     * login, find target user, navigate to Action Plan, open PDF tool,
     * then fetch user-data and recommendations APIs.
     *
     * @return [ActionPlanAdminSetupResult] holding page, user data, and
     *         recommendations data needed by each individual test.
     */
    fun setupTestSession(context: BrowserContext): ActionPlanAdminSetupResult {
        login()
        val targetUserId = fetchTargetUserId()
        navigateToActionPlan()
        val pdfPage = openPdfToolAndVerifyUrl(context, targetUserId)
        val userData = fetchUserData(pdfPage, targetUserId)
        val recommendationsData = fetchUserRecommendations(pdfPage, targetUserId)

        return ActionPlanAdminSetupResult(
            pdfPage = pdfPage,
            targetUserId = targetUserId,
            userData = userData,
            recommendationsData = recommendationsData
        )
    }
}

/**
 * Holds the common results after the shared setup phase so each test
 * can start directly with its own verification logic.
 */
data class ActionPlanAdminSetupResult(
    val pdfPage: Page,
    val targetUserId: String,
    val userData: String,
    val recommendationsData: String
)
