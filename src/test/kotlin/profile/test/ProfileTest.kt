package profile.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProfileTest {
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
    fun teardown() {
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
    fun `should complete full signup flow`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        val homePage = loginPage
            .clickSignUp()
            .enterMobileAndContinue("726408358")
            .enterOtpAndContinueToProfile("678901")
        /*   .fillAndContinue("ranjith", "test", "ranjithkumar.m@mysmitch.com")
           .fillAndContinue("Male", "170", "60")
           .fillAndContinue("456 Main Road", "Delhi", "Delhi", "110001")
           .selectSlotsAndContinue()
           .clickContinue()
           .waitForHomePageConfirmation()*/


        //  checkBloodTestBookedCardStatus(homePage)

        //homePage.takeScreenshot("signup-order-placed")
    }
}