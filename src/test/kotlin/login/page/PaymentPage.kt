package login.page

import com.microsoft.playwright.FrameLocator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mobileView.home.HomePage
import mu.KotlinLogging
import utils.report.StepHelper
import utils.report.StepHelper.COMPLETE_PAYMENT
import utils.report.StepHelper.MANUAL_NAVIGATE_HOME

private val logger = KotlinLogging.logger {}


class PaymentPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    fun completePayment(): PaymentPage {
        StepHelper.step(COMPLETE_PAYMENT)
        logger.info { "completePayment()" }

        page.waitForSelector("iframe")

        val frame = page.locator("iframe").contentFrame()

        frame.getByTestId("contactNumber").click()
        frame.getByTestId("contactNumber").fill("7010165836")

        frame.getByRole(AriaRole.BUTTON, FrameLocator.GetByRoleOptions().setName("Continue")).click()

        page.locator("iframe").nth(1).contentFrame().getByTestId("UPI - Google Pay").click()
        page.waitForPopup {
            page.locator("iframe").nth(1).contentFrame().getByTestId("fee-bearer-cta").click()
        }

        return this
    }

    fun manuallyNavigateToHome(): HomePage {
        StepHelper.step(MANUAL_NAVIGATE_HOME)
        logger.info { "manuallyNavigateToHome()" }
        page.navigate("https://app.stg.deepholistics.com/home")
        return HomePage(page)
    }
}
