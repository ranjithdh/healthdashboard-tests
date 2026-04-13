package mobileView.home.gene.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import mobileView.home.gene.page.GenePage
import mobileView.home.gut.page.GutPage
import onboard.page.LoginPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import utils.report.Modules
import kotlin.test.Test


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Epic(Modules.EPIC_GENE)
@Tag("mobile")
class GeneTest : BaseTest() {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var gutPage: GenePage

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

    private fun performInitialNavigation(): GenePage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val gutPage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage().clickGeneTab()

        return gutPage
    }


    @Order(1)
    @Test
    fun geneEmptyValidation() {
        gutPage.emptyView()
    }

    @Order(2)
    @Test
    fun geneListValidation() {
        gutPage.geneListValidation()
    }

    @Test
    @Order(3)
    fun gutDetailsVerification() {
        gutPage.geneDetailsValidation()
    }


    @Test
    @Order(4)
    fun geneFilterOptions() {
        gutPage.filterOptions()
    }


}