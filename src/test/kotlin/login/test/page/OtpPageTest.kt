package login.test.page

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OtpPageTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var page: Page

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

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }


    @Test
    fun `should display OTP confirm screen elements`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue()

        assert(otpPage.isOnConfirmScreen()) { "Confirm screen should be visible" }
        assert(otpPage.isEditButtonVisible()) { "Edit button should be visible" }
        assert(otpPage.isOtpInputVisible()) { "OTP input should be visible" }
        assert(otpPage.isWhatsAppCheckboxVisible()) { "WhatsApp checkbox should be visible on confirm screen" }
        assert(!otpPage.isContinueButtonEnabled()) { "Continue button should be disabled without OTP" }

        otpPage.takeScreenshot("otp-confirm-screen")
    }

    @Test
    fun `should display resend timer`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue()

        assert(otpPage.isResendTimerVisible()) { "Resend timer should be visible" }
        val timerText = otpPage.getResendTimerText()
        assert(timerText != null) { "Timer text should not be null" }
        assert(timerText!!.contains("Resend code in")) { "Timer should contain 'Resend code in'" }

        otpPage.takeScreenshot("resend-timer-visible")
    }

    @Test
    fun `should display timer in correct format`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue()

        val timerText = otpPage.getResendTimerText()
        assert(timerText != null) { "Timer text should not be null" }

        val timerPattern = Regex(".*Resend code in \\d+:\\d{2}.*")
        assert(timerPattern.matches(timerText!!)) { "Timer should match format 'Resend code in X:XX'" }

        otpPage.takeScreenshot("timer-format-verification")
    }

    @Test
    fun `should show decreasing timer`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue()

        val initialTimerText = otpPage.getResendTimerText()
        assert(initialTimerText != null) { "Initial timer text should not be null" }

        val initialSecondsMatch = Regex("(\\d+):(\\d+)").find(initialTimerText!!)
        assert(initialSecondsMatch != null) { "Timer should match time format" }

        val initialMinutes = initialSecondsMatch!!.groupValues[1].toInt()
        val initialSeconds = initialSecondsMatch.groupValues[2].toInt()
        val initialTotalSeconds = initialMinutes * 60 + initialSeconds

        otpPage.takeScreenshot("timer-initial-$initialTotalSeconds")

        Thread.sleep(3000)

        val laterTimerText = otpPage.getResendTimerText()
        assert(laterTimerText != null) { "Later timer text should not be null" }

        val laterSecondsMatch = Regex("(\\d+):(\\d+)").find(laterTimerText!!)
        val laterMinutes = laterSecondsMatch!!.groupValues[1].toInt()
        val laterSeconds = laterSecondsMatch.groupValues[2].toInt()
        val laterTotalSeconds = laterMinutes * 60 + laterSeconds

        assert(laterTotalSeconds <= initialTotalSeconds - 2) { "Timer should have decreased by at least 2 seconds" }

        otpPage.takeScreenshot("timer-after-3-seconds-$laterTotalSeconds")
    }


    @Test
    fun `should return to login page when Edit is clicked`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue()

        val returnedLoginPage = otpPage.clickEdit()

        assert(returnedLoginPage.isMobileInputVisible()) { "Mobile input should be visible after edit" }
        returnedLoginPage.takeScreenshot("returned-to-login")
    }

    @Test
    fun `should require re-entering number after Edit`() {
        val testUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue(testUser)

        val returnedLoginPage = otpPage.clickEdit()

        returnedLoginPage.clearMobileNumber()
        assert(!returnedLoginPage.isContinueButtonEnabled()) { "Continue should be disabled after clearing" }

        returnedLoginPage.enterMobileNumber("8888888888")
        assert(returnedLoginPage.isContinueButtonEnabled()) { "Continue should be enabled after re-entering" }

        returnedLoginPage.takeScreenshot("re-enter-number-after-edit")
    }


    @Test
    fun `should navigate to basic details after valid OTP`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        val basicDetailsPage = loginPage
            .enterMobileAndContinue()
            .enterOtpAndContinueToAccountCreation()

        assert(basicDetailsPage.isFirstNameVisible()) { "First name should be visible" }
        basicDetailsPage.takeScreenshot("navigated-to-basic-details")
    }

    @Test
    fun `should show error message for incorrect OTP`() {
        val testUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue(testUser)

        otpPage.enterOtp("123456")
        otpPage.clickContinue()

        assert(otpPage.isIncorrectOtpMessageVisible()) { "Error message 'Incorrect OTP' should be visible" }
        otpPage.takeScreenshot("incorrect-otp-error")
    }
}
