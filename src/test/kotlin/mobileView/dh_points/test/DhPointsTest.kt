package mobileView.dh_points.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing.StartOptions
import com.microsoft.playwright.Tracing.StopOptions
import config.BaseTest
import config.TestConfig
import mobileView.home.checkBloodTestBookedCardStatus
import onboard.page.LoginPage
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import mobileView.home.HomePage
import org.junit.jupiter.api.*
import utils.logger.logger
import java.nio.file.Paths
import model.profile.QuestionerMealType
import com.microsoft.playwright.options.RequestOptions
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import utils.json.json

@Epic("DH Points")
@Feature("DH Points E2E Flow")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DhPointsTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
    }

    @AfterAll
    fun tearDown() {
        browser.close()
        playwright.close()
    }

    @BeforeEach
    fun createContext() {
        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)
            .setRecordVideoDir(Paths.get(TestConfig.Artifacts.VIDEO_DIR))
            .setRecordVideoSize(390, 844)

        context = browser.newContext(contextOptions)
        context.setDefaultTimeout(TestConfig.Browser.TIMEOUT * 2)
        page = context.newPage()

        context.tracing().start(
            StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
        )
    }

    @AfterEach
    fun closeContext() {
        val path = "build/traceView/trace_${System.currentTimeMillis()}.zip"
        context.tracing().stop(
            StopOptions()
                .setPath(Paths.get(path))
        )
        context.close()
    }

    @Test
    fun `dh points full flow test`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        // Generate a dynamic mobile number to ensure a new user is created
        val dynamicMobileNumber = "9" + (System.currentTimeMillis() % 1000000000).toString().padEnd(9, '0')
        val testUser = TestConfig.TestUsers.NEW_USER.copy(mobileNumber = dynamicMobileNumber)
        logger.info("Dynamic Mobile Number: $dynamicMobileNumber")
        // Step 1: Sign up new user flow
        val homePage = loginPage
            .clickSignUp()
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
            .selectSlotsAndContinue()
            .enterCouponCode(TestConfig.Coupons.VALID_COUPON)
            .clickApplyCoupon()
            .checkTotalAmount()
            .clickCheckout()



        checkBloodTestBookedCardStatus(homePage)

//        homePage.takeScreenshot("signup-order-placed-dh-points")

        // Step 2: Login using the credentials
        // Clear cookies to simulate logging out
        context.clearCookies()

        // Navigate again to the Login page with a fresh context state
        val loginPage2 = LoginPage(page).navigate() as LoginPage
        
        // Log in using the testUser credentials
        val loggedInHomePage = loginPage2
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)

        triggerDataPipeline(TestConfig.USER_ID)
        // Continue DH points assertions ...
//        loggedInHomePage.takeScreenshot("login-successful-dh-points")
    }

    @Test
    fun `dh points verification until Questionnaire`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.EXISTING_USER
        loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)
            .claimYourConsultCard()
            .consultationConfirmationCard()
            .question_1_veg(type = QuestionerMealType.VEGAN)
             page.waitForTimeout(3000.0)

        val homePage = HomePage(page).navigate() as HomePage
        homePage.claimYourConsultCard()
        .consultationWithExpertCard()
    }

    @Test
    fun `dh points verification`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val homePage = loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)
        
        triggerDataPipeline(TestConfig.USER_ID)
        homePage.rewardPointsValidation()
    }

    private fun triggerDataPipeline(userid: String) {
        logger.info { "Triggering Data Pipeline..." }

        // Step 1: Get Access Token
        val tokenResponse = page.context().request().post(
            "https://human-token.auth.ap-south-1.amazoncognito.com/oauth2/token",
            RequestOptions.create()
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic M3R1YmN0NW5rYTBma2FiaGU0MWZpMWpnbGQ6MWJqNDZoazJlYTJkMXZ2bmkxYWdnazEwcTkybmY2ZHV1OTA0MGNwZDBycDhxMWZoM2FzNA==")
                .setData("grant_type=client_credentials&scope=auth/dh-read-dev")
        )

        if (tokenResponse.status() != 200) {
            logger.error { "Failed to get access token for pipeline: ${tokenResponse.status()} - ${tokenResponse.text()}" }
            return
        }

        val tokenJson = json.decodeFromString<JsonObject>(tokenResponse.text())
        val accessToken = tokenJson["access_token"]?.jsonPrimitive?.content ?: ""

        logger.info { "Access Token obtained for Pipeline." }

        // Step 2: Call Pipeline API
        val pipelineResponse = page.context().request().fetch(
            "https://465ifncp63.execute-api.ap-south-1.amazonaws.com/stg/user_${userid}/execute-data-pipeline?source_prd_user_id=145",
            RequestOptions.create()
                .setMethod("GET")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", accessToken)
                .setData("{}")
        )

        logger.info { "Pipeline Trigger Response Status: ${pipelineResponse.status()}" }
        logger.info { "Pipeline Trigger Response Body: ${pipelineResponse.text()}" }
    }
}
