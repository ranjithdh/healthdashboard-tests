package mobileView.orders

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.LabTestDateHelper.getDashBoardReadyToViewDate
import mobileView.LabTestDateHelper.getPhlebotomistAssignedDate
import mobileView.LabTestDateHelper.getSampleCollectionDate
import model.orders.Orders
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_ORDERS_TAB
import utils.report.StepHelper.CLICK_ORDER_STATUS


class OrdersPage(page: Page) : BasePage(page) {

    override val pageUrl = "/onboard"


    private var appointmentDate: String? = null

    fun getAppointmentDate(): String? {
        return appointmentDate
    }


    fun waitForProfilePageToLoad(): OrdersPage {
        page.waitForURL(TestConfig.Urls.PROFILE_URL)
        return this
    }


    fun clickOrdersTab(): OrdersPage {
        StepHelper.step(CLICK_ORDERS_TAB)
        page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains("https://api.stg.dh.deepholistics.com/v4/human-token/market-place/orders/list") == true && response.status() == 200
            },
            {
                page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Orders")).click()
            }
        ).let { response ->
            parseOrdersResponse(response)
        }
        return this
    }

    private fun parseOrdersResponse(response: Response) {
        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "OrdersList response body is empty" }
            return
        }

        logger.info { "OrdersList response...$responseBody" }

        try {
            val responseObj = json.decodeFromString<Orders>(responseBody)
            logger.info { "OrdersList data...$responseObj" }

            if (responseObj.data != null) {
                val diagnosticOrder = responseObj.data.orders?.firstOrNull()?.appointment_date

                logger.info { "Extracted diagnosticOrder from API: $diagnosticOrder" }

                if (diagnosticOrder != null) {
                    appointmentDate = diagnosticOrder
                    logger.info { "[${this.hashCode()}] Successfully saved appointmentDate: $appointmentDate" }
                } else {
                    logger.warn { "[${this.hashCode()}] diagnosticOrder is null in API response data" }
                }
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response..${e.message}" }
        }
    }


    fun waitForAppointmentDateToLoad() {
        page.getByText("Dashboard ready to view").waitFor()
    }

    fun waitForLongevityPanelToLOad(): Boolean {
        return page.getByText("Longevity Panel").isVisible
    }

    fun clickOrderStatus() {
        StepHelper.step(CLICK_ORDER_STATUS)
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("ORDER STATUS")).click()
    }

    fun isBloodTestCardVisible(): Boolean {
        return page.getByText("Dashboard ready to view").isVisible
    }

    fun waitForBloodTestCardToLoad(): OrdersPage {
        page.getByText("Dashboard ready to view").waitFor()
        return this
    }

    fun isPhlebotomistAssignedTitleVisible(): Boolean {
        return page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("Phlebotomist assigned")).isVisible
    }

    fun isPhlebotomistAssignedDateVisible(): Boolean {
        waitForAppointmentDateToLoad()
        logger.info { "[${this.hashCode()}] Checking phlebotomist assigned date visibility. Current appointmentDate: ${getAppointmentDate()}" }
        val fullText = "Expected: ${getPhlebotomistAssignedDate(getAppointmentDate())}"
        return byRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(fullText)).isVisible
    }

    fun isSampleCollectionTitleVisible(): Boolean {
        return page.getByText("Sample collection", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isSampleCollectionDateVisible(): Boolean {
        waitForAppointmentDateToLoad()
        val sampleCollectionDate = getSampleCollectionDate(getAppointmentDate())
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
        waitForAppointmentDateToLoad()
        val readyToViewDate = "Expected: ${getDashBoardReadyToViewDate(getAppointmentDate())}"
        return page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(readyToViewDate)).isVisible
    }

    fun isTBloodTestCancelled(): Boolean {
        return page.getByText("Sample collection", Page.GetByTextOptions().setExact(true)).isVisible && page.getByRole(
            AriaRole.PARAGRAPH
        ).filter(Locator.FilterOptions().setHasText("Cancelled")).isVisible
    }


}


