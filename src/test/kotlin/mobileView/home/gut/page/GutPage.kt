package mobileView.home.gut.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.utils.ActionPlanUtils.normalizeForUiCompare
import mobileView.home.gut.model.GutDataWrapper
import mobileView.home.gut.model.GutMetricData
import mobileView.home.gut.model.GutMetricDetails
import mobileView.home.gut.model.GutMetricItem
import mobileView.home.gut.model.GutMetricResponse
import mobileView.home.gut.model.GutResponse
import mobileView.home.gut.model.MetricDetail
import mobileView.home.gut.model.ParsedSection
import mobileView.home.gut.model.ParsedSubSection
import mobileView.home.gut.util.GutUtility.toKebabCase
import mobileView.home.gut.util.TestMappingLoader
import utils.Normalize.refactorTimeZone
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_GUT_DATA
import utils.report.StepHelper.FETCH_GUT_DETAILS
import utils.report.StepHelper.VALIDATING_CONNECTED_BIOMARKERS_TAB
import utils.report.StepHelper.VALIDATING_GUT_DETAILS
import utils.report.StepHelper.VALIDATING_GUT_LIST
import utils.report.StepHelper.VALIDATING_WHAT_IT_MEANS_TAB
import utils.report.StepHelper.logApiResponse
import kotlin.test.assertEquals

class GutPage(page: Page) : BasePage(page) {

    private val mappings = TestMappingLoader.loadGeneGutMappings() //data

    private val searchView = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in Gut"))

    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL

    private var gutDataWrapper: GutDataWrapper? = null
    private var gutMetricDetails: GutMetricDetails? = null

    fun waitForConfirmation(): GutPage {
        searchView.waitFor()
        return this
    }

    init {
        monitorTraffic()
    }

    private fun monitorTraffic() {
        captureGutListData()
    }

