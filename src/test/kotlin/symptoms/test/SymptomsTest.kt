package symptoms.test

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
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SymptomsTest {
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
        /*  val viewport = TestConfig.Viewports.ANDROID
          val contextOptions =
              Browser.NewContextOptions().setViewportSize(viewport.width, viewport.height).setHasTouch(viewport.hasTouch)
                  .setIsMobile(viewport.isMobile).setDeviceScaleFactor(viewport.deviceScaleFactor)

          context = browser.newContext(contextOptions)
          page = context.newPage()*/

        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(1366, 768)   // Desktop resolution
            .setIsMobile(false)
            .setHasTouch(false)

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }


    @Test
    @Order(1)
    fun `report symptoms with validation`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val symptomsMain =
            loginPage.enterMobileAndContinue(testUser)
                .enterOtpAndContinueToInsightsForWeb(testUser.otp)

        symptomsMain.headerValidation()
        symptomsMain.onReportSymptomsButtonClick()
        symptomsMain.dialogValidation()
        symptomsMain.reportOptionsValidations()
        symptomsMain.cancelButtonClick()
        symptomsMain.headerValidation()
        symptomsMain.onReportSymptomsButtonClick()
        symptomsMain.selectAllSymptoms()
        symptomsMain.submitSymptoms()
    }


    @Test
    @Order(2)
    fun `remove symptoms`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val symptomsMain =
            loginPage.enterMobileAndContinue(testUser)
                .enterOtpAndContinueToInsightsForWeb(testUser.otp)
        symptomsMain.headerValidation()
        symptomsMain.resetAllSymptoms()
        symptomsMain.resetConfirmationDialog()
        symptomsMain.cancelConfirmationDialog()
        symptomsMain.resetAllSymptoms()
        symptomsMain.resetConfirmationDialog()
        symptomsMain.continueConfirmationDialog()
        symptomsMain.emptySymptoms()
    }


    @Test
    fun `reported symptoms with validation`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val symptomsMain =
            loginPage.enterMobileAndContinue(testUser)
                .enterOtpAndContinueToInsightsForWeb(testUser.otp)
        symptomsMain.headerValidation()
        symptomsMain.onReportSymptomsValidation()

    }


}