package mobileView.orders

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*
import utils.SignupDataStore
import utils.logger.logger
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_ORDERS)
class OrderPageLongevityPanelTest : BaseTest() {

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


    fun navigateToOrdersPage(): OrdersPage {
        val tesUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val oderPage = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)
            .clickProfile()
            .waitForProfilePageToLoad()
            .clickOrdersTab()
        return oderPage
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
        context.setDefaultTimeout(TestConfig.Browser.TIMEOUT)
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
        val ordersPage = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)
            .clickProfile()
            .waitForProfilePageToLoad()
            .clickOrdersTab()

        logger.info { "ordersPage...${SignupDataStore.get().selectedAddOns}" }


        if (ordersPage.waitForLongevityPanelToLOad()) {
            ordersPage.clickOrderStatus()
            if (ordersPage.isBloodTestCardVisible()) {
                checkBloodTestBookedCardStatus(ordersPage)
            }
        }
    }

    @Test
    fun `login and check the blood test status cancelled state`() {
        val ordersPage = navigateToOrdersPage()

        ordersPage.waitForLongevityPanelToLOad()
        ordersPage.clickOrderStatus()


        if (ordersPage.isBloodTestCardVisible()) {
            if (ordersPage.isTBloodTestCancelled()) {
                assert(!ordersPage.isPhlebotomistAssignedDateVisible())
                assert(!ordersPage.isSampleCollectionDateVisible())
                assert(!ordersPage.isLabProcessingTimeVisible())
                assert(!ordersPage.isDashBoardReadyToViewDateVisible())
            }
        }
    }

    fun checkBloodTestBookedCardStatus(ordersPage: OrdersPage) {
        ordersPage.waitForBloodTestCardToLoad()
        assert(ordersPage.isPhlebotomistAssignedTitleVisible())
        assert(ordersPage.isPhlebotomistAssignedDateVisible())

        assert(ordersPage.isSampleCollectionTitleVisible())
//        assert(ordersPage.isSampleCollectionDateVisible())

        assert(ordersPage.isLabProcessingTitleVisible())
        assert(ordersPage.isLabProcessingTimeVisible())

        assert(ordersPage.isDashBoardReadyToViewTitleVisible())
        assert(ordersPage.isDashBoardReadyToViewDateVisible())
    }
}
