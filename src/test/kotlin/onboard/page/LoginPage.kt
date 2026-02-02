package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import io.qameta.allure.Step
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class LoginPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_URL


    @Step("Enter mobile number: {phoneNumber}")
    fun enterMobileNumber(phoneNumber: String): LoginPage {
        logger.info { "enterMobileNumber($phoneNumber)" }
        utils.SignupDataStore.update { mobileNumber = phoneNumber }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).fill(phoneNumber)
        return this
    }

    @Step("Clear Mobile Number")
    fun clearMobileNumber(): LoginPage {
        logger.info { "clearMobileNumber()" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).clear()
        return this
    }

    fun getMobileNumberValue(): String {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).inputValue()
    }


    @Step("Click Continue button")
    fun clickContinue(): LoginPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    @Step("Select country code: {countryName}")
    fun selectCountryCode(countryName: String) {
        logger.info { "selectCountryCode($countryName)" }
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("+")).click()
        page.getByPlaceholder("Search country...").click()
        page.getByPlaceholder("Search country...").fill(countryName)
        page.getByText(countryName).nth(1).click()
    }

    @Step("Login with mobile number and continue to OTP")
    fun enterMobileAndContinue(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): OtpPage {
        logger.info { "enterMobileAndContinue(${testUser.mobileNumber})" }
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


    @Step("Toggle WhatsApp checkbox")
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
        return byText("Don’t have an account? Sign up here").isVisible
    }

    fun isPrivacyPolicyLinkVisible(): Boolean {
        return byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Privacy Policy")).isVisible
    }

    fun isTermsOfServiceLinkVisible(): Boolean {
        return byRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Terms of Service")).isVisible
    }

    @Step("Click Login link")
    fun clickLogin(): LoginPage {
        logger.info { "clickLogin()" }
        byText("Log in").click()
        return this
    }

    @Step("Click Sign Up link")
    fun clickSignUp(): LoginPage {
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



    @Step("Click Privacy Policy and verify popup")
    fun clickPrivacyPolicyAndVerifyPopup(): Boolean {
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

    @Step("Click Terms of Service and verify popup")
    fun clickTermsOfServiceAndVerifyPopup(): Boolean {
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
