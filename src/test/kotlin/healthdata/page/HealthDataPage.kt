package healthdata.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class HealthDataPage(page: Page) : BasePage(page) {

    override val pageUrl = "${TestConfig.Urls.BASE_URL}health-data"

    fun waitForPageLoad() {
        logger.info { "Waiting for Health Data page to load" }
        waitForVisible("h1:has-text('Health data')")
        // Wait for at least one biomarker row to appear
        waitForVisible(".grid.grid-cols-12", TestConfig.Timeouts.ELEMENT_TIMEOUT)
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


    private fun getBiomarkerRow(name: String): com.microsoft.playwright.Locator? {

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

        // Range often contains "to" or symbols, check carefully
        // Standardize range format for comparison if needed
        val rangeMatches = rowText.contains(expectedRange, ignoreCase = true)

        if (!valueMatches) logger.error { "Value mismatch for $name. Expected: $expectedValue, Found in: $rowText" }
        if (!statusMatches) logger.error { "Status mismatch for $name. Expected: $expectedStatus, Found in: $rowText" }
        if (!rangeMatches) logger.error { "Range mismatch for $name. Expected: $expectedRange, Found in: $rowText" }

        return valueMatches && statusMatches && rangeMatches
    }
}
