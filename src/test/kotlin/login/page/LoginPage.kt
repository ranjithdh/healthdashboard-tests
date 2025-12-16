package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * LoginPage - handles the initial login screen with mobile number input.
 * After entering mobile and clicking Continue, use OtpPage for OTP screen.
 */
class LoginPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_PATH

    // ==================== Mobile Number Input ====================

    fun enterMobileNumber(phoneNumber: String): LoginPage {
        logger.info { "enterMobileNumber($phoneNumber)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).fill(phoneNumber)
        return this
    }

    fun clearMobileNumber(): LoginPage {
        logger.info { "clearMobileNumber()" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).clear()
        return this
    }

    fun getMobileNumberValue(): String {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).inputValue()
    }

    // ==================== Continue Button ====================

    fun clickContinue(): LoginPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    /**
     * Enter mobile number and continue to OTP screen.
     * Returns OtpPage for step-based navigation.
     */
    fun enterMobileAndContinue(phoneNumber: String): OtpPage {
        logger.info { "enterMobileAndContinue($phoneNumber)" }
        enterMobileNumber(phoneNumber)
        clickContinue()
        val otpPage = OtpPage(page)
        otpPage.waitForConfirmScreen()
        return otpPage
    }

    fun isContinueButtonVisible(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isEnabled
    }

    // ==================== WhatsApp Checkbox ====================

    fun toggleWhatsAppCheckbox(): LoginPage {
        logger.info { "toggleWhatsAppCheckbox()" }
        byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).click()
        return this
    }

    fun isWhatsAppCheckboxChecked(): Boolean {
        return byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isChecked
    }

    fun isWhatsAppCheckboxVisible(): Boolean {
        return byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isVisible
    }

    // ==================== Page Element Visibility ====================

    fun isLogoVisible(): Boolean {
        return byRole(AriaRole.IMG).first().isVisible
    }

    fun isDeepHolisticsTextVisible(): Boolean {
        return byText("Deep Holistics").isVisible
    }

    fun isMobileInputVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).isVisible
    }

    fun isCountryCodeButtonVisible(): Boolean {
        return byText("+91").isVisible
    }

    fun isLoginHeaderVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Login")).isVisible
    }

    // ==================== Links ====================

    fun isDontHaveAccountLinkVisible(): Boolean {
        return byText("Don't have an account?").isVisible
    }

    fun isAlreadyHaveAnAccountVisible(): Boolean {
        return byText("Already have an account?").isVisible
    }

    fun isPrivacyPolicyLinkVisible(): Boolean {
        return byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Privacy Policy")).isVisible
    }

    fun isTermsOfServiceLinkVisible(): Boolean {
        return byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Terms of Service")).isVisible
    }

    fun clickLogin(): LoginPage {
        logger.info { "clickLogin()" }
        byText("Log in").click()
        return this
    }

    fun clickSignUp(): LoginPage {
        logger.info { "clickSignUp()" }
        byText("Sign up here").click()
        return this
    }
}
