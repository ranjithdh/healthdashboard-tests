package utils.report

import io.qameta.allure.Allure

object StepHelper {

    fun step(name: String) {
        Allure.step(name)
    }

    // Generic
    const val NAVIGATE_TO = "Navigate to "
    const val TAKE_SCREENSHOT = "Take screenshot: "
    const val CLICK_CONTINUE = "Click Continue"
    const val CLICK_EDIT_BUTTON = "Click Edit button"

    // LoginPage
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

    // OtpPage
    const val ENTER_OTP = "Enter OTP: "
    const val ENTER_OTP_ACCOUNT_CREATION = "Enter OTP and continue to account creation"
    const val ENTER_OTP_MOBILE_HOME = "Enter OTP and continue to mobile home page"
    const val ENTER_OTP_PROFILE = "Enter OTP and continue to profile"
    const val ENTER_OTP_HOME = "Enter OTP and continue to home page"
    const val WAIT_CONFIRM_SCREEN = "Wait for confirm screen"
    const val ENTER_OTP_LAB_TEST = "Enter OTP and continue to Lab Test page (Web)"
    const val ENTER_OTP_HEALTH_DATA = "Enter OTP and continue to Health Data"
    const val ENTER_OTP_INSIGHTS = "Enter OTP and continue to Insights (Web)"

    // BasicDetailsPage
    const val ENTER_FIRST_NAME = "Enter First Name: "
    const val ENTER_EMAIL = "Enter Email: "
    const val FILL_BASIC_DETAILS = "Fill Basic Details"
    const val FILL_BASIC_DETAILS_CONTINUE = "Fill Basic Details and Continue"

    // PersonalDetailsPage
    const val SELECT_DOB = "Select Date of Birth: "
    const val SELECT_GENDER = "Select Gender: "
    const val ENTER_HEIGHT = "Enter Height: "
    const val ENTER_WEIGHT = "Enter Weight: "
    const val FILL_PERSONAL_DETAILS = "Fill Personal Details"
    const val FILL_PERSONAL_DETAILS_CONTINUE = "Fill personal details and continue"

    // AddressPage
    const val ENTER_FLAT_HOUSE_NO = "Enter Flat/House No: "
    const val ENTER_ADDRESS = "Enter Address: "
    const val ENTER_CITY = "Enter City: "
    const val SELECT_STATE = "Select State: "
    const val ENTER_PIN_CODE = "Enter Pin Code: "
    const val FILL_ADDRESS_FORM = "Fill Address Form"
    const val FILL_ADDRESS_DETAILS_CONTINUE = "Fill Address Details and Continue"

    // TimeSlotPage
    const val SELECT_DATE = "Select Date: "
    const val SELECT_SLOT = "Select Slot: "
    const val SELECT_MORNING_SLOT = "Select Morning Slot"
    const val SELECT_POST_MEAL_SLOT = "Select Post Meal Slot"
    const val CLICK_SCHEDULE_BUTTON = "Click Schedule Button"
    const val SELECT_SLOTS_CONTINUE = "Select Slots and Continue"
    const val CLICK_SLOT = "Click Slot: "
    const val CLOSE_INVALID_FASTING_SLOT_POPUP = "Close Invalid Fasting Slot Popup"
    const val SELECT_DATE_FILTER = "Select Date by Filter: "
    const val OPEN_DATE_PICKER = "Open Date Picker"
    const val SELECT_CALENDAR_DATE = "Select Calendar Date: "
    const val SELECT_DATE_VIEW = "Select Date View: "

    // OrderSummaryPage
    const val ENTER_COUPON_CODE = "Enter Coupon Code: "
    const val CLICK_APPLY_COUPON = "Click Apply Coupon"
    const val CLEAR_COUPON_CODE = "Clear Coupon Code"
    const val CLICK_CHECKOUT = "Click Checkout"
    const val REMOVE_COUPON = "Remove Coupon"
    const val ADD_ALL_ADDON_TESTS = "Add All Add-on Tests"
    const val REMOVE_ALL_ADDON_TESTS = "Remove All Add-on Tests"
    const val ADD_FIRST_ADDON = "Add First Add-on"
    const val ADD_SECOND_ADDON = "Add Second Add-on"

    // PaymentPage
    const val COMPLETE_PAYMENT = "Complete Payment"
    const val MANUAL_NAVIGATE_HOME = "Manually Navigate to Home"
}