    fun captureGutListData() {
        if (gutDataWrapper == null) {
            StepHelper.step(FETCH_GUT_DATA)
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
                    logApiResponse(TestConfig.APIs.API_GUT, responseObj)
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

    /**------------Gut List---------------*/

    fun gutListValidation() {
        StepHelper.step(VALIDATING_GUT_LIST)
        val gutList = gutDataWrapper?.gut?.data
        if (gutList?.isNotEmpty() == true) {
            val gutListGroupByName = getGutDataByGroup()
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
            assertEquals(
                "${gutList?.size} Markers".normalizeForUiCompare().uppercase(),
                markerTextActual.normalizeForUiCompare()
            )

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

    fun getGutDataByGroup(): Map<String, List<GutMetricData>> {
        val gutList = gutDataWrapper?.gut?.data
        val gutListGroupByName =
            gutList?.groupBy {
                it.metric?.group_name
                    ?.takeIf { it.isNotBlank() }
                    ?: "Others"
            }
        return gutListGroupByName ?: emptyMap()
    }


    /**------------Gut Details---------------*/
    fun gutDetailsValidation() {
        StepHelper.step(VALIDATING_GUT_DETAILS)
        val gutList = gutDataWrapper?.gut?.data
        if (gutList?.isNotEmpty() == true) {
            val gutListGroupByName = getGutDataByGroup()
            gutListGroupByName.forEach { (groupName, metricsList) ->
                val metricIds = metricsList.map { it.metric?.metric_id!! }
                captureGutDetails(metricIds, groupName) //API call

                val id = toKebabCase(groupName)
                val headerUiElement = page.getByTestId("gut-group-header-$id")

                headerUiElement.waitFor()
                headerUiElement.click()

                val summaryMetricsList = gutMetricDetails?.metrics //TODO need to change as list

                val gutMetricList = summaryMetricsList?.get(0)
                val metricID = gutMetricList?.metric?.metricId
                val nameUiElement = page.getByTestId("gut-item-name-$metricID")

                nameUiElement.waitFor()
                nameUiElement.scrollIntoViewIfNeeded()
                nameUiElement.click()

                detailsValidation(groupName, summaryMetricsList)


                page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Back")).click()


                /*   summaryMetricsList?.forEach { gutMetricList ->
                       val metricID = gutMetricList.metric?.metricId
                       val nameUiElement = page.getByTestId("gut-item-name-$metricID")
                       nameUiElement.waitFor()
                       nameUiElement.scrollIntoViewIfNeeded()
                       nameUiElement.click()

                       detailsValidation(groupName, gutMetricList)
                       page.waitForTimeout(2000.0)

                       page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Back")).click()
                   }*/
            }
        }
    }

    private fun detailsValidation(groupName: String, summaryMetricsList: List<GutMetricItem>?) {
//        detailsHeaderValidations(groupName)

        //      detailsMetricsValidations(summaryMetricsList)

        tabChecking(summaryMetricsList)

    }

    private fun tabChecking(summaryMetricsList: List<GutMetricItem>?) {
        val isWhyTab = shouldShowWhyTab(summaryMetricsList)
        val isConnectedTab = shouldShowConnectedTab(summaryMetricsList)
        if (isWhyTab) {
            StepHelper.step(VALIDATING_WHAT_IT_MEANS_TAB)
            val whatItMeansTab = page.getByTestId("what-it-means-tab")
            whatItMeansTab.waitFor()
            whatItMeansTab.click()
            checkWhatItMean(summaryMetricsList)
        }
        if (isConnectedTab) {
            StepHelper.step(VALIDATING_CONNECTED_BIOMARKERS_TAB)
            val connectedTab = page.getByTestId("connected-biomarkers-tab")
            connectedTab.waitFor()
            connectedTab.click()
            checkConnectedBiomarkers()
        }
    }

    private fun checkConnectedBiomarkers() {

    }

    private fun checkWhatItMean(summaryMetricsList: List<GutMetricItem>?) {
        whatItMeanTitleCheck(summaryMetricsList)

        //Todo need know the description


        //other factor
        whatItMeanOtherFactor(summaryMetricsList)

        whatItMeanSections(summaryMetricsList)

        whatItMeanBottomLine(summaryMetricsList)
    }

    private fun whatItMeanBottomLine(summaryMetricsList: List<GutMetricItem>?) {
        val gutWithDetails = summaryMetricsList
            ?.firstOrNull { it.details?.isNotEmpty() == true }

        val bottomLine = gutWithDetails
            ?.details
            ?.firstOrNull { it.category == "bottom_line" }

        if (bottomLine?.content.isNullOrEmpty()) return

        val bottomLineTitle = page.getByTestId("bottom-line-title")
        bottomLineTitle.waitFor()
        assertEquals("Bottom line", bottomLineTitle.innerText())

        val bottomLineText = page.getByTestId("bottom-line-text")
        bottomLineText.waitFor()

// If markdown is rendered as plain text in UI
        assertEquals(
            bottomLine?.content?.normalizeForUiCompare(),
            bottomLineText.innerText().normalizeForUiCompare()
        )
    }

    private fun whatItMeanSections(summaryMetricsList: List<GutMetricItem>?) {
        val details = summaryMetricsList?.firstOrNull()?.details

        val sections = parseSections(details)



        if (sections.isNotEmpty()) {
            sections.forEachIndexed { sectionPosition, section ->
                when (sectionPosition) {
                    0 -> {
                        val titleExcepted = section.mainTitle
                        val contextExcepted = section.plainContent
                        val titleActual = page.getByTestId("section-title-$sectionPosition")
                        val contentActual = page.getByTestId("section-content-$sectionPosition")

                        listOf(titleActual, contentActual).forEach { it.waitFor() }
                        contentActual.scrollIntoViewIfNeeded()

                        assertEquals(
                            titleExcepted?.normalizeForUiCompare(),
                            titleActual.innerText().normalizeForUiCompare()
                        )
                        assertEquals(
                            contextExcepted?.normalizeForUiCompare(),
                            contentActual.innerText().normalizeForUiCompare()
                        )
                    }

                    else -> {
                        if (!section.mainTitle.isNullOrBlank() || !section.plainContent.isNullOrBlank() || section.subSections?.isNotEmpty() == true) {

                            val titleExpected = section.mainTitle
                            val titleActual = page.getByTestId("section-title-$sectionPosition")
                            titleActual.waitFor()
                            titleActual.scrollIntoViewIfNeeded()

                            assertEquals(
                                titleExpected?.normalizeForUiCompare(),
                                titleActual.innerText().normalizeForUiCompare()
                            )

                            val subSectionsList = section.subSections

                            subSectionsList?.forEachIndexed { index, subSection ->
                                val subsectionTitle = page.getByTestId("subsection-title-$sectionPosition-$index")
                                val subsectionDescription =
                                    page.getByTestId("subsection-description-$sectionPosition-$index")

                                listOf(subsectionTitle, subsectionDescription).forEach { it.waitFor() }
                                subsectionDescription.scrollIntoViewIfNeeded()

                                assertEquals(
                                    subSection.title?.normalizeForUiCompare(),
                                    subsectionTitle.innerText().normalizeForUiCompare()
                                )
                                assertEquals(
                                    subSection.description?.normalizeForUiCompare(),
                                    subsectionDescription.innerText().normalizeForUiCompare()
                                )
                            }
                        }
                    }
                }
            }

        }

        logger.info {
            "List of sections:\n${sections?.joinToString("\n") ?: "No sections found"}"
        }
    }

    private fun whatItMeanOtherFactor(summaryMetricsList: List<GutMetricItem>?) {
        val otherFactors = summaryMetricsList
            ?.firstOrNull { it.details?.any { d -> d.category == "factors" } == true }
            ?.details
            ?.firstOrNull { it.category == "factors" }

        if (otherFactors?.content.isNullOrEmpty()) return

        val factorLines = otherFactors?.content
            ?.lines()
            ?.filter { line ->
                val trimmed = line.trim()
                trimmed.startsWith("*") || trimmed.startsWith("##")
            }

        val factorList = factorLines?.map { line ->
            line
                .replace(Regex("^##\\s*"), "")          // remove heading ##
                .replace(Regex("^\\*\\s*"), "")         // remove *
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // remove bold **
        }

        logger.info {
            "List of factors:\n${factorList?.joinToString("\n") ?: "No factors found"}"
        }


        val otherFactorTitle = page.getByTestId("other-factors-title")
        otherFactorTitle.waitFor()
        assertEquals(factorList?.get(0), otherFactorTitle.innerText().normalizeForUiCompare())

        factorList?.drop(1)?.forEachIndexed { index, factor ->
            val factorUiElement = page.getByTestId("other-factor-text-${index}")
            factorUiElement.waitFor()
            factorUiElement.scrollIntoViewIfNeeded()
            assertEquals(factor.normalizeForUiCompare(), factorUiElement.innerText().normalizeForUiCompare())
        }
    }

    private fun whatItMeanTitleCheck(summaryMetricsList: List<GutMetricItem>?) {
        val groupName = summaryMetricsList?.get(0)?.metric?.groupName
        val titleUiElement = page.getByTestId("why-tab-title")
        val actualTitleText = titleUiElement.innerText()

        titleUiElement.waitFor()
        assertEquals(actualTitleText.normalizeForUiCompare(), groupName?.normalizeForUiCompare())
    }

    private fun detailsMetricsValidations(summaryMetricsList: List<GutMetricItem>?) {
        summaryMetricsList?.forEachIndexed { index, metricsList ->
            val metricName = page.getByTestId("metric-name-$index")

            val metricInference = page.getByTestId("metric-inference-$index")

            val metricDescription = page.getByTestId("metric-description-$index")


            listOf(metricName, metricInference, metricDescription).forEach { it.waitFor() }

            val expectedMetricName = metricsList.metric?.displayName
            val expectedInference = metricsList.summary?.inference
            val expectedDescription = metricsList.summary?.displayDescription

            assertEquals(expectedMetricName?.normalizeForUiCompare(), metricName.innerText()?.normalizeForUiCompare())

            assertEquals(
                expectedInference?.normalizeForUiCompare(),
                metricInference.innerText()?.normalizeForUiCompare()
            )
            assertEquals(
                expectedDescription?.normalizeForUiCompare(),
                metricDescription.innerText()?.normalizeForUiCompare()
            )

        }
    }

    private fun detailsHeaderValidations(groupName: String) {
        val parameterTitle = page.getByTestId("parameter-title")
        val actualTitleText = parameterTitle.innerText()
        //TODO need to add description

        parameterTitle.waitFor()
        assertEquals(actualTitleText.normalizeForUiCompare(), groupName.normalizeForUiCompare())
    }

    fun captureGutDetails(
        metricIds: List<String>,
        groupName: String
    ) {
        StepHelper.step(FETCH_GUT_DETAILS)
        try {
            val timeZone = java.util.TimeZone.getDefault().id

            val encodedGroupName = java.net.URLEncoder.encode(groupName, "UTF-8")

            val metricParams = metricIds.joinToString("&") { "metric_id[]=$it" }


            val apiUrl =
                "${TestConfig.APIs.API_GUT_DETAILS}?$metricParams&group_name=$encodedGroupName"

            val apiContext = page.context().request()
            val response = apiContext.get(
                apiUrl,
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


            val responseObj = json.decodeFromString<GutMetricResponse>(responseBody)

            if (responseObj.status == "success") {
                gutMetricDetails = responseObj.data
                logApiResponse(apiUrl, responseObj)
            }

        } catch (e: Exception) {
            logger.error { "Failed to fetch account information: ${e.message}" }
        }
    }

    fun shouldShowWhyTab(metrics: List<GutMetricItem>?): Boolean {
        return metrics
            ?.firstOrNull { !it.details.isNullOrEmpty() }
            ?.details
            ?.isNotEmpty() == true
    }

    fun shouldShowConnectedTab(metrics: List<GutMetricItem>?): Boolean {
        return metrics
            ?.flatMap { it.correlations ?: emptyList() }
            ?.isNotEmpty() == true
    }

    fun parseSections(details: List<MetricDetail>?): List<ParsedSection> {
        if (details.isNullOrEmpty()) return emptyList()

        val sections = details
            .filter { it.category?.startsWith("section") == true }
            .sortedBy {
                it.category?.replace("section", "")?.toIntOrNull() ?: 0
            }

        return sections.map { section ->
            val content = section.content.orEmpty()

            // 1. Extract ## Main Title
            val mainTitleRegex = Regex("^##\\s*(.*)", RegexOption.MULTILINE)
            val mainTitleMatch = mainTitleRegex.find(content)
            val mainTitle = mainTitleMatch?.groupValues?.get(1)?.trim()

            // 2. Remove main title from content
            val contentWithoutMain = if (mainTitle != null) {
                content.replaceFirst(Regex("^##.*\\n?"), "").trim()
            } else {
                content.trim()
            }

            // 3. Check for ### subheadings
            val hasSubHeadings = Regex("^###", RegexOption.MULTILINE)
                .containsMatchIn(contentWithoutMain)

            if (hasSubHeadings) {
                // 4. Split into subsections
                val subSections = contentWithoutMain
                    .split(Regex("^###", RegexOption.MULTILINE))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { sub ->
                        val parts = sub.split(":", limit = 2)
                        val title = parts.getOrNull(0)?.trim()
                        val description = parts.getOrNull(1)?.trim()

                        ParsedSubSection(
                            title = title,
                            description = description
                        )
                    }

                ParsedSection(
                    mainTitle = mainTitle,
                    subSections = subSections,
                    plainContent = null
                )
            } else {
                // No ### headings → plain paragraph
                ParsedSection(
                    mainTitle = mainTitle,
                    subSections = null,
                    plainContent = contentWithoutMain
                )
            }
        }
    }
}