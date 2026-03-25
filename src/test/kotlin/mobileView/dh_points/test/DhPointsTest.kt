package mobileView.dh_points.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing.StartOptions
import com.microsoft.playwright.Tracing.StopOptions
import com.microsoft.playwright.options.RequestOptions
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Story
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import mobileView.home.HomePage
import model.profile.QuestionerMealType
import onboard.page.LoginPage
import org.junit.jupiter.api.*
import utils.DhPointsStore
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.DH_POINTS_APPLY_COUPON
import utils.report.StepHelper.DH_POINTS_CAPTURE_TOTAL
import utils.report.StepHelper.DH_POINTS_CHECKOUT
import utils.report.StepHelper.DH_POINTS_CONSULT_WITH_EXPERT
import utils.report.StepHelper.DH_POINTS_ENTER_COUPON
import utils.report.StepHelper.DH_POINTS_GENERATE_USER
import utils.report.StepHelper.DH_POINTS_LOGIN
import utils.report.StepHelper.DH_POINTS_LOGOUT
import utils.report.StepHelper.DH_POINTS_PAYMENT
import utils.report.StepHelper.DH_POINTS_PIPELINE_CALL
import utils.report.StepHelper.DH_POINTS_SIGNUP
import utils.report.StepHelper.DH_POINTS_TOKEN_FETCH
import utils.report.StepHelper.DH_POINTS_TRIGGER_PIPELINE
import utils.report.StepHelper.DH_POINTS_VERIFY_BLOOD_TEST_CARD
import utils.report.StepHelper.DH_POINTS_VERIFY_LOGIN
import java.nio.file.Paths
import kotlin.test.assertTrue

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

    // ─────────────────────────────────────────────────────────────
    // TEST 1: Full Flow – Signup → Checkout → Login → Pipeline
    // ─────────────────────────────────────────────────────────────
    @Test
    @Story("DH Points – Full Signup and Pipeline Flow")
    fun `dh points full flow test`() {

        // Step 1: Generate dynamic test user
        logger.info { "═══════════════════════════════════════" }
        logger.info { "  TEST: dh points full flow test" }
        logger.info { "═══════════════════════════════════════" }
        StepHelper.step(DH_POINTS_GENERATE_USER)
        val dynamicMobileNumber = "9" + (System.currentTimeMillis() % 1000000000).toString().padEnd(9, '0')
        val testUser = TestConfig.TestUsers.NEW_USER.copy(mobileNumber = dynamicMobileNumber)
        logger.info { "[STEP] Dynamic Mobile Number: $dynamicMobileNumber" }
       // TestConfig.TestUsers.EXISTING_USER = testUser

        // Step 2: Sign up new user
        StepHelper.step(DH_POINTS_SIGNUP)
        logger.info { "[STEP] Starting sign-up flow for: $dynamicMobileNumber" }
        val loginPage = LoginPage(page).navigate() as LoginPage
        val orderSummaryPage = loginPage
            .clickSignUp()
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
            .selectSlotsAndContinue()
        // Step 3: Enter and apply coupon
        StepHelper.step("$DH_POINTS_ENTER_COUPON: ${TestConfig.Coupons.VALID_COUPON}")
        logger.info { "[STEP] Entering coupon: ${TestConfig.Coupons.VALID_COUPON}" }
        orderSummaryPage.enterCouponCode(TestConfig.Coupons.VALID_COUPON)

        StepHelper.step(DH_POINTS_APPLY_COUPON)
        logger.info { "[STEP] Applying coupon and waiting for discount to reflect..." }
        orderSummaryPage.clickApplyCoupon()

        // Step 4: Capture total and discount
        StepHelper.step(DH_POINTS_CAPTURE_TOTAL)
        logger.info { "[STEP] Capturing total amount and discount amount from order summary..." }
        orderSummaryPage.checkTotalAmount()
        logger.info { "[STEP] Captured - Total: ${DhPointsStore.totalAmount}, Discount: ${DhPointsStore.discountAmount}, Coupon: ${DhPointsStore.couponCode}" }

        // Step 5: Checkout
//        StepHelper.step(DH_POINTS_CHECKOUT)
//        logger.info { "[STEP] Proceeding to checkout..." }
        val homePage = orderSummaryPage.clickCheckout()

        // Step 6: Payment
        StepHelper.step(DH_POINTS_PAYMENT)
        logger.info { "[STEP] Proceed to payment" }
        orderSummaryPage.clickGooglePayUPI()
        // Step 6: Verify blood test card
        StepHelper.step(DH_POINTS_VERIFY_BLOOD_TEST_CARD)
        logger.info { "[STEP] Verifying blood test booked card status..." }
        checkBloodTestBookedCardStatus(homePage)

        // Step 7: Logout
        StepHelper.step(DH_POINTS_LOGOUT)
        logger.info { "[STEP] Clearing cookies to simulate logout..." }
        context.clearCookies()

        // Step 8: Login
        StepHelper.step(DH_POINTS_LOGIN)
        logger.info { "[STEP] Logging in with newly created user: $dynamicMobileNumber" }
        val loginPage2 = LoginPage(page).navigate() as LoginPage
        loginPage2
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)
        logger.info { "[STEP] Login successful. User ID: ${TestConfig.USER_ID}" }

        // Step 9: Trigger data pipeline
        StepHelper.step(DH_POINTS_TRIGGER_PIPELINE)
        logger.info { "[STEP] Triggering data pipeline for user: ${TestConfig.USER_ID}" }
        triggerDataPipeline(TestConfig.USER_ID)

        logger.info { "═══════════════════════════════════════" }
        logger.info { "  TEST COMPLETE: dh points full flow test" }
        logger.info { "═══════════════════════════════════════" }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 2: Questionnaire Flow
    // ─────────────────────────────────────────────────────────────
    @Test
    @Story("DH Points – Questionnaire Flow")
    fun `dh points verification until Questionnaire`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.EXISTING_USER
        loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)
            .claimYourConsultCard()
            .consultationConfirmationCard()
            .question_1_veg(type = QuestionerMealType.VEGAN)

        logger.info { "[STEP] Waiting for questionnaire page to settle..." }
        page.waitForTimeout(3000.0)

        // Step 5: Navigate home and proceed to expert consultation
        StepHelper.step(DH_POINTS_CONSULT_WITH_EXPERT)
        logger.info { "[STEP] Navigating to home and proceeding to consult with expert..." }
        val homePage = HomePage(page).navigate() as HomePage
        homePage.claimYourConsultCard()
            .consultationWithExpertCard()

        logger.info { "═══════════════════════════════════════" }
        logger.info { "  TEST COMPLETE: dh points verification until Questionnaire" }
        logger.info { "═══════════════════════════════════════" }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 3: Reward Points Verification
    // ─────────────────────────────────────────────────────────────
    @Test
    @Story("DH Points – Reward Points Verification")
    fun `dh points verification`() {
        logger.info { "═══════════════════════════════════════" }
        logger.info { "  TEST: dh points verification" }
        logger.info { "═══════════════════════════════════════" }
        logger.info { "[STEP] DhPointsStore state at start - Total: ${DhPointsStore.totalAmount}, Discount: ${DhPointsStore.discountAmount}, Coupon: ${DhPointsStore.couponCode}" }

        // Step 1: Login
        StepHelper.step(DH_POINTS_VERIFY_LOGIN)
        val testUser = TestConfig.TestUsers.EXISTING_USER
        logger.info { "[STEP] Logging in with existing user: ${testUser.mobileNumber}" }
        val loginPage = LoginPage(page).navigate() as LoginPage
        val homePage = loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)
            .rewardPointsValidation()
        logger.info { "═══════════════════════════════════════" }
        logger.info { "  TEST COMPLETE: dh points verification" }
        logger.info { "═══════════════════════════════════════" }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER: Trigger Data Pipeline
    // ─────────────────────────────────────────────────────────────
    private fun triggerDataPipeline(userid: String) {
        logger.info { "─────────────────────────────────────────" }
        logger.info { "  triggerDataPipeline(userId=$userid)" }
        logger.info { "─────────────────────────────────────────" }

        // Step A: Get Access Token
        StepHelper.step(DH_POINTS_TOKEN_FETCH)
        logger.info { "[PIPELINE] Requesting access token from Cognito..." }
        val tokenResponse = page.context().request().post(
            "https://human-token.auth.ap-south-1.amazoncognito.com/oauth2/token",
            RequestOptions.create()
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic M3R1YmN0NW5rYTBma2FiaGU0MWZpMWpnbGQ6MWJqNDZoazJlYTJkMXZ2bmkxYWdnazEwcTkybmY2ZHV1OTA0MGNwZDBycDhxMWZoM2FzNA==")
                .setData("grant_type=client_credentials&scope=auth/dh-read-dev")
        )

        if (tokenResponse.status() != 200) {
            logger.error { "[PIPELINE] Failed to get access token: ${tokenResponse.status()} - ${tokenResponse.text()}" }
            return
        }

        val tokenJson = json.decodeFromString<JsonObject>(tokenResponse.text())
        val accessToken = tokenJson["access_token"]?.jsonPrimitive?.content ?: ""
        logger.info { "[PIPELINE] Access token obtained successfully." }

        // Step B: Call Pipeline API
        StepHelper.step("$DH_POINTS_PIPELINE_CALL (userId=$userid)")
        logger.info { "[PIPELINE] Calling data pipeline API for userId=$userid..." }
        val pipelineResponse = page.context().request().fetch(
            "https://465ifncp63.execute-api.ap-south-1.amazonaws.com/stg/user_${userid}/execute-data-pipeline?source_prd_user_id=145",
            RequestOptions.create()
                .setMethod("GET")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", accessToken)
                .setData("{}")
        )

        logger.info { "[PIPELINE] Response Status: ${pipelineResponse.status()}" }
        logger.info { "[PIPELINE] Response Body:   ${pipelineResponse.text()}" }
    }

    private fun checkBloodTestBookedCardStatus(homePage: HomePage) {
        homePage.waitForBloodTestCardToLoad()
        assertTrue(homePage.isPhlebotomistAssignedTitleVisible())
        assertTrue(homePage.isPhlebotomistAssignedDateVisible())

        assertTrue(homePage.isSampleCollectionTitleVisible())
        assertTrue(homePage.isSampleCollectionDateVisible())

        assertTrue(homePage.isLabProcessingTitleVisible())
        assertTrue(homePage.isLabProcessingTimeVisible())

        assertTrue(homePage.isDashBoardReadyToViewTitleVisible())
        assertTrue(homePage.isDashBoardReadyToViewDateVisible())
    }
}
