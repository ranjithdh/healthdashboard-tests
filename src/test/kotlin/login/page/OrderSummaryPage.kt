package login.page

import home.page.HomePage
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import utils.logger.logger


class OrderSummaryPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login" // Verify if URL changes

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

    fun isProductNameVisible(productName: String = "Longevity Panel"): Boolean {
        return byText(productName).isVisible
    }

    fun isTotalVisible(): Boolean {
        return byText("Total").isVisible
    }

    fun isReferralDiscountVisible(): Boolean {
        return byText("Referral discount").isVisible
    }

    fun getCouponErrorText(): String? {
        val errorNode = byText("Invalid Referral Code")
        return if (errorNode.isVisible) errorNode.textContent() else null
    }

    fun isInvalidCouponErrorVisible(): Boolean {
        return byText("Invalid Referral Code").isVisible
    }

    fun isCouponAppliedSuccessVisible(): Boolean {
        return byText("Offer has been successfully").isVisible
    }

    fun isCouponCodeAppliedVisible(code: String): Boolean {
        return page.locator("div")
            .filter(com.microsoft.playwright.Locator.FilterOptions().setHasText(java.util.regex.Pattern.compile("^$code applied$")))
            .nth(1)
            .isVisible
    }
    
    fun removeCoupon(): OrderSummaryPage {
        logger.info { "removeCoupon()" }
        byRole(AriaRole.IMG).nth(3).click()
        return this
    }

    fun getTotalAmountText(): String {
        // Assuming the total amount is text in a specific place or we look for the price pattern
        // The user said: page.getByText("₹9,999")
        // We will look for elements containing "₹" and return the visible one(s) or specific logic.
        // For now, let's just make a method to check visibility of a specific amount string as per request logic,
        // or return a found amount. 
        // User request validation style: page.getByText("₹9,999")
        
        // Let's rely on checking visibility of specific text for the test as requested.
        return "" 
    }

    fun isTotalAmountVisible(amount: String): Boolean {
        // "Total" usually has 'text-xl' class as seen in error loop: <span class="text-foreground text-xl font-medium">₹9,999</span>
        // The item price has 'text-base'.
        // So we filter by the class to be specific to the Total.
        return page.locator("span.text-xl").getByText(amount).isVisible
    }

    fun isCouponValueVisible(value: String): Boolean {
        return byText(value).isVisible
    }
}
