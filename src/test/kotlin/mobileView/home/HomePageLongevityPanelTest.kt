package mobileView.home

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import org.junit.jupiter.api.*
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_HOME)
class BloodTestWaitingCardTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

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


}

fun checkBloodTestBookedCardStatus(homePage: HomePage) {
    homePage.waitForBloodTestCardToLoad()
    assertTrue(homePage.isPhlebotomistAssignedTitleVisible())
    assertTrue(homePage.isPhlebotomistAssignedDateVisible())

    assertTrue(homePage.isSampleCollectionTitleVisible())
    assertTrue(homePage.isSampleCollectionDateVisible())

    assertTrue(homePage.isLabProcessingTitleVisible())
    assertTrue(homePage.isLabProcessingTimeVisible())

    assertTrue(homePage.isDashBoardReadyToViewTitleVisible())
    assertTrue(homePage.isDashBoardReadyToViewDateVisible())
}