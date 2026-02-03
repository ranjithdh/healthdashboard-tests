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


class HealthDataPage(page: Page, val healthData: HealthData?=null) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.BIOMARKERS

    fun waitForPageLoad() {
        logger.info { "Waiting for Health Data page to load" }
//        waitForVisible("h1:has-text('Health data')")
//        waitForVisible(".grid.grid-cols-12", TestConfig.Timeouts.ELEMENT_TIMEOUT)

        page.waitForURL(TestConfig.Urls.BIOMARKERS)

//        getHealthDataResponse()
    }


    fun clickSystemTab(systemName: String) {
        logger.info { "Clicking system tab: $systemName" }

        val tabSelector = "button:has-text('$systemName')"

        if (!isVisible(tabSelector)) {
            byText(systemName).first().click()
        } else {
            element(tabSelector).first().click()
        }

        page.waitForTimeout(500.0)
    }


    private fun getBiomarkerRow(name: String): Locator? {

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

        return valueMatches && statusMatches && rangeMatches
    }


    fun downloadReport(): Download {
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
        trackResult.click()
    }


}
