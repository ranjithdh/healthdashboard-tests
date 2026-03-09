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
import mobileView.home.gene.model.GeneMetricCorrelation
import mobileView.home.gene.model.GeneMetricData
import mobileView.home.gene.model.GeneMetricDetail
import mobileView.home.gene.model.GeneMetricItem
import mobileView.home.gene.model.GeneMetricResponse
import mobileView.home.gene.model.GeneResponse
import mobileView.home.gut.model.GutDataWrapper
import mobileView.home.gut.model.GutMetricItem
import mobileView.home.gut.model.GutResponse
import mobileView.home.gut.model.MetricCorrelation
import mobileView.home.gut.model.MetricDetail
import mobileView.home.gut.model.ParsedSection
import mobileView.home.gut.model.ParsedSubSection
import mobileView.home.gut.util.GutUtility.gutSourceType
import mobileView.home.gut.util.GutUtility.toKebabCase
import mobileView.home.gut.util.TestMappingLoader
import model.healthdata.HealthData
import utils.Normalize.refactorTimeZone
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_GENE_DATA
import utils.report.StepHelper.FETCH_GUT_DATA
import utils.report.StepHelper.FETCH_HEALTH_DATA
import utils.report.StepHelper.VALIDATING_CONNECTED_BIOMARKERS_TAB
import utils.report.StepHelper.VALIDATING_GENE_LIST
import utils.report.StepHelper.VALIDATING_WHAT_IT_MEANS_TAB
import utils.report.StepHelper.logApiResponse
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.test.assertEquals
import kotlin.text.replace
import kotlin.text.startsWith

