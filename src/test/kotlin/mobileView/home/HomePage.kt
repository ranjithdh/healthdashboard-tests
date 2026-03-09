package mobileView.home

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import mobileView.LabTestDateHelper.getDashBoardReadyToViewDate
import mobileView.LabTestDateHelper.getPhlebotomistAssignedDate
import mobileView.LabTestDateHelper.getSampleCollectionDate
import mobileView.actionPlan.page.ActionPlanPage
import mobileView.orders.OrdersPage
import mobileView.profile.page.ProfilePage
import model.home.HomeData
import model.home.HomeDataResponse
import utils.DateHelper
import utils.SignupDataStore
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_ACCOUNT_PROFILE
import utils.report.StepHelper.CLICK_ACTION_PLAN
import utils.report.StepHelper.CLICK_PROFILE_ICON
import utils.report.StepHelper.FETCH_HOME_DATA
import utils.report.StepHelper.WAIT_MOBILE_HOME_CONFIRMATION
import utils.report.StepHelper.logApiResponse
import java.util.regex.Pattern

class HomePage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.HOME_PAGE_URL

    val profileImage: Locator = page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("profile"))
    val actionButtonPlan = page.getByText("Action Plan")

    private var homeData: HomeData? = HomeData()
    private var appointmentDate: String? = null


    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = true
        encodeDefaults = true
    }

    fun waitForMobileHomePageConfirmation(): HomePage {
        StepHelper.step(WAIT_MOBILE_HOME_CONFIRMATION)
        logger.info("Waiting for mobileView.home page confirmation...")
        page.waitForURL(TestConfig.Urls.HOME_PAGE_URL)
        return this
    }

    fun isBloodTestCardVisible(): Boolean {
        return page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("Dashboard ready to view")).isVisible
    }

    fun waitForBloodTestCardToLoad(): HomePage {
        page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("Dashboard ready to view"))
            .waitFor()
        return this
    }

    init {
        getExpectedAssignmentDateFromApi()
    }

    fun getExpectedAssignmentDateFromApi() {
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains(TestConfig.APIs.API_HOME) == true && response.status() == 200
            },
            {
                page.waitForURL(TestConfig.Urls.HOME_PAGE_URL)
            }
        )

        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "API response body is empty" }
        }

        logger.info { "API response...${response.text()}" }

        try {
            val responseObj = json.decodeFromString<HomeDataResponse>(responseBody)
            logger.error { "responseObj...$responseObj" }

            if (responseObj.data != null) {
                homeData = responseObj.data
                StepHelper.step(FETCH_HOME_DATA)
                logApiResponse(TestConfig.APIs.API_HOME, responseObj)
                val diagnostic = homeData?.diagnostics?.firstOrNull { it.blood_test_appointment_date != null }
                appointmentDate = diagnostic?.blood_test_appointment_date
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response..${e.message}" }
        }
    }


    fun isPhlebotomistAssignedTitleVisible(): Boolean {
        return page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("Phlebotomist assigned")).isVisible
    }

    fun isPhlebotomistAssignedDateVisible(): Boolean {
        val fullText = "Expected: ${getPhlebotomistAssignedDate(appointmentDate)}"
        return byRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(fullText)).isVisible
    }

    fun isSampleCollectionTitleVisible(): Boolean {
        return page.getByText("Sample collection", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isSampleCollectionDateVisible(): Boolean {
        val sampleCollectionDate = getSampleCollectionDate(appointmentDate)
        return page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText(sampleCollectionDate)).isVisible
    }

    fun isLabProcessingTitleVisible(): Boolean {
        return page.getByText("Lab processing", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isLabProcessingTimeVisible(): Boolean {
        return page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("~24 - 36hrs")).isVisible
    }

    fun isDashBoardReadyToViewTitleVisible(): Boolean {
        return page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("Dashboard ready to view")).isVisible
    }

    fun isDashBoardReadyToViewDateVisible(): Boolean {
        val readyToViewDate = "Expected: ${getDashBoardReadyToViewDate(appointmentDate)}"
        return page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(readyToViewDate)).isVisible
    }

    fun isTBloodTestCancelled(): Boolean {
        return page.getByText("Sample collection", Page.GetByTextOptions().setExact(true)).isVisible && page.getByRole(
            AriaRole.PARAGRAPH
        ).filter(Locator.FilterOptions().setHasText("Cancelled")).isVisible
    }


    fun isSavedFullSlotMatchingApi(): Boolean {
        val signUpData = SignupDataStore.get()
        val fastingSlotTime = signUpData.fastingSlot?.split(":")

        val savedLocalDate = signUpData.slotDate?.withHour(fastingSlotTime?.first()?.trim()?.toInt() ?: 0)
            ?.withMinute(fastingSlotTime?.last()?.trim()?.toInt() ?: 0)?.withSecond(0)


        val diagnostic = homeData?.diagnostics?.firstOrNull { it.blood_test_appointment_date != null }
        val appointmentDateUTC = diagnostic?.blood_test_appointment_date ?: run {
            return false
        }

        val apiLocalDateTime = DateHelper.utcToLocalDateTime(appointmentDateUTC)

        return savedLocalDate?.equals(apiLocalDateTime) ?: false
    }


    fun clickProfile(): OrdersPage {
        StepHelper.step(CLICK_PROFILE_ICON)
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("profile")).click()
        val orderPage = OrdersPage(page)
        return orderPage
    }


    fun clickAccountProfile(): ProfilePage {
        StepHelper.step(CLICK_ACCOUNT_PROFILE)
        val profilePage = ProfilePage(page)
        profilePage.captureAddressData {
            profileImage.click()
        }
        profilePage.waitForConfirmation()
        return profilePage
    }

    fun clickActionPlan(): ActionPlanPage {
        StepHelper.step(CLICK_ACTION_PLAN)
        actionButtonPlan.click()
        val actionPlan = ActionPlanPage(page)
        return actionPlan
    }
    fun claimYourConsultCard(): HomePage  {
//        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("free-consultation")).first().click()
        page.getByText("Claim your Consult").click()
        page.getByText("1-on-1 consult with our").click()
        page.getByTestId("button-book-consultation").click()
        return HomePage(page)
    }

    fun consultationConfirmationCard(): ProfilePage {
        val profilePage = ProfilePage(page)
        page.getByText("Book a consultationwith our expert30min video call with a longevity expertWhat'").first()
            .click()
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Book a consultation")).click()
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText(Pattern.compile("^with our expert$")))
            .click()
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("30min video call with a")).click()
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What's included")).click()
        page.locator("div")
            .filter(FilterOptions().setHasText(Pattern.compile("^Quick overview of dashboard and results$"))).nth(2)
            .click()
        page.locator("div")
            .filter(FilterOptions().setHasText(Pattern.compile("^Symptoms check-in and mapping results$"))).nth(2)
            .click()
        page.locator("div")
            .filter(FilterOptions().setHasText(Pattern.compile("^State of existing medical conditions \\(if any reported\\)$")))
            .nth(2).click()
        page.locator("div")
            .filter(FilterOptions().setHasText(Pattern.compile("^Highlight any urgent or clinically significant flags$")))
            .nth(2).click()
        page.locator("div")
            .filter(FilterOptions().setHasText(Pattern.compile("^Suggest further medical diagnosis if any required$")))
            .nth(2).click()
        page.locator("div:nth-child(6) > .box-border > .content-stretch").first().click()
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Note: Consultations will not")).click()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Next")).click()
        return profilePage
    }
    fun consulationWithExpertCard(): HomePage {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Consultation with Longevity")).click()
        page.getByText("Personalized consultation").click()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Maybe later")).click()
        return HomePage(page)
    }
}