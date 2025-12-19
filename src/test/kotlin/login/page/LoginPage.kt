package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class LoginPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_PATH


    fun enterMobileNumber(phoneNumber: String): LoginPage {
        logger.info { "enterMobileNumber($phoneNumber)" }
        utils.SignupDataStore.update { mobileNumber = phoneNumber }
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


    fun clickContinue(): LoginPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }


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


    fun isDontHaveAccountLinkVisible(): Boolean {
        return byText("Don't have an account?").isVisible
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


    fun isSignUpStatsTextVisible(): Boolean {
        return byText("It all starts with 100+ lab").isVisible
    }

    fun isLabTestsTextVisible(): Boolean {
        return byText("100+ lab tests for your whole body").isVisible
    }

    fun isBloodDrawTextVisible(): Boolean {
        return byText("At-mobileView.home blood-draw").isVisible
    }

    fun isResultsTrackedTextVisible(): Boolean {
        return byText("Results tracked over your lifetime in one secure place").isVisible
    }

    fun clickPrivacyPolicyAndVerifyPopup(): Boolean {
        logger.info { "clickPrivacyPolicyAndVerifyPopup()" }
        val popup = page.waitForPopup {
            byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Privacy Policy")).click()
        }
        popup.waitForLoadState()
        val headingVisible = popup.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Deep Holistics Privacy Policy")).isVisible
        popup.close()
        return headingVisible
    }

    fun clickTermsOfServiceAndVerifyPopup(): Boolean {
        logger.info { "clickTermsOfServiceAndVerifyPopup()" }
        val popup = page.waitForPopup {
            byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Terms of Service")).click()
        }
        popup.waitForLoadState()
        val headingVisible = popup.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Deep Holistics Terms of Use")).isVisible
        popup.close()
        return headingVisible
    }
}
