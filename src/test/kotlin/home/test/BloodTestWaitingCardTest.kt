package home.test

import com.microsoft.playwright.*
import config.TestConfig
import home.page.HomePage
import login.page.LoginPage
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BloodTestWaitingCardTest {

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
            .enterMobileAndContinue(tesUser.mobileNumber)
            .enterOtpAndContinueToMobileHomePage(tesUser.otp)

        if (homePage.isBloodTestCardVisible()){
            checkBloodTestBookedCardStatus(homePage)
        }
    }
}

fun checkBloodTestBookedCardStatus(homePage: HomePage) {
    homePage.waitForBloodTestCardToLoad()
    assert(homePage.isPhlebotomistAssignedTitleVisible())
    assert(homePage.isPhlebotomistAssignedDateVisible())

    assert(homePage.isSampleCollectionTitleVisible())
    assert(homePage.isSampleCollectionDateVisible())

    assert(homePage.isLabProcessingTitleVisible())
    assert(homePage.isLabProcessingTimeVisible())

    assert(homePage.isDashBoardReadyToViewTitleVisible())
    assert(homePage.isDashBoardReadyToViewDateVisible())
}