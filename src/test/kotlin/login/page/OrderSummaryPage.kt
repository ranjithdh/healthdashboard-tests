package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.home.HomePage
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.ADD_ALL_ADDON_TESTS
import utils.report.StepHelper.ADD_FIRST_ADDON
import utils.report.StepHelper.ADD_SECOND_ADDON
import utils.report.StepHelper.CLEAR_COUPON_CODE
import utils.report.StepHelper.CLICK_APPLY_COUPON
import utils.report.StepHelper.CLICK_CHECKOUT
import utils.report.StepHelper.ENTER_COUPON_CODE
import utils.report.StepHelper.REMOVE_ALL_ADDON_TESTS
import utils.report.StepHelper.REMOVE_COUPON


class OrderSummaryPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_URL

    fun enterCouponCode(code: String): OrderSummaryPage {
        StepHelper.step(ENTER_COUPON_CODE + code)
        logger.info { "enterCouponCode($code)" }
        if (!isCouponInputVisible()) {
            byText("Have a referral/ coupon code").click()
        }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).fill(code)
        return this
    }

    fun clickApplyCoupon(): OrderSummaryPage {
        StepHelper.step(CLICK_APPLY_COUPON)
        logger.info { "clickApplyCoupon()" }
        val button = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Apply"))
        if (button.isVisible) {
            button.click()
        } else {
            byText("Apply").click()
        }
        return this
    }

    fun clearCouponCode(): OrderSummaryPage {
        StepHelper.step(CLEAR_COUPON_CODE)
        logger.info { "clearCouponCode()" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).clear()
        return this
    }

    fun clickCheckout(): HomePage {
        StepHelper.step(CLICK_CHECKOUT)
        logger.info { "clickCheckout()" }
        page.getByRole(AriaRole.BUTTON,Page.GetByRoleOptions().setName("Checkout")).click()

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }


    fun waitForConfirmation(): OrderSummaryPage {
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Order summary")).waitFor()
        return this
    }

    fun isOrderSummaryHeaderVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Order summary")).isVisible
    }

    fun isCouponInputVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).isVisible
    }

    fun isProductNameVisible(productName: String = "Baseline Test"): Boolean {
        return byText(productName).isVisible
    }

    fun isTotalVisible(): Boolean {
        return page.getByText("Total", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isInvalidCouponErrorVisible(): Boolean {
        return byText("Invalid Referral Code").isVisible
    }

    fun isCouponAppliedSuccessVisible(): Boolean {
        return byText("Offer has been successfully").isVisible
    }

    fun isCouponCodeAppliedVisible(code: String): Boolean {
        return page.locator("div")
            .filter(
                com.microsoft.playwright.Locator.FilterOptions()
                    .setHasText(java.util.regex.Pattern.compile("^$code applied$"))
            )
            .nth(1)
            .isVisible
    }

    fun removeCoupon(): OrderSummaryPage {
        StepHelper.step(REMOVE_COUPON)
        logger.info { "removeCoupon()" }
        byRole(AriaRole.IMG).nth(3).click()
        return this
    }

    fun isTotalAmountVisible(amount: String): Boolean {
        return page.locator("span.text-xl").getByText(amount).isVisible
    }

    fun isCouponValueVisible(value: String): Boolean {
        return byText(value).isVisible
    }

    private val firstTest = page.getByTestId("addon-card-add-button-11")
    private val secondTest = page.getByTestId("addon-card-add-button-12")
    private val thirdTest = page.getByTestId("addon-card-add-button-4")
    private val fourthTest = page.getByTestId("addon-card-add-button-34")

    private val removeFirstTest = page.getByTestId("selected-addon-remove-11")
    private val removeSecondTest = page.getByTestId("selected-addon-remove-12")
    private val removeThirdTest = page.getByTestId("selected-addon-remove-4")
    private val removeFourthTest = page.getByTestId("selected-addon-remove-34")

    private val firstTestName = page.getByTestId("selected-addon-name-11")
    private val secondTestName = page.getByTestId("selected-addon-name-12")
    private val thirdTestName = page.getByTestId("selected-addon-name-4")
    private val fourthTestName = page.getByTestId("selected-addon-name-34")

    private val totalAmount = page.getByText("Total₹9,999", Page.GetByTextOptions().setExact(true))


    fun addAllTheAddOnTests() {
        StepHelper.step(ADD_ALL_ADDON_TESTS)
        firstTest?.click()
        secondTest?.click()
        thirdTest?.click()
        fourthTest?.click()
    }

    fun removeAllTheAddOnTests() {
        StepHelper.step(REMOVE_ALL_ADDON_TESTS)
        removeFirstTest?.click()
        removeSecondTest?.click()
        removeThirdTest?.click()
        removeFourthTest?.click()
    }

    fun addFirstAddOn() {
        StepHelper.step(ADD_FIRST_ADDON)
        firstTest.click()
    }

    fun addSecondAddOn() {
        StepHelper.step(ADD_SECOND_ADDON)
        secondTest.click()
    }

    fun addThirdAddOn() {
        thirdTest.click()
    }

    fun addFourthAddOn() {
        fourthTest.click()
    }

    fun removeFirstAddOn() {
        removeFirstTest.click()
    }

    fun removeSecondAddOn() {
        removeSecondTest.click()
    }

    fun removeThirdAddOn() {
        removeThirdTest.click()
    }

    fun removeFourthAddOn() {
        removeFourthTest.click()
    }

    fun getFirstAddOnName(): String {
        return firstTestName.innerText()
    }

    fun getSecondAddOnName(): String {
        return secondTestName.innerText()
    }

    fun getThirdAddOnName(): String {
        return thirdTestName.innerText()
    }

    fun getFourthAddOnName(): String {
        return fourthTestName.innerText()
    }
}
