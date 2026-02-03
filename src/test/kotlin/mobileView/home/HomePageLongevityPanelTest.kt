package mobileView.home

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import login.page.LoginPage
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

    @Test
    fun `login and check the blood test status booked state`() {
        val tesUser = TestConfig.TestUsers.NEW_USER
        
        val loginPage = LoginPage(page).navigate() as LoginPage
        val homePage = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)

        if (homePage.isBloodTestCardVisible()){
            checkBloodTestBookedCardStatus(homePage)
        }
    }

    @Test
    fun `login and check the blood test status cancelled state`() {
        val tesUser = TestConfig.TestUsers.NEW_USER

        val loginPage = LoginPage(page).navigate() as LoginPage
        val homePage = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)

        if (homePage.isBloodTestCardVisible()){
            if (homePage.isTBloodTestCancelled()){
                assertTrue(!homePage.isPhlebotomistAssignedDateVisible())
                assertTrue(!homePage.isSampleCollectionDateVisible())
                assertTrue(!homePage.isLabProcessingTimeVisible())
                assertTrue(!homePage.isDashBoardReadyToViewDateVisible())
            }
        }
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