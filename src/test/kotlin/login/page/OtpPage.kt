package login.page

import home.page.HomePage
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging
import java.util.Scanner

private val logger = KotlinLogging.logger {}

/**
 * OTP Page - handles OTP entry after mobile number submission
 */
class OtpPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    fun enterOtp(otp: String): OtpPage {
        logger.info { "enterOtp($otp)" }
        byRole(AriaRole.TEXTBOX).fill(otp)
        return this
    }

    fun clickContinue(): OtpPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    fun enterOtpFromConsole(): BasicDetailsPage {
        println("\n" + "=".repeat(50))
        println("📱 Enter the OTP received on your phone:")
        println("=".repeat(50))

        val scanner = Scanner(System.`in`)
        val otp = scanner.nextLine().trim()

        return enterOtpAndContinueToAccountCreation(otp)
    }

    fun enterOtpAndContinueToAccountCreation(otp: String): BasicDetailsPage {
        enterOtp(otp)
        clickContinue()

        val basicDetailsPage = BasicDetailsPage(page)
        basicDetailsPage.waitForConfirmation()
        return basicDetailsPage
    }


    fun enterOtpAndContinueToMobileHomePage(otp: String): HomePage {
        enterOtp(otp)
        clickContinue()

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    fun clickEdit(): LoginPage {
        logger.info { "clickEdit()" }
        byText("Edit").click()
        return LoginPage(page)
    }

    fun waitForConfirmScreen(): OtpPage {
        logger.info { "waitForConfirmScreen()" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Confirm your number")).waitFor()
        return this
    }

    fun waitAndGetTimerValue(): String? {
        byText("Resend code in").first().waitFor()
        return getResendTimerText()
    }

    // ==================== Visibility Checks ====================

    fun isOnConfirmScreen(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Confirm your number")).isVisible
    }

    fun isOtpInputVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX).isVisible
    }

    fun isResendTimerVisible(): Boolean {
        return byText("Resend code in").first().isVisible
    }

    fun getTimerText(): String? {
        return byText("Resend code in").first().textContent()
    }

    fun getResendTimerText(): String? {
        return byText("Resend code in").first().textContent()
    }

    fun isEditButtonVisible(): Boolean {
        return byText("Edit").isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isEnabled
    }

    fun isWhatsAppCheckboxVisible(): Boolean {
        return byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isVisible
    }

    fun isWhatsAppCheckboxChecked(): Boolean {
        return byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isChecked
    }

    fun toggleWhatsAppCheckbox(): OtpPage {
        logger.info { "toggleWhatsAppCheckbox()" }
        byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).click()
        return this
    }
}
