package mobileView.home.gut.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.utils.ActionPlanUtils.normalizeForUiCompare
import mobileView.home.gut.model.*
import mobileView.home.gut.util.GutUtility.gutSourceType
import mobileView.home.gut.util.GutUtility.toKebabCase
import mobileView.home.gut.util.RiskLevel
import mobileView.home.gut.util.TestMappingLoader
import model.healthdata.HealthData
import utils.Normalize.refactorTimeZone
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_GENE_DATA
import utils.report.StepHelper.FETCH_GUT_DATA
import utils.report.StepHelper.FETCH_GUT_DETAILS
import utils.report.StepHelper.FETCH_HEALTH_DATA
import utils.report.StepHelper.VALIDATING_CONNECTED_BIOMARKERS_TAB
import utils.report.StepHelper.VALIDATING_GUT_DETAILS
import utils.report.StepHelper.VALIDATING_GUT_LIST
import utils.report.StepHelper.VALIDATING_WHAT_IT_MEANS_TAB
import utils.report.StepHelper.logApiResponse
import kotlin.test.assertEquals

class GutPage(page: Page) : BasePage(page) {

    private val geneGutMappings = TestMappingLoader.loadGeneGutMappings() //data
    private val bloodGutCorrleations = TestMappingLoader.loadBloodGutCorrelationsMappings() //data

    private val searchView = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in Gut"))

    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL

    private var gutDataWrapper: GutDataWrapper? = null
    private var gutMetricDetails: GutMetricDetails? = null

    private var geneDataWrapper: GeneDataWrapper? = null

    private var healthData: HealthData? = null


    fun waitForConfirmation(): GutPage {
        searchView.waitFor()
        return this
    }

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


    /**------------Gut List---------------*/

    fun gutListValidation() {
        StepHelper.step(VALIDATING_GUT_LIST)
        val gutList = gutDataWrapper?.gut?.data
        if (gutList?.isNotEmpty() == true) {
            val gutListGroupByName = getGutDataByGroup(gutList)
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

            logger.info { "Validating Header: Expected='$headerExpected', Actual='$headerTextActual'" }
            assertEquals(headerExpected.normalizeForUiCompare(), headerTextActual.normalizeForUiCompare())

            val expectedMarkerCount = "${gutList?.size} Markers".uppercase()
            logger.info { "Validating Marker Count for '$headerExpected': Expected='$expectedMarkerCount', Actual='${markerTextActual.uppercase()}'" }
            assertEquals(
                expectedMarkerCount.normalizeForUiCompare(),
                markerTextActual.normalizeForUiCompare()
            )

            logger.info { "Clicking Header to expand: $headerExpected" }
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

            logger.info { "Validating Marker [ID: $metricID]: ExpectedName='$expectedName', ActualName='$actualName', ExpectedInference='$expectedInference', ActualInference='$actualInference'" }
            assertEquals(expectedName, actualName)
            assertEquals(expectedInference, actualInference)
        }
    }

