package mobileView.home

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import onboard.page.LoginPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utils.report.Modules

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_HOME)
class HomePageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    private lateinit var homePage: HomePage

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
        homePage = navigateToHomePage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    fun navigateToHomePage(): HomePage {
        val tesUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val homePage = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)
        return homePage
    }

    @Test
    fun `verify baseline score card details`() {
        assertTrue(homePage.isBaselineScoreTitleVisible())
        assertTrue(homePage.isBetaTagVisible())
        assertEquals(homePage.getBaselineScore(), homePage.getBaseLineScoreFromUI())
    }


}

