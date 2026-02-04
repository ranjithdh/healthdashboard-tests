package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import mu.KotlinLogging
import utils.report.StepHelper
import utils.report.StepHelper.CLEAR_MOBILE_NUMBER
import utils.report.StepHelper.CLICK_CONTINUE_BUTTON
import utils.report.StepHelper.CLICK_LOGIN_LINK
import utils.report.StepHelper.CLICK_PRIVACY_POLICY
import utils.report.StepHelper.CLICK_SIGN_UP_LINK
import utils.report.StepHelper.CLICK_TERMS_OF_SERVICE
import utils.report.StepHelper.ENTER_MOBILE_NUMBER
import utils.report.StepHelper.LOGIN_WITH_MOBILE_AND_CONTINUE
import utils.report.StepHelper.SELECT_COUNTRY_CODE
import utils.report.StepHelper.TOGGLE_WHATSAPP_CHECKBOX

private val logger = KotlinLogging.logger {}


class LoginPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_URL


    fun enterMobileNumber(phoneNumber: String): LoginPage {
        StepHelper.step(ENTER_MOBILE_NUMBER + phoneNumber)
        logger.info { "enterMobileNumber($phoneNumber)" }
        utils.SignupDataStore.update { mobileNumber = phoneNumber }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).fill(phoneNumber)
        return this
    }

    fun clearMobileNumber(): LoginPage {
        StepHelper.step(CLEAR_MOBILE_NUMBER)
        logger.info { "clearMobileNumber()" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).clear()
        return this
    }

    fun getMobileNumberValue(): String {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).inputValue()
    }


    fun clickContinue(): LoginPage {
        StepHelper.step(CLICK_CONTINUE_BUTTON)
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    fun selectCountryCode(countryName: String) {
        StepHelper.step(SELECT_COUNTRY_CODE + countryName)
        logger.info { "selectCountryCode($countryName)" }
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("+")).click()
        page.getByPlaceholder("Search country...").click()
        page.getByPlaceholder("Search country...").fill(countryName)
        page.getByText(countryName).nth(1).click()
    }

    fun enterMobileAndContinue(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): OtpPage {
        StepHelper.step(LOGIN_WITH_MOBILE_AND_CONTINUE)
        logger.info { "enterMobileAndContinue(${testUser.mobileNumber})" }
        page.waitForTimeout(5000.0)
        selectCountryCode(testUser.country)
        enterMobileNumber(testUser.mobileNumber)
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
        StepHelper.step(TOGGLE_WHATSAPP_CHECKBOX)
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
        return byText("Don’t have an account? Sign up here").isVisible
    }

    fun isPrivacyPolicyLinkVisible(): Boolean {
        return byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Privacy Policy")).isVisible
    }

    fun isTermsOfServiceLinkVisible(): Boolean {
        return byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Terms of Service")).isVisible
    }

    fun clickLogin(): LoginPage {
        StepHelper.step(CLICK_LOGIN_LINK)
        logger.info { "clickLogin()" }
        byText("Log in").click()
        return this
    }

    fun clickSignUp(): LoginPage {
        StepHelper.step(CLICK_SIGN_UP_LINK)
        logger.info { "clickSignUp()" }
        byText("Sign up here").click()
        return this
    }


    fun isSignUpStatsTextVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Signup")).isVisible
    }

    fun itAllStartsWith100LabTest(): Boolean {
        return page.getByText("It all starts with 100+ biomarkers. All for just ₹9,999.").isVisible
    }

    fun alreadyHaveAnAccountLinkVisible(): Boolean {
        return page.getByText("Already have an account? Log in").isVisible
    }

    fun isSendOtpOnWhatsAppVisible(): Boolean {
        return page.getByText("Send OTP on WhatsApp").isVisible &&
                page.getByRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isVisible
    }


    fun whatsIncludedSectionContentVisible(): Boolean {
        return page.getByText("100+ biomarkers for your whole body").isVisible &&
                page.getByText("1:1 consultation with our expert included").isVisible &&
                page.getByText("Expert-led personalised action plan").isVisible &&
                page.getByText("One secure place for all your health results").isVisible
    }


    fun clickPrivacyPolicyAndVerifyPopup(): Boolean {
        StepHelper.step(CLICK_PRIVACY_POLICY)
        logger.info { "clickPrivacyPolicyAndVerifyPopup()" }
        val popup = page.waitForPopup {
            byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Privacy Policy")).click()
        }
        popup.waitForLoadState()
        val headingVisible = popup.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Deep Holistics Privacy Policy")
        ).isVisible
        popup.close()
        return headingVisible
    }

    fun clickTermsOfServiceAndVerifyPopup(): Boolean {
        StepHelper.step(CLICK_TERMS_OF_SERVICE)
        logger.info { "clickTermsOfServiceAndVerifyPopup()" }
        val popup = page.waitForPopup {
            byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Terms of Service")).click()
        }
        popup.waitForLoadState()
        val headingVisible =
            popup.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Deep Holistics Terms of Use")).isVisible
        popup.close()
        return headingVisible
    }
}