class GenePage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL

    private var gutDataWrapper: GutDataWrapper? = null
    private var geneMetricData: GeneMetricData? = null

    private var geneDataWrapper: GeneDataWrapper? = null

    private var healthData: HealthData? = null

    private val bloodGeneCorrleations = TestMappingLoader.loadBloodGeneCorrelationsMappings() //data

    private val geneGutMappings = TestMappingLoader.loadGeneGutMappings() //data


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


    /**------------Gene Details---------------*/

    fun geneDetailsValidation() {
        val geneList = geneDataWrapper?.gene?.data
        if (geneList?.isNotEmpty() == true) {
            val geneListGroupByName = getGeneDataByGroup(geneList)
            geneListGroupByName.keys.forEach { groupName ->
                val id = toKebabCase(groupName)
                val markerUiElement = page.getByTestId("gene-group-marker-$id")

                markerUiElement.click()

                val geneItemList = geneListGroupByName[groupName]

                geneItemList?.forEach { geneItem ->
                    val metricID = geneItem.metric?.metricId

                    val nameUiElement = page.getByTestId("gene-item-name-$metricID")
                    nameUiElement.click()

                    captureGeneDetails(listOf(metricID)) //API call

                    if (geneMetricData != null) {
                        validatingGeneDetails(geneMetricData)
                    }

                    page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Back")).click()
                }
            }
        }
    }

    private fun validatingGeneDetails(geneMetricData: GeneMetricData?) {
        val metricData = geneMetricData?.metrics?.get(0)

        geneDetailsHeaderValidation(metricData)

        tabChecking(metricData)


        bottomLineValidation(metricData)
    }

    private fun tabChecking(summaryMetricsList: GeneMetricItem?) {
        val isWhyTab = shouldShowWhyTab(summaryMetricsList)
        val isConnectedTab = shouldShowConnectedTab(summaryMetricsList)
        if (isWhyTab) {
            StepHelper.step(VALIDATING_WHAT_IT_MEANS_TAB)
            logger.info { "Checking 'What it means' tab" }
            val whatItMeansTab = page.getByTestId("gene-what-it-means-tab")
            whatItMeansTab.waitFor()
            whatItMeansTab.click()
            checkWhatItMean(summaryMetricsList)
        }
        if (isConnectedTab) {
            StepHelper.step(VALIDATING_CONNECTED_BIOMARKERS_TAB)
            logger.info { "Checking 'Connected Biomarkers' tab" }
            val connectedTab = page.getByTestId("gene-connected-biomarkers-tab")
            connectedTab.waitFor()
            connectedTab.click()
            checkConnectedBiomarkers(summaryMetricsList)
        }
    }

    private fun checkConnectedBiomarkers(summaryMetricsList: GeneMetricItem?) {
        val allCorrelations: List<GeneMetricCorrelation>? =
            summaryMetricsList?.correlations?.filter { !it.description.isNullOrBlank() || !it.sourceInference.isNullOrBlank() }
                ?.associateBy { it.sourceMetricId }?.values
                ?.toList()


        val subText =
            "These biomarkers are influenced by your genetic profile and can help monitor your health status."
        val title = "Connected Biomarkers"

        val titleUiElement = page.getByTestId("gene-connected-biomarkers-title")
        titleUiElement.waitFor()
        val actualTitle = titleUiElement.innerText()
        logger.info { "Validating Connected Biomarkers Title: Expected='$title', Actual='$actualTitle'" }
        assertEquals(title, actualTitle)

        val subTexUiElement = page.getByTestId("gene-connected-biomarkers-description")
        subTexUiElement.waitFor()
        val actualSubText = subTexUiElement.innerText()
        logger.info { "Validating Connected Biomarkers Subtext: Expected='$subText', Actual='$actualSubText'" }
        assertEquals(subText, actualSubText)

        allCorrelations?.forEachIndexed { index, correlations ->
            val biomarkerName = correlations.sourceMetricName
            logger.info { "Validating Biomarker [$index]: $biomarkerName" }

            val nameUiElement = page.getByTestId("gene-biomarker-name-$index")
            val typeUiElement = page.getByTestId("gene-biomarker-type-$index")


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
                    page.getByTestId("gene-biomarker-gene-inference-$index")
                } else {
                    page.getByTestId("gene-biomarker-inference-$index")
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
                        val gutCorrleations = bloodGeneCorrleations.filter { it.geneMetricId == targetMetricId }

                        if (gutCorrleations.isNotEmpty()) {
                            val bloodMetricId = gutCorrleations[0].bloodMetricId

                            val bloodData = bloodList.filter { it.metric_id == bloodMetricId }
                            val geneData = summaryMetricsList
                            //   val gutData = summaryMetricsList.filter { it.metric?.metricId == targetMetricId }

                            if (bloodData.isNotEmpty() && geneData != null) {
                                val gutValue = geneData.summary.inference
                                val bloodLevel = bloodData[0].display_rating

                                val relatedObject =
                                    gutCorrleations.find { it.revisedRating == gutValue && it.bloodLevel == bloodLevel }

                                if (relatedObject != null) { //TODO need to recomment it
                                    //assertEquals(relatedObject.Description.normalizeForUiCompare(), correlations.description.normalizeForUiCompare())
                                }
                            }

                        }
                    }
                }
                if (correlations.sourceType == "gut") {

                    val gutMapping = geneGutMappings.find { it.gut_metric_id == targetMetricId }
                    if (gutMapping != null) { //TODO need to recomment it
                        // assertEquals(gutMapping.gene_upsell.normalizeForUiCompare(), correlations.description.normalizeForUiCompare())
                    }
                }

                if (!correlations.description.isNullOrBlank()) {
                    val desUiElement = page.getByTestId("gene-biomarker-description-$index")
                    desUiElement.waitFor()
                    val actualDescription = desUiElement.innerText()
                    logger.info { "Validating Description for [$index]: Expected='${correlations.description}', Actual='$actualDescription'" }
                    assertEquals(
                        correlations.description.normalizeForUiCompare(),
                        actualDescription.normalizeForUiCompare()
                    )
                }
            }
        }
    }

    private fun checkWhatItMean(summaryMetricsList: GeneMetricItem?) {
        whatItMeanTitleCheck(summaryMetricsList)

        //other factor
        whatItMeanOtherFactor(summaryMetricsList)

        whatItMeanSections(summaryMetricsList)
    }

    private fun whatItMeanTitleCheck(summaryMetricsList: GeneMetricItem?) {
        val descriptionExpected =
            summaryMetricsList?.details?.filter { it.category == "impact_of_your_genes" }?.get(0)?.content
        val groupName = "Impact of your genes"
        val titleUiElement = page.getByTestId("gene-impact-title")
        val descriptionUiElement = page.getByTestId("gene-impact-content")


        titleUiElement.waitFor()
        val actualTitleText = titleUiElement.innerText()
        assertEquals(actualTitleText.normalizeForUiCompare(), groupName.normalizeForUiCompare())

        descriptionUiElement.waitFor()
        val descriptionActual = descriptionUiElement.innerText()
        assertEquals(descriptionExpected?.normalizeForUiCompare(), descriptionActual?.normalizeForUiCompare())
    }

    private fun whatItMeanOtherFactor(summaryMetricsList: GeneMetricItem?) {


        val otherFactors = summaryMetricsList?.details?.find { it.category == "factors" }

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


        val otherFactorTitle = page.getByTestId("gene-other-factors-title")
        otherFactorTitle.waitFor()

        val actualFactorTitle = otherFactorTitle.innerText()
        val expectedFactorTitle = factorList?.get(0)
        logger.info { "Validating Other Factors Title: Expected='$expectedFactorTitle', Actual='$actualFactorTitle'" }
        assertEquals(expectedFactorTitle, actualFactorTitle.normalizeForUiCompare())

        factorList?.drop(1)?.forEachIndexed { index, factor ->
            val factorUiElement = page.getByTestId("gene-other-factor-text-${index}")
            factorUiElement.waitFor()
            factorUiElement.scrollIntoViewIfNeeded()
            val actualFactorText = factorUiElement.innerText()
            logger.info { "Validating Other Factor [$index]: Expected='$factor', Actual='$actualFactorText'" }
            assertEquals(factor.normalizeForUiCompare(), actualFactorText.normalizeForUiCompare())
        }
    }

    private fun whatItMeanSections(summaryMetricsList: GeneMetricItem?) {
        val details = summaryMetricsList?.details

        val sections = parseSections(details)

        logger.info { "whatItMeanSections.. ${summaryMetricsList?.metric?.groupName}" }

        if (sections.isNotEmpty()) {
            sections.forEachIndexed { sectionPosition, section ->
                val titleExcepted = section.mainTitle
                val contextExcepted = section.plainContent
                val titleActual = page.getByTestId("gene-section-title-$sectionPosition")
                val contentActual = page.getByTestId("gene-section-content-$sectionPosition")

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
                /*when (sectionPosition) {
                    0 -> {

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
                }*/
            }

        }

        logger.info {
            "List of sections:\n${sections?.joinToString("\n") ?: "No sections found"}"
        }
    }

    fun parseSections(details: List<GeneMetricDetail>?): List<ParsedSection> {
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


    private fun bottomLineValidation(metricData: GeneMetricItem?) {

        logger.info { "Bottom line validation validation '${metricData?.metric?.displayName}'" }
        val bottomLine = metricData
            ?.details
            ?.firstOrNull { it.category == "bottom_line" }

        if (bottomLine?.content.isNullOrEmpty()) return

        val bottomLineTitle = page.getByTestId("gene-bottom-line-title")
        bottomLineTitle.waitFor()
        val actualBottomLineTitle = bottomLineTitle.innerText()
        logger.info { "Validating Bottom Line Title: Expected='Bottom line', Actual='$actualBottomLineTitle'" }
        assertEquals("Bottom line", actualBottomLineTitle)

        val bottomLineText = page.getByTestId("gene-bottom-line-text")
        bottomLineText.waitFor()
        bottomLineText.scrollIntoViewIfNeeded()
        val actualBottomLineText = bottomLineText.innerText()
        val expectedBottomLine = bottomLine?.content

        logger.info { "Validating Bottom Line Content: Expected='$expectedBottomLine', Actual='$actualBottomLineText'" }
        assertEquals(
            expectedBottomLine?.normalizeForUiCompare()?.replace("\\", "")?.trim(),
            actualBottomLineText.normalizeForUiCompare()
        )
    }

    private fun geneDetailsHeaderValidation(metricData: GeneMetricItem?) {


        val name = metricData?.metric?.displayName?.normalizeForUiCompare()
        val inference = metricData?.summary?.inference
        val displayDescription = metricData?.summary?.displayDescription


        val titleUiElement = page.getByTestId("gene-title")
        val badgeUiElement = page.getByTestId("gene-inference-badge")
        val descriptionUiElement = page.getByTestId("gene-display-description")


        titleUiElement.waitFor()
        assertEquals(name, titleUiElement.innerText().normalizeForUiCompare().replace(inference ?: "", "").trim())

        if (!inference.isNullOrBlank()) {
            badgeUiElement.waitFor()
            assertEquals(metricData.summary.inference, badgeUiElement.innerText())
        }


        if (!displayDescription.isNullOrBlank()) {
            descriptionUiElement.waitFor()
            assertEquals(displayDescription, descriptionUiElement.innerText())
        }
    }

    private fun captureGeneDetails(metricIds: List<String?>) {
        try {
            val timeZone = java.util.TimeZone.getDefault().id

            val metricParams = metricIds.joinToString("&") { "metric_id[]=$it" }


            val apiUrl =
                "${TestConfig.APIs.API_GENE_DETAILS}?$metricParams"

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


            val responseObj = json.decodeFromString<GeneMetricResponse>(responseBody)

            if (responseObj.status == "success") {
                geneMetricData = responseObj.data
                logApiResponse(apiUrl, responseObj)
            }

        } catch (e: Exception) {
            logger.error { "Failed to fetch account information: ${e.message}" }
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


    fun shouldShowWhyTab(metrics: GeneMetricItem?): Boolean {
        return metrics
            ?.details
            ?.isNotEmpty() == true
    }

    fun shouldShowConnectedTab(metrics: GeneMetricItem?): Boolean {
        return metrics?.correlations?.isNotEmpty() == true
    }

}