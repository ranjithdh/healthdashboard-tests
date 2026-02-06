package healthdata.page

import com.microsoft.playwright.Download
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import config.BasePage
import config.TestConfig
import model.healthdata.HealthData
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_TRACK_RESULT
import utils.report.StepHelper.DOWNLOAD_REPORT
import utils.report.StepHelper.FETCH_HEALTH_DATA
import utils.report.StepHelper.VERIFY_BIOMARKER_DATA
import utils.LogFullApiCall
import java.util.regex.Pattern


class HealthDataPage(page: Page, val healthData: HealthData?=null) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.HEALTH_DATA_URL

    fun waitForPageLoad() {
        StepHelper.step("${StepHelper.NAVIGATE_TO} ${TestConfig.Urls.HEALTH_DATA_URL}")
        logger.info { "Waiting for Health Data page to load" }
        page.waitForURL(TestConfig.Urls.HEALTH_DATA_URL)
    }

    fun clickSystemTab(systemName: String) {
        logger.info { "Clicking system tab: $systemName" }

        val tabSelector = "button:has-text('$systemName')"

        if (!isVisible(tabSelector)) {
            StepHelper.step("Clicking system tab: $systemName")
            byText(systemName).first().click()
        } else {
            StepHelper.step("Clicking system tab: $systemName")
            element(tabSelector).first().click()
        }

        page.waitForTimeout(500.0)
    }

    fun getBiomarkerRow(name: String): Locator? {

        val row = page.locator("div.grid.grid-cols-12")
            .filter(
                Locator.FilterOptions().setHas(
                    page.getByText(name, Page.GetByTextOptions().setExact(true))
                )
            )

        if (row.count() > 0) {
            return row.first()
        }
        return null
    }

    fun scrollToBiomarker(name: String) {
        val row = getBiomarkerRow(name)
        if (row != null) {
            row.scrollIntoViewIfNeeded()
        } else {
            logger.warn { "Biomarker row not found for scrolling: $name" }
        }
    }

    fun isBiomarkerVisible(name: String): Boolean {
        return getBiomarkerRow(name)?.isVisible ?: false
    }

    fun verifyBiomarkerData(
        name: String,
        expectedValue: String,
        expectedStatus: String,
        expectedRange: String
    ): Boolean {
        val row = getBiomarkerRow(name)
        if (row == null) {
            logger.error { "Biomarker '$name' not found on page" }
            return false
        }

        val rowText = row.textContent()
        logger.info { "Row text for '$name': $rowText" }

        val valueMatches = rowText.contains(expectedValue, ignoreCase = true)
        val statusMatches = rowText.contains(expectedStatus, ignoreCase = true)

        val rangeMatches = rowText.contains(expectedRange, ignoreCase = true)

        if (!valueMatches) logger.error { "Value mismatch for $name. Expected: $expectedValue, Found in: $rowText" }
        if (!statusMatches) logger.error { "Status mismatch for $name. Expected: $expectedStatus, Found in: $rowText" }
        if (!rangeMatches) logger.error { "Range mismatch for $name. Expected: $expectedRange, Found in: $rowText" }

        StepHelper.step("$VERIFY_BIOMARKER_DATA $name")
        return valueMatches && statusMatches && rangeMatches
    }


    fun downloadReport(): Download {
        StepHelper.step(DOWNLOAD_REPORT)
        logger.info { "Downloading biomarker report" }
        val download = page.waitForDownload {
            byTestId("download-report-button").click()
        }
        return download
    }

    fun shouldShowEmptyState(): Boolean {
        return page.getByText("Your report is in progress").isVisible &&
                page.getByText("Your blood sample is under analysis at the lab. We will notify you when it’s ready.").isVisible
    }

    private val trackResult = page.getByText("Track test status")

    fun shouldShowTrackResult(): Boolean {
        return trackResult.isVisible
    }

    fun clickTrackResult() {
        StepHelper.step(CLICK_TRACK_RESULT)
        trackResult.click()
    }

    fun getHealthDataResponse(): HealthData? {
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains(TestConfig.APIs.HEALTH_DATA) == true && response.status() == 200
            },
            {
                page.waitForURL(TestConfig.Urls.HOME_PAGE_URL)
            }
        )

        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "getHealthDataResponse API response body is empty" }
        }

        try {
            val responseObj = json.decodeFromString<HealthData>(responseBody)

            if (responseObj.data != null) {
                StepHelper.step(FETCH_HEALTH_DATA)
                LogFullApiCall.logFullApiCall(response)
                return responseObj
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response..${e.message}" }
            return null
        }

        return null
    }


}
