package login.test.login

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginMobileTest {

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

    // ---------------------- Page Visibility Tests ----------------------

    @Test
    fun `should display all required elements when login page loads`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        assert(loginPage.isLogoVisible()) { "Logo should be visible" }
        assert(loginPage.isDeepHolisticsTextVisible()) { "Deep Holistics text should be visible" }
        assert(loginPage.isLoginHeaderVisible()) { "Login header should be visible" }
        assert(loginPage.isMobileInputVisible()) { "Mobile input should be visible" }
        assert(loginPage.isCountryCodeButtonVisible()) { "Country code button (+91) should be visible" }
        assert(loginPage.isWhatsAppCheckboxVisible()) { "WhatsApp checkbox should be visible" }
        assert(!loginPage.isWhatsAppCheckboxChecked()) { "WhatsApp checkbox should be unchecked by default" }
        assert(loginPage.isContinueButtonVisible()) { "Continue button should be visible" }
        assert(!loginPage.isContinueButtonEnabled()) { "Continue button should be disabled" }
        assert(loginPage.isDontHaveAccountLinkVisible()) { "Don't have an account link should be visible" }
        assert(loginPage.isPrivacyPolicyLinkVisible()) { "Privacy Policy link should be visible" }
        assert(loginPage.isTermsOfServiceLinkVisible()) { "Terms of Service link should be visible" }

        loginPage.takeScreenshot("login-page-loaded-correctly")
    }

    // ------------------------- Mobile Number Validation Tests ----------------------

    @Test
    fun `should keep Continue disabled when mobile number is empty`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        val mobileValue = loginPage.getMobileNumberValue()
        assert(mobileValue.isEmpty()) { "Mobile number field should be empty" }
        assert(!loginPage.isContinueButtonEnabled()) { "Continue should be disabled for empty number" }

        loginPage.takeScreenshot("continue-disabled-empty-number")
    }

    @Test
    fun `should keep Continue disabled for 5-digit number`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("12345")
        assert(!loginPage.isContinueButtonEnabled()) { "Continue should be disabled for 5-digit number" }

        loginPage.takeScreenshot("continue-disabled-5-digits")
    }

    @Test
    fun `should keep Continue disabled for 6-digit number`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("123456")
        assert(!loginPage.isContinueButtonEnabled()) { "Continue should be disabled for 6-digit number" }

        loginPage.takeScreenshot("continue-disabled-6-digits")
    }

    @Test
    fun `should keep Continue disabled for 9-digit number`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("123456789")
        assert(!loginPage.isContinueButtonEnabled()) { "Continue should be disabled for 9-digit number" }

        loginPage.takeScreenshot("continue-disabled-9-digits")
    }

    @Test
    fun `should enable Continue for valid 10-digit number`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("9999999999")
        assert(loginPage.isContinueButtonEnabled()) { "Continue should be enabled for 10-digit number" }

        loginPage.takeScreenshot("continue-enabled-valid-number")
    }

    @Test
    fun `should handle clearing and re-entering mobile number`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("9999999999")
        assert(loginPage.isContinueButtonEnabled()) { "Continue should be enabled" }

        loginPage.clearMobileNumber()
        assert(!loginPage.isContinueButtonEnabled()) { "Continue should be disabled after clearing" }

        loginPage.enterMobileNumber("8888888888")
        assert(loginPage.isContinueButtonEnabled()) { "Continue should be enabled after re-entering" }

        loginPage.takeScreenshot("clear-and-reenter-number")
    }

    // ----------------------- WhatsApp Checkbox Tests ----------------------

    @Test
    fun `should toggle WhatsApp checkbox correctly`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("9999999999")

        assert(!loginPage.isWhatsAppCheckboxChecked()) { "Should be unchecked initially" }

        loginPage.toggleWhatsAppCheckbox()
        assert(loginPage.isWhatsAppCheckboxChecked()) { "Should be checked after toggle" }

        loginPage.toggleWhatsAppCheckbox()
        assert(!loginPage.isWhatsAppCheckboxChecked()) { "Should be unchecked after second toggle" }

        loginPage.takeScreenshot("whatsapp-checkbox-toggle")
    }

    @Test
    fun `should keep Continue enabled regardless of checkbox state`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.enterMobileNumber("9999999999")

        repeat(3) {
            loginPage.toggleWhatsAppCheckbox()
            assert(loginPage.isContinueButtonEnabled()) { "Continue should remain enabled" }
        }

        loginPage.takeScreenshot("continue-enabled-multiple-toggles")
    }
}