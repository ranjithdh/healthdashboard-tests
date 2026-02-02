package login.page

import com.microsoft.playwright.FrameLocator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mobileView.home.HomePage
import mu.KotlinLogging
import io.qameta.allure.Step

private val logger = KotlinLogging.logger {}


class PaymentPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    @Step("Complete Payment")
    fun completePayment(): PaymentPage {
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

    @Step("Manually Navigate to Home")
    fun manuallyNavigateToHome(): HomePage {
        logger.info { "manuallyNavigateToHome()" }
        page.navigate("https://app.stg.deepholistics.com/home")
        return HomePage(page)
    }
}
