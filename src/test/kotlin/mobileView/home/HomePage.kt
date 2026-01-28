package mobileView.home

import com.microsoft.playwright.Locator
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
import mobileView.orders.OrdersPage
import model.home.HomeData
import model.home.HomeDataResponse
import profile.page.ProfilePage
import utils.DateHelper
import utils.SignupDataStore
import utils.logger.logger

class HomePage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.HOME_PAGE_URL

    val profileImage: Locator = page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("profile"))

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
                    ?.contains("https://api.stg.dh.deepholistics.com/v4/human-token/market-place/home") == true && response.status() == 200
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
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("profile")).click()
        val orderPage = OrdersPage(page)
        return orderPage
    }


    fun clickAccountProfile(): ProfilePage {
        val profilePage = ProfilePage(page)
        profilePage.captureAddressData {
            profileImage.click()
        }
        profilePage.waitForConfirmation()
        return profilePage
    }



}