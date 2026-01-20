package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.home.HomePage
import utils.logger.logger


class OrderSummaryPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_URL

    fun enterCouponCode(code: String): OrderSummaryPage {
        logger.info { "enterCouponCode($code)" }
        if (!isCouponInputVisible()) {
            byText("Have a referral/ coupon code").click()
        }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).fill(code)
        return this
    }

    fun clickApplyCoupon(): OrderSummaryPage {
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
        logger.info { "clearCouponCode()" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).clear()
        return this
    }

    fun clickContinue(): HomePage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    fun clickContinueToPayment(): PaymentPage {
        logger.info { "clickContinueToPayment()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return PaymentPage(page)
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
        firstTest?.click()
        secondTest?.click()
        thirdTest?.click()
        fourthTest?.click()
    }

    fun removeAllTheAddOnTests() {
        removeFirstTest?.click()
        removeSecondTest?.click()
        removeThirdTest?.click()
        removeFourthTest?.click()
    }

    fun addFirstAddOn() {
        firstTest.click()
    }

    fun addSecondAddOn() {
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
