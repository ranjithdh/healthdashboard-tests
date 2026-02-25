package mobileView.home.gut.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.utils.ActionPlanUtils.normalizeForUiCompare
import mobileView.home.gut.model.GutDataWrapper
import mobileView.home.gut.model.GutMetricData
import mobileView.home.gut.model.GutResponse
import mobileView.home.gut.util.GutUtility.toKebabCase
import mobileView.home.gut.util.TestMappingLoader
import utils.json.json
import utils.logger.logger
import kotlin.test.assertEquals

class GutPage(page: Page) : BasePage(page) {

    private val mappings = TestMappingLoader.loadGeneGutMappings() //data

    private val searchView = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in Gut"))

    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL

    private var gutDataWrapper: GutDataWrapper? = null

    fun waitForConfirmation(): GutPage {
        searchView.waitFor()
        return this
    }

    init {
        monitorTraffic()
    }

    private fun monitorTraffic() {
        captureGutData()
    }

    fun captureGutData() {
        if (gutDataWrapper == null) {
            try {
                val response = page.waitForResponse(
                    { response: Response? ->
                        response?.url()?.contains(TestConfig.APIs.API_GUT) == true &&
                                response.status() == 200 &&
                                response.request().method() == "GET"
                    }, { }
                )

                if (response.status() != 200) {
                    logger.error { "API returned error status: ${response.status()}" }
                    return
                }

                val responseBody = response.text()
                if (responseBody.isNullOrBlank()) {
                    logger.error { "API response body is empty" }
                    return
                }

                val responseObj = json.decodeFromString<GutResponse>(responseBody)

                if (responseObj.status == "success") {
                    gutDataWrapper = responseObj.data
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

    fun gutListValidation() {
        val gutList = gutDataWrapper?.gut?.data
        if (gutList?.isNotEmpty() == true) {
            val gutListGroupByName =
                gutList.groupBy {
                    it.metric?.group_name
                        ?.takeIf { it.isNotBlank() }
                        ?: "Others"
                }

            headerValidations(gutListGroupByName)
        }
    }

    private fun headerValidations(headerList: Map<String, List<GutMetricData>>) {
        headerList.keys.forEach { headerExpected ->
            val gutList = headerList[headerExpected]
            val id = toKebabCase(headerExpected)
            val headerUiElement = page.getByTestId("gut-group-header-$id")
            val markerUiElement = page.getByTestId("gut-group-markers-$id")

            val headerTextActual = headerUiElement.innerText()
            val markerTextActual = markerUiElement.innerText()

            headerUiElement.waitFor()
            markerUiElement.waitFor()
            assertEquals(headerExpected.normalizeForUiCompare(), headerTextActual.normalizeForUiCompare())
            assertEquals("${gutList?.size} Markers".normalizeForUiCompare().uppercase(), markerTextActual.normalizeForUiCompare())

            headerUiElement.click()
            markerListValidations(gutList)

        }


    }

    private fun markerListValidations(gutList: List<GutMetricData>?) {
        gutList?.forEach { gutMetricList ->
            val expectedName = gutMetricList.metric?.display_name
            val expectedInference = gutMetricList.inference

            val metricID = gutMetricList.metric?.metric_id

            val nameUiElement = page.getByTestId("gut-item-name-$metricID")
            val inferenceUiElement = page.getByTestId("gut-item-status-mobile-$metricID")

            listOf(nameUiElement, inferenceUiElement).forEach { it.waitFor() }

            nameUiElement.scrollIntoViewIfNeeded()

            val actualName = nameUiElement.innerText()
            val actualInference = inferenceUiElement.innerText()

            assertEquals(expectedName, actualName)
            assertEquals(expectedInference, actualInference)

        }

    }


}