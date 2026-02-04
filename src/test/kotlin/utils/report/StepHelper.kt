package utils.report

import io.qameta.allure.Allure

object StepHelper {

    fun step(name: String) {
        Allure.step(name)
    }

    const val ENTER_MOBILE_NUMBER = "Enter mobile number: "
    const val CLEAR_MOBILE_NUMBER = "Clear Mobile Number"
    const val CLICK_CONTINUE_BUTTON = "Click Continue button"
    const val SELECT_COUNTRY_CODE = "Select country code: "
    const val LOGIN_WITH_MOBILE_AND_CONTINUE = "Login with mobile number and continue to OTP"
    const val TOGGLE_WHATSAPP_CHECKBOX = "Toggle WhatsApp checkbox"
    const val CLICK_LOGIN_LINK = "Click Login link"
    const val CLICK_SIGN_UP_LINK = "Click Sign Up link"
    const val CLICK_PRIVACY_POLICY = "Click Privacy Policy and verify popup"
    const val CLICK_TERMS_OF_SERVICE = "Click Terms of Service and verify popup"

}