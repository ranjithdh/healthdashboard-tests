package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import healthdata.page.HealthDataPage
import io.qameta.allure.Step
import webView.diagnostics.page.LabTestsPage
import mobileView.home.HomePage
import model.healthdata.HealthData
import model.signup.VerifyOtpResponse
import profile.page.ProfilePage
import utils.json.json
import webView.diagnostics.symptoms.page.SymptomsPage
import utils.logger.logger
import webView.diagnostics.home.HomePageWebsite


class OtpPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    init {
        monitorTraffic()
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


    @Step("Enter OTP: {otp}")
    fun enterOtp(otp: String): OtpPage {
        logger.info { "enterOtp($otp)" }
        byRole(AriaRole.TEXTBOX).fill(otp)
        return this
    }

    @Step("Click Continue button")
    fun clickContinue(): OtpPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }



    @Step("Enter OTP and continue to account creation")
    fun enterOtpAndContinueToAccountCreation(testUser: TestUser = TestConfig.TestUsers.NEW_USER): BasicDetailsPage {
        enterOtp(testUser.otp)

        val basicDetailsPage = BasicDetailsPage(page)
        basicDetailsPage.waitForConfirmation()
        return basicDetailsPage
    }


    @Step("Enter OTP and continue to mobile home page")
    fun enterOtpAndContinueToMobileHomePage(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): HomePage {
        enterOtp(testUser.otp)

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    @Step("Enter OTP and continue to profile")
    fun enterOtpAndContinueToProfile(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): ProfilePage {
        enterOtp(testUser.otp)
        val profilePage = ProfilePage(page)

        profilePage.waitForConfirmation()

        return profilePage
    }


    @Step("Enter OTP and continue to home page")
    fun enterOtpAndContinueToHomePage(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): HomePage {
        enterOtp(testUser.otp)
        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    @Step("Click Edit button")
    fun clickEdit(): LoginPage {
        logger.info { "clickEdit()" }
        byText("Edit").click()
        return LoginPage(page)
    }

    @Step("Wait for confirm screen")
    fun waitForConfirmScreen(): OtpPage {
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

    @Step("Toggle WhatsApp Checkbox")
    fun toggleWhatsAppCheckbox(): OtpPage {
        logger.info { "toggleWhatsAppCheckbox()" }
        byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).click()
        return this
    }

    @Step("Enter OTP and continue to Lab Test page (Web)")
    fun enterOtpAndContinueToLabTestForWeb(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): LabTestsPage {
        enterOtp(testUser.otp)
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

    @Step("Enter OTP and continue to Health Data")
    fun enterOtpAndContinueToWebViewHealthData(testUser: TestUser = TestConfig.TestUsers.EXISTING_USER): healthdata.page.HealthDataPage {

        enterOtp(testUser.otp)

        page.waitForURL {
            page.url().contains(TestConfig.Urls.BASE_URL)
        }
        page.navigate(TestConfig.Urls.HOME_PAGE_URL)

        val homePage = webView.HomePage(page)
        homePage.waitForHomePageConfirmation()

        val healthData = homePage.getHealthDataResponse()

        homePage.clickHealthTab()

        val healthDataPage = HealthDataPage(page,healthData)
        healthDataPage.waitForPageLoad()
        return healthDataPage
    }

    @Step("Enter OTP and continue to Insights (Web)")
    fun enterOtpAndContinueToInsightsForWeb(otp: String): SymptomsPage {
        enterOtp(otp)

        val homePage = HomePageWebsite(page)
        homePage.waitFoWebPageHomePageConfirmation()

        // Create LabTestsPage instance BEFORE navigation to set up response listener
        val symptomsPage = SymptomsPage(page)

        // Navigate to diagnostics (API call happens during this navigation)
        page.navigate(TestConfig.Urls.SYMPTOMS_PAGE_URL)

        symptomsPage.waitForSymptomsPageConfirmation()


        return symptomsPage
    }

}

