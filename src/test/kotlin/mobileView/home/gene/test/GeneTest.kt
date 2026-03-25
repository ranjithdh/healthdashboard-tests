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
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import utils.report.Modules
import utils.report.StepHelper
import kotlin.test.Test


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Epic(Modules.EPIC_GENE)
class GeneTest : BaseTest() {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var gutPage: GenePage
    private var setupError: Throwable? = null

    @BeforeAll
    fun setup() {
        try {
            StepHelper.step(StepHelper.GENE_SETUP_START)
            playwright = Playwright.create()
            browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
            StepHelper.step("${StepHelper.GENE_BROWSER_LAUNCHED}: headless=${TestConfig.Browser.isHeadless}")

            val viewport = TestConfig.Viewports.ANDROID
            val contextOptions = Browser.NewContextOptions()
                .setViewportSize(viewport.width, viewport.height)
                .setHasTouch(viewport.hasTouch)
                .setIsMobile(viewport.isMobile)
                .setDeviceScaleFactor(viewport.deviceScaleFactor)

            context = browser.newContext(contextOptions)
            page = context.newPage()
            page.setDefaultTimeout(TestConfig.Browser.TIMEOUT)
            StepHelper.step("${StepHelper.GENE_PAGE_CREATED}: timeout=${TestConfig.Browser.TIMEOUT}ms")

            StepHelper.step(StepHelper.GENE_NAVIGATION_START)
            gutPage = performInitialNavigation()
            StepHelper.step("${StepHelper.GENE_SETUP_COMPLETE}: url=${page.url()}")

        } catch (e: com.microsoft.playwright.TimeoutError) {
            setupError = e
            StepHelper.step("${StepHelper.GENE_SETUP_TIMEOUT_ERROR}: url=${runCatching { page.url() }.getOrDefault("unknown")}")
            StepHelper.step("Page title: ${runCatching { page.title() }.getOrDefault("unknown")}")
            StepHelper.step("Body text: ${runCatching { page.locator("body").innerText() }.getOrDefault("unavailable")}")
            takeScreenshotOnFailure("gene_setup_timeout")
            throw e

        } catch (e: Exception) {
            setupError = e
            StepHelper.step("${StepHelper.GENE_SETUP_UNEXPECTED_ERROR}: ${e::class.simpleName} → ${e.message}")
            StepHelper.step("URL at failure: ${runCatching { page.url() }.getOrDefault("unknown")}")
            takeScreenshotOnFailure("gene_setup_error")
            throw e
        }
    }

    @AfterAll
    fun tearDown() {
        if (setupError != null) {
            StepHelper.step("${StepHelper.GENE_TEARDOWN_AFTER_FAILURE}: ${setupError!!::class.simpleName}")
        }
        runCatching { context.close() }
        runCatching { browser.close() }
        runCatching { playwright.close() }
        StepHelper.step(StepHelper.GENE_TEARDOWN_COMPLETE)
    }

    private fun takeScreenshotOnFailure(name: String) {
        try {
            val screenshotDir = java.io.File(TestConfig.Artifacts.SCREENSHOT_DIR)
            screenshotDir.mkdirs()
            val file = screenshotDir.resolve("$name-${System.currentTimeMillis()}.png")
            page.screenshot(com.microsoft.playwright.Page.ScreenshotOptions().setPath(file.toPath()).setFullPage(true))
            StepHelper.step("${StepHelper.GENE_SCREENSHOT_SAVED}: ${file.absolutePath}")
        } catch (ex: Exception) {
            StepHelper.step("${StepHelper.GENE_SCREENSHOT_FAILED}: ${ex.message}")
        }
    }

    private fun performInitialNavigation(): GenePage {
        StepHelper.step(StepHelper.GENE_STEP_1_START)
        val loginPage = LoginPage(page).navigate() as LoginPage
        StepHelper.step("${StepHelper.GENE_STEP_1_DONE}: url=${page.url()}")

        StepHelper.step(StepHelper.GENE_STEP_2_START)
        val otpPage = loginPage.enterMobileAndContinue()
        StepHelper.step("${StepHelper.GENE_STEP_2_DONE}: url=${page.url()}")

        StepHelper.step(StepHelper.GENE_STEP_3_START)
        val homePage = otpPage.enterOtpAndContinueToHomePage()
        StepHelper.step("${StepHelper.GENE_STEP_3_DONE}: url=${page.url()}")

        StepHelper.step(StepHelper.GENE_STEP_4_START)
        val genePage = homePage.clickGeneTab()
        StepHelper.step("${StepHelper.GENE_STEP_4_DONE}: url=${page.url()}")

        return genePage
    }


    @Order(1)
    @Test
    fun geneEmptyValidation() {
        assumeTrue(setupError == null) { "⚠️ Skipped: setup failed → ${setupError?.message}" }
        gutPage.emptyView()
    }

    @Order(2)
    @Test
    fun geneListValidation() {
        assumeTrue(setupError == null) { "⚠️ Skipped: setup failed → ${setupError?.message}" }
        gutPage.geneListValidation()
    }

    @Test
    @Order(3)
    fun gutDetailsVerification() {
        assumeTrue(setupError == null) { "⚠️ Skipped: setup failed → ${setupError?.message}" }
        gutPage.geneDetailsValidation()
    }

    @Test
    @Order(4)
    fun geneFilterOptions() {
        assumeTrue(setupError == null) { "⚠️ Skipped: setup failed → ${setupError?.message}" }
        gutPage.filterOptions()
    }


}