    fun getGutDataByGroup(gutList: List<GutMetricData>): Map<String, List<GutMetricData>> {
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
            val gutListGroupByName = getGutDataByGroup(gutList)
            gutListGroupByName.forEach { (groupName, metricsList) ->
                val metricIds = metricsList.map { it.metric?.metric_id!! }
                captureGutDetails(metricIds, groupName) //API call

                val id = toKebabCase(groupName)
                val headerUiElement = page.getByTestId("gut-group-header-$id")

                logger.info { "Expanding group for details: $groupName" }
                headerUiElement.waitFor()
                headerUiElement.click()

                val summaryMetricsList = gutMetricDetails?.metrics //TODO need to change as list

                val gutMetricList = summaryMetricsList?.get(0)
                val metricID = gutMetricList?.metric?.metricId
                val nameUiElement = page.getByTestId("gut-item-name-$metricID")

                logger.info { "Navigating to details page for MetricID: $metricID" }
                nameUiElement.waitFor()
                nameUiElement.scrollIntoViewIfNeeded()
                nameUiElement.click()

                detailsValidation(groupName, summaryMetricsList)

                page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Back")).click()
            }
        }
    }

    private fun detailsValidation(groupName: String, summaryMetricsList: List<GutMetricItem>?) {
        detailsHeaderValidations(groupName, summaryMetricsList)

        detailsMetricsValidations(summaryMetricsList)

        tabChecking(summaryMetricsList)

        whatItMeanBottomLine(summaryMetricsList)
    }

    private fun tabChecking(summaryMetricsList: List<GutMetricItem>?) {
        val isWhyTab = shouldShowWhyTab(summaryMetricsList)
        val isConnectedTab = shouldShowConnectedTab(summaryMetricsList)
        if (isWhyTab) {
            StepHelper.step(VALIDATING_WHAT_IT_MEANS_TAB)
            logger.info { "Checking 'What it means' tab" }
            val whatItMeansTab = page.getByTestId("what-it-means-tab")
            whatItMeansTab.waitFor()
            whatItMeansTab.click()
            checkWhatItMean(summaryMetricsList)
        }
        if (isConnectedTab) {
            StepHelper.step(VALIDATING_CONNECTED_BIOMARKERS_TAB)
            logger.info { "Checking 'Connected Biomarkers' tab" }
            val connectedTab = page.getByTestId("connected-biomarkers-tab")
            connectedTab.waitFor()
            connectedTab.click()
            checkConnectedBiomarkers(summaryMetricsList)
        }
    }

    private fun checkConnectedBiomarkers(summaryMetricsList: List<GutMetricItem>?) {
        val allCorrelations: List<MetricCorrelation>? =
            summaryMetricsList
                ?.flatMap { it.correlations.orEmpty() }
                ?.filter { !it.description.isNullOrBlank() || !it.sourceInference.isNullOrBlank() }
                ?.associateBy { it.sourceMetricId }   // dedupe by source_metric_id
                ?.values
                ?.toList()

        val subText =
            "These biomarkers are influenced by your gut parameter status and can help monitor your digestive health."
        val title = "Connected Biomarkers"

        val titleUiElement = page.getByTestId("connected-biomarkers-title")
        titleUiElement.waitFor()
        val actualTitle = titleUiElement.innerText()
        logger.info { "Validating Connected Biomarkers Title: Expected='$title', Actual='$actualTitle'" }
        assertEquals(title, actualTitle)

        val subTexUiElement = page.getByTestId("connected-biomarkers-description")
        subTexUiElement.waitFor()
        val actualSubText = subTexUiElement.innerText()
        logger.info { "Validating Connected Biomarkers Subtext: Expected='$subText', Actual='$actualSubText'" }
        assertEquals(subText, actualSubText)


        allCorrelations?.forEachIndexed { index, correlations ->
            val biomarkerName = correlations.sourceMetricName
            logger.info { "Validating Biomarker [$index]: $biomarkerName" }

            val nameUiElement = page.getByTestId("biomarker-name-$index")
            val typeUiElement = page.getByTestId("biomarker-type-$index")


            listOf(nameUiElement, typeUiElement).forEach { it.waitFor() }

            val actualName = nameUiElement.innerText()
            val actualType = typeUiElement.innerText()
            val expectedType = gutSourceType(correlations.sourceType)

            logger.info { "Validating Name and Type for [$index]: ExpectedName='$biomarkerName', ActualName='$actualName', ExpectedType='$expectedType', ActualType='$actualType'" }
            assertEquals(biomarkerName, actualName)
            assertEquals(expectedType, actualType)

            nameUiElement.scrollIntoViewIfNeeded()

            if (!correlations.sourceInference.isNullOrBlank()) {
                val inferenceUiElement = if (correlations.sourceType == "gene") {
                    page.getByTestId("biomarker-gene-inference-$index")
                } else {
                    page.getByTestId("biomarker-inference-$index")
                }

                inferenceUiElement.waitFor()
                val actualInference = inferenceUiElement.innerText()
                logger.info { "Validating Inference for [$index]: Expected='${correlations.sourceInference}', Actual='$actualInference'" }
                assertEquals(correlations.sourceInference, actualInference)
            }

            if (!correlations.description.isNullOrBlank()) {
                val isGeneEmpty = geneDataWrapper?.gene?.data
                val bloodList = healthData?.data?.blood?.data

                val targetMetricId = correlations.targetMetricId

                if (correlations.sourceType == "blood") {
                    if (bloodList?.isNotEmpty() == true) {
                        val gutCorrleations = bloodGutCorrleations.filter { it.gutMetricIds == targetMetricId }

                        if (gutCorrleations.isNotEmpty()) {
                            val bloodMetricId = gutCorrleations[0].bloodMetricIds

                            val bloodData = bloodList.filter { it.metric_id == bloodMetricId }
                            val gutData = summaryMetricsList.filter { it.metric?.metricId == targetMetricId }

                            if (bloodData.isNotEmpty() && gutData.isNotEmpty()) {
                                val gutValue = gutData[0].summary?.inference
                                val bloodLevel = bloodData[0].display_rating

                                val relatedObject =
                                    gutCorrleations.find { it.gutValue == gutValue && it.bloodLevel == bloodLevel }

                                if (relatedObject != null) { //TODO need to recomment it
                                    //assertEquals(relatedObject.Description.normalizeForUiCompare(), correlations.description.normalizeForUiCompare())
                                }
                            }

                        }
                    }
                }
                if (correlations.sourceType == "gene") {

                    val gutMapping = geneGutMappings.find { it.gut_metric_id == targetMetricId }
                    if (gutMapping != null) { //TODO need to recomment it
                        // assertEquals(gutMapping.gene_upsell.normalizeForUiCompare(), correlations.description.normalizeForUiCompare())
                    }
                }

                val desUiElement = page.getByTestId("biomarker-description-$index")
                desUiElement.waitFor()
                val actualDescription = desUiElement.innerText()
                logger.info { "Validating Description for [$index]: Expected='${correlations.description}', Actual='$actualDescription'" }
                assertEquals(correlations.description, actualDescription)
            }
        }
    }

    private fun checkWhatItMean(summaryMetricsList: List<GutMetricItem>?) {
        whatItMeanTitleCheck(summaryMetricsList)

        //other factor
        whatItMeanOtherFactor(summaryMetricsList)

        whatItMeanSections(summaryMetricsList)
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
        val actualBottomLineTitle = bottomLineTitle.innerText()
        logger.info { "Validating Bottom Line Title: Expected='Bottom line', Actual='$actualBottomLineTitle'" }
        assertEquals("Bottom line", actualBottomLineTitle)

        val bottomLineText = page.getByTestId("bottom-line-text")
        bottomLineText.waitFor()
        val actualBottomLineText = bottomLineText.innerText()
        val expectedBottomLine = bottomLine?.content

        logger.info { "Validating Bottom Line Content: Expected='$expectedBottomLine', Actual='$actualBottomLineText'" }
        assertEquals(
            expectedBottomLine?.normalizeForUiCompare(),
            actualBottomLineText.normalizeForUiCompare()
        )
    }

    private fun whatItMeanSections(summaryMetricsList: List<GutMetricItem>?) {
        val details = summaryMetricsList?.firstOrNull()?.details

        val sections = parseSections(details)

        logger.info { "whatItMeanSections.. ${summaryMetricsList?.get(0)?.metric?.groupName}" }

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

                        val actualTitleText = titleActual.innerText()
                        val actualContentText = contentActual.innerText()

                        logger.info { "Validating Section $sectionPosition [Title]: Expected='$titleExcepted', Actual='$actualTitleText'" }
                        assertEquals(
                            titleExcepted?.normalizeForUiCompare(),
                            actualTitleText.normalizeForUiCompare()
                        )
                        logger.info { "Validating Section $sectionPosition [Content]: Expected='$contextExcepted', Actual='$actualContentText'" }
                        assertEquals(
                            contextExcepted?.normalizeForUiCompare(),
                            actualContentText.normalizeForUiCompare()
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

                                if (!subSection.title.isNullOrBlank()) {

                                    val subsectionTitle = page.getByTestId("subsection-title-$sectionPosition-$index")
                                    subsectionTitle.waitFor()
                                    subsectionTitle.scrollIntoViewIfNeeded()

                                    val actualSubTitle = subsectionTitle.innerText()
                                    logger.info { "Validating Subsection $sectionPosition-$index [Title]: Expected='${subSection.title}', Actual='$actualSubTitle'" }
                                    assertEquals(
                                        subSection.title.normalizeForUiCompare(),
                                        actualSubTitle.normalizeForUiCompare()
                                    )
                                }


                                if (!subSection.description.isNullOrBlank()) {
                                    val subsectionDescription =
                                        page.getByTestId("subsection-description-$sectionPosition-$index")
                                    subsectionDescription.waitFor()
                                    subsectionDescription.scrollIntoViewIfNeeded()
                                    val actualSubDesc = subsectionDescription.innerText()
                                    logger.info { "Validating Subsection $sectionPosition-$index [Description]: Expected='${subSection.description}', Actual='$actualSubDesc'" }
                                    assertEquals(
                                        subSection.description.normalizeForUiCompare(),
                                        actualSubDesc.normalizeForUiCompare()
                                    )
                                }
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
        val actualFactorTitle = otherFactorTitle.innerText()
        val expectedFactorTitle = factorList?.get(0)
        logger.info { "Validating Other Factors Title: Expected='$expectedFactorTitle', Actual='$actualFactorTitle'" }
        assertEquals(expectedFactorTitle, actualFactorTitle.normalizeForUiCompare())

        factorList?.drop(1)?.forEachIndexed { index, factor ->
            val factorUiElement = page.getByTestId("other-factor-text-${index}")
            factorUiElement.waitFor()
            factorUiElement.scrollIntoViewIfNeeded()
            val actualFactorText = factorUiElement.innerText()
            logger.info { "Validating Other Factor [$index]: Expected='$factor', Actual='$actualFactorText'" }
            assertEquals(factor.normalizeForUiCompare(), actualFactorText.normalizeForUiCompare())
        }
    }

    private fun whatItMeanTitleCheck(summaryMetricsList: List<GutMetricItem>?) {
        val descriptionExpected =
            summaryMetricsList?.getOrNull(0)?.details?.filter { it.category == "description" }?.get(0)?.content
        val groupName = summaryMetricsList?.get(0)?.metric?.groupName
        val titleUiElement = page.getByTestId("why-tab-title")
        val descriptionUiElement = page.getByTestId("description-content")


        titleUiElement.waitFor()
        val actualTitleText = titleUiElement.innerText()
        assertEquals(actualTitleText.normalizeForUiCompare(), groupName?.normalizeForUiCompare())

        descriptionUiElement.waitFor()
        val descriptionActual = descriptionUiElement.innerText()
        assertEquals(descriptionExpected?.normalizeForUiCompare(), descriptionActual?.normalizeForUiCompare())
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

    private fun detailsHeaderValidations(groupName: String, summaryMetricsList: List<GutMetricItem>?) {
        val descriptionExpected =
            summaryMetricsList?.getOrNull(0)?.details?.filter { it.category == "short_description" }
                ?.getOrNull(0)?.content
        val parameterTitle = page.getByTestId("parameter-title")
        val descriptionUiElement = page.getByTestId("short-description-content")

        parameterTitle.waitFor()
        val actualTitleText = parameterTitle.innerText()
        assertEquals(actualTitleText.normalizeForUiCompare(), groupName.normalizeForUiCompare())


        descriptionUiElement.waitFor()
        val descriptionActual = descriptionUiElement.innerText()
        assertEquals(descriptionExpected?.normalizeForUiCompare(), descriptionActual.normalizeForUiCompare())
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

    /**------------------Search View-------------------------*/
    fun gutSearchViewValidation() {
        val gutList = gutDataWrapper?.gut?.data
        if (gutList.isNullOrEmpty()) return


        val searchView = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in Gut"))
        searchView.waitFor()
        val inference = gutList.get(0).inference

        val filteredGuList = gutList.filter { it.inference == inference }

        searchView.fill("$inference")

        headerValidations(getGutDataByGroup(filteredGuList))

        searchView.fill("")

        headerValidations(getGutDataByGroup(gutList))
    }

    /**------------------Filter View-------------------------*/
    fun gutFilterViewValidation() {

        val gutList = gutDataWrapper?.gut?.data
        if (gutList.isNullOrEmpty()) return

        val idealsList = gutList.filter { it.inference == RiskLevel.Ideal.value }
        val nonIdealsList = gutList.filter { it.inference == RiskLevel.NonIdeal.value }
        val moderateList = gutList.filter { it.inference == RiskLevel.ModerateRisk.value }

        val idealFilter=page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Ideal (${idealsList.size})"))
        val nonIdealFilter=page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Non Ideal (${nonIdealsList.size})"))
        val improvementFilter=page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Needs Improvement (${moderateList.size})"))

        listOf(idealFilter,nonIdealFilter,improvementFilter).forEach { it.waitFor() }

        idealFilter.click()
        headerValidations(getGutDataByGroup(idealsList))


        nonIdealFilter.click()
        headerValidations(getGutDataByGroup(nonIdealsList))


        improvementFilter.click()
        headerValidations(getGutDataByGroup(moderateList))

    }

}