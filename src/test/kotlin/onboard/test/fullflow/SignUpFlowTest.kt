package onboard.test.fullflow

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing.StartOptions
import com.microsoft.playwright.Tracing.StopOptions
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import mobileView.home.checkBloodTestBookedCardStatus
import org.junit.jupiter.api.*
import utils.SignupDataStore
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import java.nio.file.Paths
import kotlin.test.assertTrue

@Epic("Login")
@Feature("Sign Up Flow")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignUpFlowTest : BaseTest() {

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
            .setRecordVideoDir(Paths.get(TestConfig.Artifacts.VIDEO_DIR))
            .setRecordVideoSize(390, 844)

        context = browser.newContext(contextOptions)
        context.setDefaultTimeout(TestConfig.Browser.TIMEOUT * 2)
        page = context.newPage()


        context.tracing().start(
            StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
        )

        val videoPath = page.video()?.path()
        println("📹 Video saved to: $videoPath")
    }

    @AfterEach
    fun closeContext() {
        val path = "build/traceView/trace_${System.currentTimeMillis()}.zip"
        context.tracing().stop(
            StopOptions()
                .setPath(Paths.get(path))
        )

        context.close()
    }

    @Test
    fun `should complete full signup flow`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER

        val homePage = loginPage
            .clickSignUp()
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
            .selectSlotsAndContinue()
            .clickCheckout()
            .waitForMobileHomePageConfirmation()


        checkBloodTestBookedCardStatus(homePage)

        assertTrue(
            homePage.isSavedFullSlotMatchingApi(),
            "Selected full slot (Date & Time) should match API response on HomePage"
        )

        homePage.takeScreenshot("signup-order-placed")
    }

    @Test
    fun `complete full signup flow with coupon code`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER

        val homePage = loginPage
            .clickSignUp()
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
            .selectSlotsAndContinue()
            .enterCouponCode(TestConfig.Coupons.VALID_COUPON)
            .clickApplyCoupon()
            .clickCheckout()
            .waitForMobileHomePageConfirmation()


        checkBloodTestBookedCardStatus(homePage)

        assertTrue(
            homePage.isSavedFullSlotMatchingApi(),
            "Selected full slot (Date & Time) should match API response on HomePage"
        )

        homePage.takeScreenshot("signup-order-placed")
    }

    @Test
    fun `complete full signup flow with add-on tests`() {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER

        val orderSummaryPage = loginPage
            .clickSignUp()
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
            .selectSlotsAndContinue()

        val allIndices = (1..4).toList().shuffled()
        val selectedIndices = allIndices.take(2)

        selectedIndices.forEach { index ->
            when (index) {
                1 -> {
                    orderSummaryPage.addFirstAddOn()
                    val name = orderSummaryPage.getFirstAddOnName()
                    SignupDataStore.update { selectedAddOns.add(name) }
                }
                2 -> {
                    orderSummaryPage.addSecondAddOn()
                    val name = orderSummaryPage.getSecondAddOnName()
                    SignupDataStore.update { selectedAddOns.add(name) }
                }
                3 -> {
                    orderSummaryPage.addThirdAddOn()
                    val name = orderSummaryPage.getThirdAddOnName()
                    SignupDataStore.update { selectedAddOns.add(name) }
                }
                4 -> {
                    orderSummaryPage.addFourthAddOn()
                    val name = orderSummaryPage.getFourthAddOnName()
                    SignupDataStore.update { selectedAddOns.add(name) }
                }
            }
        }

        val homePage = orderSummaryPage
            .clickCheckout()
            .waitForMobileHomePageConfirmation()

        checkBloodTestBookedCardStatus(homePage)

        assertTrue(
            homePage.isSavedFullSlotMatchingApi(),
            "Selected full slot (Date & Time) should match API response on HomePage"
        )

        homePage.takeScreenshot("signup-with-addons-placed")
    }
}
