package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.home.HomePage
import model.addontest.OnboardDiagnosticProductList
import utils.logger.logger


class OrderSummaryPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_URL

    private val addOnTestAPIUrl =
       "https://api.stg.dh.deepholistics.com/v4/human-token/diagnostics/onboarding-addon?show_onboarding_addon=true"

    private var diagnosticProductList = OnboardDiagnosticProductList()


    init {
//        monitorTraffic()
//        getAddOnTestList()
    }


//    private fun monitorTraffic() {
//        val requestHandler = { request: com.microsoft.playwright.Request ->
//            if (request.url().contains(addOnTestAPIUrl)) {
//                logger.info { "getAddOnTestList API Request: ${request.method()} ${request.url()}" }
//                request.postData()?.let {
//                    logger.info { "getAddOnTestList API Request Payload: $it" }
//                }
//            }
//        }
//
//        val responseHandler = { response: Response ->
//            if (response.url().contains(addOnTestAPIUrl)) {
//                logger.info { "getAddOnTestList API Response: ${response.status()} ${response.url()}" }
//                try {
//                    logger.info { "getAddOnTestList API Response Body: ${response.text()}" }
//                } catch (e: Exception) {
//                    logger.warn { "getAddOnTestList Could not read response body: ${e.message}" }
//                }
//            }
//        }
//
//        page.onRequest(requestHandler)
//        page.onResponse(responseHandler)
//
//        try {
//        } finally {
//            page.offRequest(requestHandler)
//            page.offResponse(responseHandler)
//        }
//    }




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
}
