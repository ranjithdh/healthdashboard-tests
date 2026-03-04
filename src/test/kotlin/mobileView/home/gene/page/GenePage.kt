package mobileView.home.gene.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.utils.ActionPlanUtils.normalizeForUiCompare
import mobileView.diagnostics.TestDetailPage
import mobileView.home.gene.model.GeneDataWrapper
import mobileView.home.gene.model.GeneItem
import mobileView.home.gene.model.GeneResponse
import mobileView.home.gut.model.GutDataWrapper
import mobileView.home.gut.model.GutMetricDetails
import mobileView.home.gut.model.GutResponse
import mobileView.home.gut.util.GutUtility.toKebabCase
import model.healthdata.HealthData
import utils.Normalize.refactorTimeZone
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_GENE_DATA
import utils.report.StepHelper.FETCH_GUT_DATA
import utils.report.StepHelper.FETCH_HEALTH_DATA
import utils.report.StepHelper.VALIDATING_GENE_LIST
import utils.report.StepHelper.logApiResponse
import kotlin.collections.forEach
import kotlin.test.assertEquals

class GenePage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL

    private var gutDataWrapper: GutDataWrapper? = null
    private var gutMetricDetails: GutMetricDetails? = null

    private var geneDataWrapper: GeneDataWrapper? = null

    private var healthData: HealthData? = null


    init {
        monitorTraffic()
    }

    private fun monitorTraffic() {
        captureGutListData()
        captureGeneListData()
        captureBloodData()
    }

    fun captureBloodData() {
        if (healthData === null) {
            StepHelper.step(FETCH_HEALTH_DATA)
            try {
                val timeZone = java.util.TimeZone.getDefault().id

                val apiContext = page.context().request()
                val response = apiContext.get(
                    TestConfig.APIs.HEALTH_DATA,
                    RequestOptions.create()
                        .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                        .setHeader("client_id", TestConfig.CLIENT_ID)
                        .setHeader("user_timezone", refactorTimeZone(timeZone))
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

                val responseObj = json.decodeFromString<HealthData>(responseBody)

                if (responseObj.status == "success") {
                    healthData = responseObj
                    logApiResponse(TestConfig.APIs.HEALTH_DATA, responseObj)
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

    fun captureGeneListData() {
        if (geneDataWrapper === null) {
            StepHelper.step(FETCH_GENE_DATA)
            try {
                val timeZone = java.util.TimeZone.getDefault().id

                val apiContext = page.context().request()
                val response = apiContext.get(
                    TestConfig.APIs.API_GENE,
                    RequestOptions.create()
                        .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                        .setHeader("client_id", TestConfig.CLIENT_ID)
                        .setHeader("user_timezone", refactorTimeZone(timeZone))
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

                val responseObj = json.decodeFromString<GeneResponse>(responseBody)

                if (responseObj.status == "success") {
                    geneDataWrapper = responseObj.data
                    logApiResponse(TestConfig.APIs.API_GENE, responseObj)
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

    fun captureGutListData() {
        if (gutDataWrapper === null) {
            StepHelper.step(FETCH_GUT_DATA)
            try {
                val timeZone = java.util.TimeZone.getDefault().id

                val apiContext = page.context().request()
                val response = apiContext.get(
                    TestConfig.APIs.API_GUT,
                    RequestOptions.create()
                        .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                        .setHeader("client_id", TestConfig.CLIENT_ID)
                        .setHeader("user_timezone", refactorTimeZone(timeZone))
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
                    logApiResponse(TestConfig.APIs.API_GUT, responseObj)
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }


    /**------------Empty View---------------*/
    fun emptyView() {
        val geneList = geneDataWrapper?.gene?.data
        if (geneList?.isEmpty() == true) {
            val dnaHelixImg =
                page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("DNA helix"))

            val geneticInsightsHeading =
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Unlock your Genetic insights"))

            val discoverGenesText =
                page.getByText("Discover how your genes")

            val geneEmptyStateButton =
                page.getByTestId("gene-empty-state-view-test-button")

            listOf(
                dnaHelixImg,
                geneticInsightsHeading,
                discoverGenesText,
                geneEmptyStateButton
            ).forEach { it.waitFor() }

            geneEmptyStateButton.click()

            TestDetailPage(page)
                .waitGeneTabLoad()
                .clickBackButtonToHome()
        }
    }

    /**------------Gene List---------------*/
    fun geneListValidation() {
        StepHelper.step(VALIDATING_GENE_LIST)
        val geneList = geneDataWrapper?.gene?.data
        if (geneList?.isNotEmpty() == true) {
            val genListGroupByName = getGeneDataByGroup(geneList)
            headerValidations(genListGroupByName)
        }
    }

    private fun headerValidations(headerList: Map<String, List<GeneItem>>) {
        headerList.keys.forEach { headerExpected ->
            val geneList = headerList[headerExpected]
            val id = toKebabCase(headerExpected)
            logger.info { "Validating marker id '$id'" }
             val headerUiElement = page.getByTestId("gene-group-header-$id")
            val markerUiElement = page.getByTestId("gene-group-marker-$id")

             val headerTextActual = headerUiElement.innerText()
            val markerTextActual = markerUiElement.innerText()

             headerUiElement.waitFor()
            markerUiElement.waitFor()

             logger.info { "Validating Header: Expected='$headerExpected', Actual='$headerTextActual'" }
                assertEquals(headerExpected.normalizeForUiCompare(), headerTextActual.normalizeForUiCompare())

            val expectedMarkerCount = "${geneList?.size} Markers".uppercase()
            logger.info { "Validating Marker Count for '$headerExpected': Expected='$expectedMarkerCount', Actual='${markerTextActual.uppercase()}'" }
            assertEquals(
                expectedMarkerCount.normalizeForUiCompare(),
                markerTextActual.normalizeForUiCompare()
            )

            logger.info { "Clicking Header to expand: $headerExpected" }
            markerUiElement.click()
            markerListValidations(geneList)

            markerUiElement.click()
        }
    }

    private fun markerListValidations(gutList: List<GeneItem>?) {
        gutList?.forEach { gutMetricList ->
            val expectedName = gutMetricList.metric?.displayName
            val expectedDescription = gutMetricList.displayDescription

            val metricID = gutMetricList.metric?.metricId

            val nameUiElement = page.getByTestId("gene-item-name-$metricID")
            val descriptionUiElement = page.getByTestId("gene-item-description-mobile-$metricID")
            val statusIconUiElement = page.getByTestId("gene-item-status-icon-mobile-$metricID")

            listOf(nameUiElement, descriptionUiElement, statusIconUiElement).forEach { it.waitFor() }

            nameUiElement.scrollIntoViewIfNeeded()

            val actualName = nameUiElement.innerText()
            val actualDescription = descriptionUiElement.innerText()

            logger.info { "Validating Marker [ID: $metricID]: ExpectedName='$expectedName', ActualName='$actualName', ExpectedInference='$expectedDescription', ActualInference='$actualDescription'" }
            assertEquals(expectedName, actualName)
            assertEquals(expectedDescription, actualDescription)
        }
    }

    fun getGeneDataByGroup(gutList: List<GeneItem>): Map<String, List<GeneItem>> {
        val gutListGroupByName =
            gutList?.groupBy {
                it.metric?.groupName
                    ?.takeIf { it.isNotBlank() }
                    ?: "Others"
            }
        return gutListGroupByName ?: emptyMap()
    }

}