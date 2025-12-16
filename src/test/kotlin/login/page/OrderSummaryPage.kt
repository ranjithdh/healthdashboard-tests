package login.page

import home.page.HomePage
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * OrderSummaryPage - Handles order review, coupon application, and final continuation.
 */
class OrderSummaryPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login" // Verify if URL changes
    override val pageLoadedSelector = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Order summary")).toString()

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
        homePage.waitForHomePageConfirmation()

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
}
