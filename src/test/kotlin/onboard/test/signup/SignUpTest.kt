package onboard.test.signup

import com.microsoft.playwright.*
import config.TestConfig
import onboard.page.LoginPage
import org.junit.jupiter.api.*
import io.qameta.allure.Epic
import io.qameta.allure.Feature

@Epic("Login")
@Feature("Sign Up UI")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignUpTest {

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
    fun `should verify signup page links and texts`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        loginPage.clickSignUp()

        assert(loginPage.isSignUpStatsTextVisible()) { "Stats text should be visible" }
        assert(loginPage.itAllStartsWith100LabTest()) { "Lab tests text should be visible" }
        assert(loginPage.alreadyHaveAnAccountLinkVisible()) { "Blood draw text should be visible" }
        assert(loginPage.isSendOtpOnWhatsAppVisible()) { "Results text should be visible" }
        assert(loginPage.whatsIncludedSectionContentVisible()) { "Section content should be visible" }

        assert(loginPage.clickPrivacyPolicyAndVerifyPopup()) { "Privacy Policy popup header should be visible" }
        assert(loginPage.clickTermsOfServiceAndVerifyPopup()) { "Terms of Service popup header should be visible" }

        loginPage.clickLogin()
        assert(loginPage.isLoginHeaderVisible()) { "Should be back on Login page" }

        loginPage.takeScreenshot("signup-links-verified")
    }


}
