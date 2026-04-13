package mobileView.home.gut.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import mobileView.home.gut.page.GutPage
import onboard.page.LoginPage
import org.junit.jupiter.api.*
import utils.report.Modules
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Epic(Modules.EPIC_GUT)
@Tag("mobile")
class GutTest : BaseTest() {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var gutPage: GutPage

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())

        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
        gutPage = performInitialNavigation()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun performInitialNavigation(): GutPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val gutPage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage().clickGutTab()

        return gutPage
    }

    @Test
    @Order(1)
    fun gutListEmptyView() {
        gutPage.emptyView()
    }

    @Test
    @Order(2)
    fun gutListVerification() {
        gutPage.gutListValidation()
    }

    @Test
    @Order(3)
    fun gutDetailsVerification() {
        gutPage.gutDetailsValidation()
    }


    @Test
    @Order(3)
    fun gutListSearchView() {
        gutPage.gutSearchViewValidation()
    }

    @Test
    @Order(4)
    fun gutFilterOptions() {
        gutPage.filterOptions()
    }

}