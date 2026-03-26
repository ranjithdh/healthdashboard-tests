package webView.diagnostics.symptoms.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import onboard.page.LoginPage
import org.junit.jupiter.api.*
import utils.logger.logger
import utils.report.Modules
import utils.report.StepHelper
import webView.diagnostics.symptoms.page.SymptomsPage
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Epic(Modules.EPIC_SYMPTOMS)
class SymptomsTest : BaseTest() {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var symptomsMain: SymptomsPage

    @BeforeAll
    fun setup() {
        StepHelper.step(StepHelper.SYMPTOMS_SETUP_START)
        logger.info { "[SymptomsTest] Starting @BeforeAll setup" }
        try {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
            StepHelper.step("${StepHelper.SYMPTOMS_BROWSER_LAUNCHED} | headless=${TestConfig.Browser.isHeadless}, slowMo=${TestConfig.Browser.SLOW_MO}")
            logger.info { "[SymptomsTest] Browser launched. headless=${TestConfig.Browser.isHeadless}, slowMo=${TestConfig.Browser.SLOW_MO}" }

            val contextOptions = Browser.NewContextOptions()
                .setViewportSize(1366, 768)   // Desktop resolution
                .setIsMobile(false)
                .setHasTouch(false)

            context = browser.newContext(contextOptions)
            page = context.newPage()
            StepHelper.step(StepHelper.SYMPTOMS_PAGE_CREATED)
            logger.info { "[SymptomsTest] Page created with viewport 1366x768" }

            StepHelper.step(StepHelper.SYMPTOMS_NAVIGATION_START)
            symptomsMain = performInitialNavigation()

            StepHelper.step(StepHelper.SYMPTOMS_SETUP_COMPLETE)
            logger.info { "[SymptomsTest] @BeforeAll setup complete" }
        } catch (e: com.microsoft.playwright.TimeoutError) {
            StepHelper.step("${StepHelper.SYMPTOMS_SETUP_TIMEOUT_ERROR} | ${e.message}")
            logger.error { "[SymptomsTest] TimeoutError during setup: ${e.message}" }
            throw e
        } catch (e: Exception) {
            StepHelper.step("${StepHelper.SYMPTOMS_SETUP_UNEXPECTED_ERROR} | ${e.javaClass.simpleName}: ${e.message}")
            logger.error { "[SymptomsTest] Unexpected error during setup: ${e.javaClass.simpleName} — ${e.message}" }
            throw e
        }
    }

    @AfterAll
    fun tearDown() {
        logger.info { "[SymptomsTest] Starting @AfterAll tearDown" }
        if (::context.isInitialized) context.close()
        if (::browser.isInitialized) browser.close()
        if (::playwright.isInitialized) playwright.close()
        StepHelper.step(StepHelper.SYMPTOMS_TEARDOWN_COMPLETE)
        logger.info { "[SymptomsTest] TearDown complete — all browser resources released" }
    }

    private fun performInitialNavigation(): SymptomsPage {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        logger.info { "[SymptomsTest] performInitialNavigation — mobile=${testUser.mobileNumber}, otp=${testUser.otp}" }

        StepHelper.step(StepHelper.SYMPTOMS_STEP_1_START)
        logger.info { "[SymptomsTest] Navigating to Login page: ${TestConfig.Urls.LOGIN_URL}" }
        val loginPage = LoginPage(page).navigate() as LoginPage
        StepHelper.step("${StepHelper.SYMPTOMS_STEP_1_DONE} | URL: ${page.url()}")
        logger.info { "[SymptomsTest] Login page loaded. URL: ${page.url()}" }

        StepHelper.step(StepHelper.SYMPTOMS_STEP_2_START)
        logger.info { "[SymptomsTest] Entering mobile=${testUser.mobileNumber}, countryCode=${testUser.countryCode}" }
        val otpPage = loginPage.enterMobileAndContinue(testUser)
        StepHelper.step("${StepHelper.SYMPTOMS_STEP_2_DONE} | URL: ${page.url()}")
        logger.info { "[SymptomsTest] OTP page reached. URL: ${page.url()}" }

        StepHelper.step(StepHelper.SYMPTOMS_STEP_3_START)
        logger.info { "[SymptomsTest] Entering OTP=${testUser.otp} and navigating to Symptoms page" }
        val symptomsMain = otpPage.enterOtpAndContinueToInsightsForWeb(testUser.otp)
        StepHelper.step("${StepHelper.SYMPTOMS_STEP_3_DONE} | URL: ${page.url()}")
        logger.info { "[SymptomsTest] Symptoms page reached. URL: ${page.url()}" }

        return symptomsMain
    }

    @Test
    @Order(1)
    fun `report symptoms with validation`() {
        symptomsMain.headerValidation()
        symptomsMain.onReportSymptomsButtonClick()
        symptomsMain.dialogValidation()
        symptomsMain.reportOptionsValidations()
        symptomsMain.cancelButtonClick()
        symptomsMain.headerValidation()
        symptomsMain.onReportSymptomsButtonClick()
        symptomsMain.selectAllSymptoms()
        symptomsMain.submitSymptoms()

        // 🔥 NEW: wait for all APIs + then validate
        symptomsMain.waitForApiAndValidate()
    }


    @Test
    @Order(2)
    fun `remove symptoms`() {

        symptomsMain.headerValidation()
        symptomsMain.resetAllSymptoms()
        symptomsMain.resetConfirmationDialog()
        symptomsMain.cancelConfirmationDialog()
        symptomsMain.resetAllSymptoms()
        symptomsMain.resetConfirmationDialog()
        symptomsMain.continueConfirmationDialog()
        symptomsMain.emptySymptoms()
    }
}


