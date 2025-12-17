package home.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import model.home.HomeData
import model.home.HomeDataResponse
import utils.DateHelper
import utils.json.json
import utils.logger.logger
import java.time.format.DateTimeFormatter
import java.util.*


class HomePage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.HOME_PAGE_URL

    private var homeData: HomeData? = HomeData()

    fun waitForMobileHomePageConfirmation(): HomePage {
        logger.info("Waiting for home page confirmation...")
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
                Page.WaitForResponseOptions().setTimeout(TestConfig.Browser.TIMEOUT * 2)
            },
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
        val fullText = "Expected: ${getPhlebotomistAssignedDate()}"
        return byRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText(fullText))
            .isVisible
    }

    fun isSampleCollectionTitleVisible(): Boolean {
        return page.getByText("Sample collection", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isSampleCollectionDateVisible(): Boolean {
        val sampleCollectionDate = getSampleCollectionDate()
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
        val readyToViewDate = "Expected: ${getDashBoardReadyToViewDate()}"
        return page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(readyToViewDate))
            .isVisible
    }

    fun isTBloodTestCancelled(): Boolean {
        return page.getByText("Sample collection", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("Cancelled")).isVisible
    }


    fun getPhlebotomistAssignedDate(): String {
        val diagnostic = homeData?.diagnostics?.firstOrNull { it.blood_test_appointment_date != null }
        val appointmentDate = diagnostic?.blood_test_appointment_date
        val localDate = DateHelper.utcToLocalDateTime(appointmentDate)

        val assignedDate = localDate.minusDays(1).withHour(21).withMinute(0).withSecond(0)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM, hh:mm a", Locale.ENGLISH)
        val formattedDateTime = dateTimeFormatter.format(assignedDate)

        logger.info {
            "formattedDateTime.....$formattedDateTime"
        }

        return formattedDateTime
    }


    fun getSampleCollectionDate(): String {
        val diagnostic = homeData?.diagnostics?.firstOrNull { it.blood_test_appointment_date != null }
        val appointmentDate = diagnostic?.blood_test_appointment_date
        val localDate = DateHelper.utcToLocalDateTime(appointmentDate)

        val endTime = localDate.plusMinutes(30)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM, hh:mm", Locale.ENGLISH)
        val endTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

        val formattedDateTime = dateTimeFormatter.format(localDate)
        val formattedEndTime = endTimeFormatter.format(endTime)

        logger.info {
            "getLabProcessingDate.....${formattedDateTime.plus("-").plus(formattedEndTime)}"
        }

        return formattedDateTime.plus(" - ").plus(formattedEndTime)
    }


    fun getDashBoardReadyToViewDate(): String {
        val diagnostic = homeData?.diagnostics?.firstOrNull { it.blood_test_appointment_date != null }
        val appointmentDate = diagnostic?.blood_test_appointment_date
        val localDate = DateHelper.utcToLocalDateTime(appointmentDate)

        val assignedDate = localDate.plusDays(2)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM, hh:mm a", Locale.ENGLISH)
        val formattedDateTime = dateTimeFormatter.format(assignedDate)

        logger.info {
            "getDashBoardReadyToViewDate.....${formattedDateTime}"
        }
        return formattedDateTime
    }


    fun isLabTestVisible() {
        return page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Home")).waitFor()
    }

    fun clickHomeMenu(): HomePage {
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Home")).click()

        logger.info("" +
                "page url.........${page.url()}")
        return this
    }

}