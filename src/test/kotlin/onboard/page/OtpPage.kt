package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import config.TestConfig.SECRET_KEY
import config.TestUser
import kotlinx.serialization.encodeToString
import mobileView.home.HomePage
import mobileView.profile.page.ProfilePage
import model.signup.GetOtpRequest
import model.signup.OtpResponse
import model.signup.VerifyOtpResponse
import utils.LogFullApiCall.logFullApiCall
import utils.Normalize.refactorCountryCode
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_CONTINUE
import utils.report.StepHelper.CLICK_EDIT_BUTTON
import utils.report.StepHelper.ENTER_OTP
import utils.report.StepHelper.ENTER_OTP_ACCOUNT_CREATION
import utils.report.StepHelper.ENTER_OTP_HEALTH_DATA
import utils.report.StepHelper.ENTER_OTP_HOME
import utils.report.StepHelper.ENTER_OTP_INSIGHTS
import utils.report.StepHelper.ENTER_OTP_LAB_TEST
import utils.report.StepHelper.ENTER_OTP_MOBILE_HOME
import utils.report.StepHelper.ENTER_OTP_PROFILE
import utils.report.StepHelper.NAVIGATE_TO
import utils.report.StepHelper.TOGGLE_WHATSAPP_CHECKBOX
import utils.report.StepHelper.WAIT_CONFIRM_SCREEN
import webView.diagnostics.home.HomePageWebsite
import webView.diagnostics.page.LabTestsPage
import webView.diagnostics.symptoms.page.SymptomsPage
import webView.healthdata.page.HealthDataPage


class OtpPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    private var fetchedOtp: String? = null
    private var mobileNumber: String? = null
    private var countryCode: String? = null

    init {
        monitorTraffic()
    }

    fun requestOtp() {
        try {
            if (!mobileNumber.isNullOrBlank() && !countryCode.isNullOrBlank()) {
                val apiContext = page.context().request()
                val url = TestConfig.APIs.GET_OTP

                val headers = mapOf(
                    "client_id" to TestConfig.CLIENT_ID,
                    "Content-Type" to "application/json"
                )

                val requestObj = GetOtpRequest(
                    mobile = mobileNumber!!,
                    country_code = refactorCountryCode(countryCode!!),
                    secret_key = SECRET_KEY,
                    service = "DH_VERIFICATION"
                )

                val requestJson = json.encodeToString(requestObj)


                val requestOptions = RequestOptions.create()
                requestOptions.setData(requestJson)
                headers.forEach { (k, v) -> requestOptions.setHeader(k, v) }

                val response = apiContext.post(
                    url,
                    requestOptions
                )

                logFullApiCall(
                    method = "POST",
                    url = url,
                    requestHeaders = headers,
                    requestBody = requestJson,
                    response = response
                )

                logger.info { "[OTP-API] Response status: ${response.status()} for mobile=$mobileNumber" }
                if (response.status() != 200) {
                    StepHelper.step("${StepHelper.OTP_API_FAILED} — status=${response.status()}, body=${response.text()}")
                    logger.error { "[OTP-API] FAILED: status=${response.status()}, body=${response.text()}" }
                    return
                }

                val responseBody = response.text()
                val responseObj = json.decodeFromString<OtpResponse>(responseBody)
                fetchedOtp = responseObj.data.otp
                StepHelper.step(StepHelper.OTP_API_SUCCESS)
                logger.info { "[OTP-API] Successfully fetched OTP from API: $fetchedOtp" }
                logFullApiCall("POST", TestConfig.APIs.GET_OTP, emptyMap(), requestJson, response)
            }
        } catch (e: Exception) {
            logger.error { "Failed to call OTP API: ${e.message}" }
        }
    }


    private fun monitorTraffic() {
        val updateProfileRequest = { request: com.microsoft.playwright.Request ->
            if (request.url().contains(TestConfig.APIs.API_VERIFY_OTP)) {
                logger.info { "OTP Page-->" }
                logger.info { "OTP Page--> API Request: ${request.method()} ${request.url()}" }
                request.postData()?.let {
                    logger.info { "OTP Page--> API Request Payload: $it" }
                }
            }
        }


        val updateProfileResponse = { response: Response ->
            if (response.url().contains(TestConfig.APIs.API_VERIFY_OTP)) {
                logger.info { "OTP Page--> API Response: ${response.status()} ${response.url()}" }
                try {
                    if (response.status() == 200) {
                        val responseBody = response.text()
                        if (responseBody.isNullOrBlank()) {
                            logger.info { "API response body is empty" }
                        } else {
                            val responseObj = json.decodeFromString<VerifyOtpResponse>(responseBody)
                            TestConfig.ACCESS_TOKEN = responseObj.data.accessToken
                            TestConfig.USER_ID = responseObj.data.userId
                            TestConfig.USER_NAME = responseObj.data.piiUser?.name ?: ""
                            logFullApiCall(response)
                        }
                    }
                    logger.info { "OTP Page--> API Response Body: ${response.text()}" }
                } catch (e: Exception) {
                    logger.warn { "OTP Page--> Could not read response body: ${e.message}" }
                }
            }
        }
        page.onRequest(updateProfileRequest)
        page.onResponse(updateProfileResponse)
        try {
        } finally {
            page.offRequest(updateProfileRequest)
            page.offResponse(updateProfileResponse)
        }
    }


    fun enterOtp(otp: String, mobileNumber: String, countryCode: String): OtpPage {
        StepHelper.step(ENTER_OTP + otp)
        this.mobileNumber = mobileNumber
        this.countryCode = countryCode

        StepHelper.step("${StepHelper.OTP_API_REQUESTING}$mobileNumber (countryCode=$countryCode) | URL: ${page.url()}")
        logger.info { "[OTP] Current page URL before OTP entry: ${page.url()}" }
        logger.info { "[OTP] Requesting OTP via API for mobile=$mobileNumber, countryCode=$countryCode" }
        requestOtp()

        val otpToUse = fetchedOtp ?: TestConfig.STATIC_OTP
        if (fetchedOtp != null) {
            StepHelper.step(StepHelper.OTP_API_SUCCESS)
        } else {
            StepHelper.step("${StepHelper.OTP_API_FAILED} | using STATIC_OTP=${TestConfig.STATIC_OTP}")
        }
        logger.info { "[OTP] fetchedOtp=${fetchedOtp}, STATIC_OTP=${TestConfig.STATIC_OTP}, using OTP=$otpToUse" }

        val otpInput = byRole(AriaRole.TEXTBOX)
        if (otpInput.isVisible) {
            StepHelper.step(StepHelper.OTP_INPUT_VISIBLE)
        } else {
            StepHelper.step("${StepHelper.OTP_INPUT_NOT_VISIBLE} | URL: ${page.url()}")
        }
        logger.info { "[OTP] OTP input visible=${otpInput.isVisible}" }
        StepHelper.step("${StepHelper.OTP_FILLING}$otpToUse")
        otpInput.fill(otpToUse)
        StepHelper.step(StepHelper.OTP_FILLED)
        logger.info { "[OTP] OTP field filled with: $otpToUse" }

        //for flipboard url
//        page.getByRole(AriaRole.TEXTBOX).nth(1).fill(fetchedOtp ?: TestConfig.STATIC_OTP)

        return this
    }

    fun clickContinue(): OtpPage {
        StepHelper.step(CLICK_CONTINUE)
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }


    fun enterOtpAndContinueToAccountCreation(testUser: TestUser = TestConfig.TestUsers.NEW_USER): BasicDetailsPage {
        StepHelper.step(ENTER_OTP_ACCOUNT_CREATION)
        enterOtp(testUser.otp, testUser.mobileNumber, testUser.countryCode)

        val basicDetailsPage = BasicDetailsPage(page)
        basicDetailsPage.waitForConfirmation()
        return basicDetailsPage
    }


    fun enterOtpAndContinueToMobileHomePage(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): HomePage {
        StepHelper.step(ENTER_OTP_MOBILE_HOME)
        enterOtp(testUser.otp, testUser.mobileNumber, testUser.countryCode)

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    fun enterOtpAndContinueToProfile(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): ProfilePage {
        StepHelper.step(ENTER_OTP_PROFILE)
        enterOtp(testUser.otp, testUser.mobileNumber, testUser.countryCode)
        val profilePage = ProfilePage(page)

        profilePage.waitForConfirmation()

        return profilePage
    }


    fun enterOtpAndContinueToHomePage(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): HomePage {
        StepHelper.step(ENTER_OTP_HOME)
        enterOtp(testUser.otp, testUser.mobileNumber, testUser.countryCode)
        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    fun clickEdit(): LoginPage {
        StepHelper.step(CLICK_EDIT_BUTTON)
        logger.info { "clickEdit()" }
        byText("Edit").click()
        return LoginPage(page)
    }

    fun waitForConfirmScreen(): OtpPage {
        StepHelper.step(WAIT_CONFIRM_SCREEN)
        logger.info { "waitForConfirmScreen()" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Confirm your number")).waitFor()
        return this
    }

    fun isOnConfirmScreen(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Confirm your number")).isVisible
    }

    fun isOtpInputVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX).isVisible
    }

    fun isResendTimerVisible(): Boolean {
        return byText("Resend code in").first().isVisible
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
        StepHelper.step(TOGGLE_WHATSAPP_CHECKBOX)
        logger.info { "toggleWhatsAppCheckbox()" }
        byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).click()
        return this
    }

    fun enterOtpAndContinueToLabTestForWeb(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): LabTestsPage {
        StepHelper.step(ENTER_OTP_LAB_TEST)
        enterOtp(testUser.otp, testUser.mobileNumber, testUser.countryCode)
//        clickContinue()

        // Create LabTestsPage instance BEFORE navigation to set up response listener
        val labTestPage = LabTestsPage(page)

        // Set up response listener BEFORE navigation to capture API response
        var capturedResponse: Response? = null
        val listener = page.onResponse { response ->
            if (response.url()
                    .contains("https://api.stg.dh.deepholistics.com/v4/human-token/lab-test") && response.status() == 200
            ) {
                capturedResponse = response
            }
        }

        // Navigate to diagnostics (API call happens during this navigation)
        StepHelper.step("$NAVIGATE_TO: https://app.stg.deepholistics.com/diagnostics")
        page.navigate("https://app.stg.deepholistics.com/diagnostics")

        // Remove listener
//        listener.close()

        // Process captured response if available
        if (capturedResponse != null) {
            labTestPage.processApiResponse(capturedResponse!!)
        }

        labTestPage.waitForConfirmation()

        logger.info { "enterOtpAndContinueToHomePage(${testUser.otp}...${page.url()}" }

        return labTestPage
    }

    fun isIncorrectOtpMessageVisible(): Boolean {
        return page.getByText("Incorrect OTP").isVisible
    }

    fun enterOtpAndContinueToWebViewHealthData(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): webView.healthdata.page.HealthDataPage {
        StepHelper.step(ENTER_OTP_HEALTH_DATA)
        enterOtp(testUser.otp, testUser.mobileNumber, testUser.countryCode)

        page.waitForURL {
            page.url().contains(TestConfig.Urls.BASE_URL)
        }
        StepHelper.step("$NAVIGATE_TO: ${TestConfig.Urls.HOME_PAGE_URL}")
        page.navigate(TestConfig.Urls.HOME_PAGE_URL)

        val homePage = webView.HomePage(page)
        homePage.waitForHomePageConfirmation()

        val healthData = homePage.getHealthDataResponse()

        homePage.clickHealthTab()

        val healthDataPage = HealthDataPage(page, healthData)
        healthDataPage.waitForPageLoad()
        return healthDataPage
    }

    fun enterOtpAndContinueToInsightsForWeb(
        otp: String,
        testUser: TestUser = TestConfig.TestUsers.EXISTING_USER
    ): SymptomsPage {
        StepHelper.step(ENTER_OTP_INSIGHTS)
        StepHelper.step("$ENTER_OTP_INSIGHTS | mobile=${testUser.mobileNumber}, otp=$otp")
        logger.info { "[InsightsNav] Starting enterOtpAndContinueToInsightsForWeb with otp=$otp, mobile=${testUser.mobileNumber}, countryCode=${testUser.countryCode}" }

        enterOtp(otp, testUser.mobileNumber, testUser.countryCode)

        StepHelper.step("OTP entered | Current URL: ${page.url()}")
        logger.info { "[InsightsNav] OTP entered. Current URL: ${page.url()}" }

        val homePage = HomePageWebsite(page)
        StepHelper.step(StepHelper.HOME_PAGE_WAIT_START)
        logger.info { "[InsightsNav] Waiting for home page confirmation (looking for 'Help' link)..." }
        try {
            homePage.waitFoWebPageHomePageConfirmation()
            StepHelper.step("${StepHelper.HOME_PAGE_WAIT_DONE} | URL: ${page.url()}")
            logger.info { "[InsightsNav] Home page confirmed. Current URL: ${page.url()}" }
        } catch (e: Exception) {
            StepHelper.step("${StepHelper.HOME_PAGE_WAIT_FAILED} | URL: ${page.url()} | title: ${page.title()}")
            logger.error { "[InsightsNav] FAILED waiting for home page. Current URL: ${page.url()}" }
            logger.error { "[InsightsNav] Page title: ${page.title()}" }
            logger.error { "[InsightsNav] Exception: ${e.message}" }
            throw e
        }

        val symptomsPage = SymptomsPage(page)

        StepHelper.step("$NAVIGATE_TO${TestConfig.Urls.SYMPTOMS_PAGE_URL}")
        logger.info { "[InsightsNav] Navigating to symptoms page: ${TestConfig.Urls.SYMPTOMS_PAGE_URL}" }
        page.navigate(TestConfig.Urls.SYMPTOMS_PAGE_URL)
        StepHelper.step("Navigation triggered | Current URL: ${page.url()}")
        logger.info { "[InsightsNav] After navigate. Current URL: ${page.url()}" }

        try {
            symptomsPage.waitForSymptomsPageConfirmation()
            StepHelper.step("Symptoms page confirmed | URL: ${page.url()}")
            logger.info { "[InsightsNav] Symptoms page confirmed. Current URL: ${page.url()}" }
        } catch (e: Exception) {
            StepHelper.step("Symptoms page wait FAILED | URL: ${page.url()} | title: ${page.title()}")
            logger.error { "[InsightsNav] FAILED waiting for symptoms page. Current URL: ${page.url()}" }
            logger.error { "[InsightsNav] Page title: ${page.title()}" }
            logger.error { "[InsightsNav] Exception: ${e.message}" }
            throw e
        }

        return symptomsPage
    }

}

