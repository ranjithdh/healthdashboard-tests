package utils.report

import io.qameta.allure.Allure

object StepHelper {

    fun step(name: Any?) {
        Allure.step(name.toString())
    }

    fun logApiResponse(url: String, response: Any?) {
        step("API Response from: $url")
        step(response)
    }

    // Generic
    const val NAVIGATE_TO = "Navigate to "
    const val TAKE_SCREENSHOT = "Take screenshot: "
    const val CLICK_CONTINUE = "Click Continue"
    const val CLICK_EDIT_BUTTON = "Click Edit button"
    const val CLICK_SAVE_CHANGES = "Click Save Changes"
    const val CLICK_CANCEL = "Click Cancel"
    const val CLICK_CLOSE = "Click Close"
    const val FETCH_DATA_FROM_API = "Fetch data from API: "
    const val CLICK_STATIC_ELEMENT = "Click static element: "
    const val VERIFY_STATIC_CONTENT = "Verify static content"
    const val VERIFY_SECTION = "Verify section: "
    const val VERIFY_STEP = "Verify Step: "
    const val VERIFY_PRIVACY_INFO = "Verify privacy information"
    const val VERIFY_LABS_INFO = "Verify labs information"
    const val CLICK_SEARCH_TEXTBOX = "Click search textbox"
    const val CLICK_BOOK_LAB_TESTS_HEADING = "Click Book Lab Tests heading"
    const val CLICK_GET_TESTED_HEADING = "Click Get tested from the comfort heading"
    const val CLICK_FLEXIBLE_TESTING_PARA = "Click paragraph with flexible testing options"

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

    // ProfilePage - Questionnaire
    const val ANSWER_QUESTION = "Answer question: "

    // ProfilePage - Address
    const val FETCH_ADDRESS_DATA = "Fetch address data"
    const val CLICK_ADDRESS_DROPDOWN = "Click address dropdown"
    const val CLICK_ADD_NEW_ADDRESS = "Click Add New Address"
    const val FILL_ADDRESS_FORM_MANDATORY = "Fill mandatory address fields"
    const val REMOVE_USER_ADDRESS = "Remove user address"
    const val EDIT_USER_ADDRESS = "Edit user address"
    const val SUBMIT_ADDRESS = "Submit address"
    const val YES_DELETE = "Yes delete"
    const val CLICK_ACCOUNT_PROFILE = "Click Account Profile"

    // ProfilePage - Preference
    const val FETCH_PREFERENCE = "Fetch communication preference"
    const val SELECT_COMMUNICATION_OPTION = "Select communication option"

    // ProfilePage - Account Info
    const val FETCH_ACCOUNT_INFORMATION = "Fetch account information"
    const val ACCOUNT_INFO_VALIDATION = "Account information validation"
    const val EDIT_PROFILE = "Edit profile"
    const val SAVE_CHANGES = "Save changes"
    const val EDIT_HEALTH_METRICS = "Edit Health Metrics"
    const val SAVE_HEALTH_METRICS = "Save Health Metrics"

    // mobileView - HomePage
    const val WAIT_MOBILE_HOME_CONFIRMATION = "Wait for Mobile Home Page confirmation"
    const val CLICK_PROFILE_ICON = "Click Profile icon"
    const val FETCH_HOME_DATA = "Fetch Home data from API"

    // mobileView - LabTestsPage
    const val NAVIGATE_TO_DIAGNOSTICS = "Navigate to Diagnostics"
    const val VIEW_TEST_DETAILS = "View test details: "
    const val CLICK_FILTER = "Click filter: "

    // mobileView - OrdersPage
    const val CLICK_ORDERS_TAB = "Click Orders tab"
    const val CLICK_ORDER_STATUS = "Click Order Status"

    // mobileView - ServicePage
    const val NAVIGATE_TO_SERVICES = "Navigate to Services"
    const val FETCH_SERVICE_DATA = "Fetch service data"
    const val VERIFY_SERVICE_CARD = "Verify service card: "
    const val CLICK_SCHEDULE_NOW = "Click Schedule Now"

    // webView - LabTestsPage
    const val WAIT_LAB_TESTS_PAGE_LOAD = "Wait for Lab Tests page load"
    const val SEARCH_LAB_TESTS = "Search in lab tests: "
    const val CLICK_TEST_PANEL_ELEMENT = "Click test panel element: "

    // webView - SymptomsPage
    const val WAIT_SYMPTOMS_PAGE_LOAD = "Wait for Symptoms page load"
    const val OPEN_REPORT_SYMPTOMS_DIALOG = "Open Report Symptoms dialog"
    const val EXPAND_SYMPTOMS_SECTION = "Expand symptoms section: "
    const val SELECT_SYMPTOM = "Select symptom: "
    const val SUBMIT_SYMPTOMS = "Submit symptoms"
    const val RESET_SYMPTOMS = "Reset all symptoms"

    // website - General & LandingPage
    const val WAIT_WEBSITE_PAGE_LOAD = "Wait for website page load: "
    const val CLICK_HERO_BOOK_NOW = "Click Hero Book Now"
    const val CLICK_LEARN_MORE = "Click Learn More"
    const val CLICK_WHAT_WE_TEST = "Click What We Test"
    const val CLICK_READ_OUR_WHY = "Click Read Our Why"
    const val CLICK_BOOK_NOW = "Click Book Now"
    const val CLICK_BOOK_NOW_WHAT_INCLUDED = "Click Book Now (What's included section)"

    // website - HeaderSection
    const val NAVIGATE_TO_PAGE = "Navigate to page: "

    // website - WhatWeTestPage
    const val CLICK_BIOMARKER = "Click biomarker: "

    // website - FaqSection
    const val CLICK_FAQ_TAB = "Click FAQ tab: "
    const val VIEW_FAQ_QUESTION = "View FAQ question: "
}