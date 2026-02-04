package onboard.test.page

import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import onboard.page.OrderSummaryPage
import org.junit.jupiter.api.*
import utils.OnboardAddOnTestDataStore
import utils.SignupDataStore
import java.text.DecimalFormat
import kotlin.collections.forEach


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderSummaryPageTest : BaseTest() {

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
        val viewport = TestConfig.Viewports.MOBILE_LANDSCAPE
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        context.setDefaultTimeout(TestConfig.Browser.TIMEOUT * 2)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    private fun navigateToOrderSummaryPage(): OrderSummaryPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER

        val orderSummaryPage = loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
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
        
        orderSummaryPage.enterCouponCode(TestConfig.Coupons.INVALID_COUPON)
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

        val couponCode = TestConfig.Coupons.VALID_COUPON
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


    @Test
    fun `add and remove all the add on tests`() {
        val orderSummaryPage = navigateToOrderSummaryPage()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Initial total should be ₹9,999" }

        orderSummaryPage.addAllTheAddOnTests()

        val addOnTests = OnboardAddOnTestDataStore.get()

        var totalAmount = 9999f

        addOnTests.packages?.forEach {
            totalAmount += it.product?.price?.toFloat() ?: 0.0f
        }

        addOnTests.tests?.forEach {
            totalAmount += it.product?.price?.toFloat() ?: 0.0f
        }

        addOnTests.test_profiles?.forEach {
            totalAmount += it.product?.price?.toFloat() ?: 0.0f
        }

        val formatter = DecimalFormat("#,###")
        val totalAmountToShow = formatter.format(totalAmount)

        assert(orderSummaryPage.isTotalAmountVisible("₹$totalAmountToShow")) { "Initial total should be ₹$totalAmountToShow" }
        orderSummaryPage.removeAllTheAddOnTests()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Initial total should be ₹9,999" }
    }

    @Test
    fun `should add and remove first add-on test`() {
        val orderSummaryPage = navigateToOrderSummaryPage()
        orderSummaryPage.addFirstAddOn()
        val addOnName = orderSummaryPage.getFirstAddOnName()
        SignupDataStore.update { selectedAddOns.add(addOnName) }
        
        val price = getAddOnPrice(addOnName)
        val expectedTotal = DecimalFormat("#,###").format(9999f + price)
        assert(orderSummaryPage.isTotalAmountVisible("₹$expectedTotal")) { "Total should be ₹$expectedTotal after adding $addOnName" }
        assert(orderSummaryPage.isProductNameVisible(addOnName)) { "Add-on $addOnName should be visible" }
        
        orderSummaryPage.removeFirstAddOn()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Total should revert to ₹9,999" }
    }

    @Test
    fun `should add and remove second add-on test`() {
        val orderSummaryPage = navigateToOrderSummaryPage()
        orderSummaryPage.addSecondAddOn()
        val addOnName = orderSummaryPage.getSecondAddOnName()
        SignupDataStore.update { selectedAddOns.add(addOnName) }

        val price = getAddOnPrice(addOnName)
        val expectedTotal = DecimalFormat("#,###").format(9999f + price)
        assert(orderSummaryPage.isTotalAmountVisible("₹$expectedTotal")) { "Total should be ₹$expectedTotal after adding $addOnName" }
        assert(orderSummaryPage.isProductNameVisible(addOnName)) { "Add-on $addOnName should be visible" }
        
        orderSummaryPage.removeSecondAddOn()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Total should revert to ₹9,999" }
    }

    @Test
    fun `should add and remove third add-on test`() {
        val orderSummaryPage = navigateToOrderSummaryPage()
        orderSummaryPage.addThirdAddOn()
        val addOnName = orderSummaryPage.getThirdAddOnName()
        SignupDataStore.update { selectedAddOns.add(addOnName) }

        val price = getAddOnPrice(addOnName)
        val expectedTotal = DecimalFormat("#,###").format(9999f + price)
        assert(orderSummaryPage.isTotalAmountVisible("₹$expectedTotal")) { "Total should be ₹$expectedTotal after adding $addOnName" }
        assert(orderSummaryPage.isProductNameVisible(addOnName)) { "Add-on $addOnName should be visible" }
        
        orderSummaryPage.removeThirdAddOn()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Total should revert to ₹9,999" }
    }

    @Test
    fun `should add and remove fourth add-on test`() {
        val orderSummaryPage = navigateToOrderSummaryPage()
        orderSummaryPage.addFourthAddOn()
        val addOnName = orderSummaryPage.getFourthAddOnName()
        SignupDataStore.update { selectedAddOns.add(addOnName) }

        val price = getAddOnPrice(addOnName)
        val expectedTotal = DecimalFormat("#,###").format(9999f + price)
        assert(orderSummaryPage.isTotalAmountVisible("₹$expectedTotal")) { "Total should be ₹$expectedTotal after adding $addOnName" }
        assert(orderSummaryPage.isProductNameVisible(addOnName)) { "Add-on $addOnName should be visible" }
        
        orderSummaryPage.removeFourthAddOn()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Total should revert to ₹9,999" }
    }

    @Test
    fun `should add add-on and apply coupon`() {
        val orderSummaryPage = navigateToOrderSummaryPage()
        
        orderSummaryPage.addFirstAddOn()
        val addOnName = orderSummaryPage.getFirstAddOnName()
        val price = getAddOnPrice(addOnName)
        
        val couponCode = TestConfig.Coupons.VALID_COUPON
        orderSummaryPage.enterCouponCode(couponCode)
        orderSummaryPage.clickApplyCoupon()
        
        val expectedTotal = DecimalFormat("#,###").format(9999f + price - TestConfig.Coupons.DISCOUNT_AMOUNT)
        
        assert(orderSummaryPage.isCouponAppliedSuccessVisible()) { "Success message should be visible" }
        assert(orderSummaryPage.isTotalAmountVisible("₹$expectedTotal")) { 
            "Total should be ₹$expectedTotal after adding $addOnName and applying coupon $couponCode" 
        }
        
        orderSummaryPage.removeFirstAddOn()
        orderSummaryPage.removeCoupon()
        assert(orderSummaryPage.isTotalAmountVisible("₹9,999")) { "Total should revert to ₹9,999" }
    }

    private fun getAddOnPrice(name: String): Float {
        val addOnTests = OnboardAddOnTestDataStore.get()
        
        val packagePrice = addOnTests.packages?.find { it.name == name }?.product?.price?.toFloat()
        if (packagePrice != null) return packagePrice

        val testProfilePrice = addOnTests.test_profiles?.find { it.name == name }?.product?.price?.toFloat()
        if (testProfilePrice != null) return testProfilePrice

        val testPrice = addOnTests.tests?.find { it.name == name }?.product?.price?.toFloat()
        if (testPrice != null) return testPrice

        return 0.0f
    }
}
