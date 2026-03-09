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
import org.junit.jupiter.api.*
import utils.logger.logger
import java.nio.file.Paths

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

        // Continue DH points assertions ...
//        loggedInHomePage.takeScreenshot("login-successful-dh-points")
    }

    @Test
    fun `dh points verification`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loggedInHomePage = loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToMobileHomePage(testUser)
            .claimYourConsultCard()
            .consultationConfirmationCard()
    }
}
