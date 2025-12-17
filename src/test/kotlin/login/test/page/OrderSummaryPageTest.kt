package login.test.page

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import login.page.OrderSummaryPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderSummaryPageTest {

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

    private fun navigateToOrderSummaryPage(): OrderSummaryPage {
        val testUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val orderSummaryPage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToAccountCreation(testUser.otp)
            .fillAndContinue("Test", "User", "test@test.com")
            .fillAndContinue()
            .fillAndContinue("Flat 101","Test Address", "Chennai", "Tamil Nadu", "600001")
            .selectSlotsAndContinue()

        return orderSummaryPage
    }

    @Test
    fun `should display order summary details`() {
        val orderSummaryPage = navigateToOrderSummaryPage()

        assert(orderSummaryPage.isOrderSummaryHeaderVisible()) { "Header should be visible" }
        assert(orderSummaryPage.isProductNameVisible()) { "Product name should be visible" }
        assert(orderSummaryPage.isTotalVisible()) { "Total should be visible" }

        orderSummaryPage.takeScreenshot("order-summary-details")
    }

    @Test
    fun `should handle invalid coupon code`() {
        val orderSummaryPage = navigateToOrderSummaryPage()

        orderSummaryPage.enterCouponCode("INVALID123")
        orderSummaryPage.clickApplyCoupon()

        assert(orderSummaryPage.isInvalidCouponErrorVisible()) { "Error should be visible for invalid code" }

        orderSummaryPage.takeScreenshot("invalid-coupon-error")
    }

    @Test
    fun `should clear coupon code`() {
        val orderSummaryPage = navigateToOrderSummaryPage()

        orderSummaryPage.enterCouponCode("12")
        orderSummaryPage.clearCouponCode()

        orderSummaryPage.takeScreenshot("cleared-coupon-code")
    }


    @Test
    fun `should apply valid coupon and verify discount`() {
        val orderSummaryPage = navigateToOrderSummaryPage()

        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Initial total should be ₹9,999" }

        val couponCode = "D261C0"
        orderSummaryPage.enterCouponCode(couponCode)
        orderSummaryPage.clickApplyCoupon()

        assert(orderSummaryPage.isCouponAppliedSuccessVisible()) { "Success message should be visible" }
        assert(orderSummaryPage.isCouponCodeAppliedVisible(couponCode)) { "Coupon applied text should be visible" }

        assert(orderSummaryPage.isTotalAmountVisible("₹8,999")) { "Total should be discounted to ₹8,999" }

        orderSummaryPage.takeScreenshot("coupon-applied-successfully")

        orderSummaryPage.removeCoupon()

        assert(orderSummaryPage.isCouponValueVisible("- ₹0")) { "Coupon discount should be 0 after removal" }
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Total should revert to ₹9,999" }
        
        orderSummaryPage.takeScreenshot("coupon-removed")
    }
}
