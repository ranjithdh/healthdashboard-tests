package mobileView.actionPlan.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import mobileView.actionPlan.page.ActionPlanPage
import onboard.page.LoginPage
import org.junit.jupiter.api.*
import utils.report.Modules

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Epic(Modules.EPIC_ACTION_PLAN)
class ActionPlanTest : BaseTest() {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var actionPlanPage: ActionPlanPage

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
        // context.setDefaultTimeout(TestConfig.Browser.TIMEOUT * 2)
        page = context.newPage()
        actionPlanPage = performInitialNavigation()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun performInitialNavigation(): ActionPlanPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val actionPage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickActionPlan().waitForConfirmation()
        return actionPage
    }

    @Test
    @Order(1)
    fun nutritionTest() {
        actionPlanPage.captureRecommendationData()
        actionPlanPage.dailyCaloriesIntakeCard()
        actionPlanPage.whatToEat()
        actionPlanPage.searchValidation()
    }


    @Test
    @Order(2)
    fun activityTest() {
        actionPlanPage.captureRecommendationData()

    }

}