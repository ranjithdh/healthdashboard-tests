package mobileView.diagnostics

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import forWeb.diagnostics.page.TestSchedulingPage
import io.qameta.allure.Epic
import org.junit.jupiter.api.*
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import utils.logger.logger
import utils.report.Modules
import utils.report.StepHelper


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@Epic(Modules.EPIC_BOOKLABTEST)
class LabTestsTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var labTestsPage: LabTestsPage

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())

        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
        labTestsPage = performInitialNavigation()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun performInitialNavigation(): LabTestsPage {
        val pageObject = LabTestsPage(page)
        // LabTestsPage.init/getLabTestsResponse already handles login and navigation
        // but we can make it explicit if preferred, or just return the initialized page.
        return pageObject
    }

    @BeforeEach
    fun resetToDiagnosticsPage() {
        // Ensure every test starts at the diagnostics page
        if (!page.url().contains("diagnostics")) {
            logger.info { "Navigating back to Diagnostics URL..." }
            page.navigate(TestConfig.Urls.DIAGNOSTICS_URL)
            page.waitForLoadState()
        }
    }

    @Test
    @Order(1)
    fun `verify lab tests page static texts and segments`() {
        labTestsPage.checkStaticTextsAndSegments()
    }


    @Test
    @Order(2)
    fun `verify lab tests cards using API response`() {

        logger.info { "Starting test: verify lab tests cards using API response" }
        StepHelper.step("Starting test: verify lab tests cards using API response")

        // Capture the API response during navigation
        logger.info { "Navigating to diagnostics page and capturing API response..." }
        StepHelper.step("Navigating to diagnostics page and capturing API response...")
        val responseObj = labTestsPage.labTestData ?: throw AssertionError("Failed to capture Lab Test API response")

        // Parse response and verify cards
        logger.info { "Parsing API response..." }
        StepHelper.step("Parsing API response...")
        val productList = responseObj.data?.diagnostic_product_list
            ?: throw AssertionError("diagnostic_product_list not found in API response")
        data class TestCardData(val code: String, val name: String, val sampleType: String, val rawSampleType: String, val price: String)
        val testCards = mutableListOf<TestCardData>()

        fun addTestCard(code: String?, name: String?, rawSampleType: String?, priceStr: String?) {
            if (code == null) return
            val safeName = name ?: ""
            val safeRawSampleType = rawSampleType ?: ""

            // Sample type logic as per user requirement
            val sampleType = when {
                code.startsWith("CORTISOL") -> "At-Home Test Kit"
                code.startsWith("OMEGA") -> "At-Home Test Kit"
                safeRawSampleType.lowercase() == "saliva" -> "At-Home Test Kit"
                safeRawSampleType.lowercase() == "stool" -> "At-Home Test Kit"
                else -> "Blood test"
            }

            // Price extraction and formatting
            val rawPrice = priceStr?.toDoubleOrNull() ?: 0.0
            val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
            numberFormat.maximumFractionDigits = 0
            val formattedPrice = "₹ " + numberFormat.format(rawPrice)

            testCards.add(TestCardData(code, safeName, sampleType, safeRawSampleType, formattedPrice))
        }

        // Extract data from all sections
        productList.packages?.forEach { addTestCard(it.code, it.name, it.sample_type, it.product?.price) }
        productList.test_profiles?.forEach { addTestCard(it.code, it.name, it.sample_type, it.product?.price) }
        productList.tests?.forEach { addTestCard(it.code, it.name, it.sample_type, it.product?.price) }

        logger.info { "Found ${testCards.size} cards total." }
        StepHelper.step("Found ${testCards.size} cards total.")

        // initial check - All should be visible
        logger.info { "Verifying initial state (All visible)..." }
        StepHelper.step("Verifying initial state (All visible)...")
        testCards.forEach { card ->
            labTestsPage.verifyTestCard(card.code, card.name, card.sampleType, card.price)
        }

        // --- Filter Testing Logic ---

        fun getCategory(rawSampleType: String): String {

            return when {
                rawSampleType.contains("stool", ignoreCase = true) -> "Gut"
                // Gene is specifically "saliva" but NOT "saliva_stress" (Cortisol)
                rawSampleType.equals("saliva", ignoreCase = true) -> "Gene"
                // Blood is specifically "blood" but NOT "dried_blood_spot" (Metabolic/Omega)
                rawSampleType.equals("blood", ignoreCase = true) -> "Blood"
                else -> "All"
            }
        }

        val activeFilters = mutableSetOf<String>()
        activeFilters.add("All") // Initially All is selected

        fun verifyVisibility() {
            logger.info { "Verifying visibility for filters: $activeFilters" }
            StepHelper.step("Verifying visibility for filters: $activeFilters")
            testCards.forEach { card ->
                val category = getCategory(card.rawSampleType)

                val shouldBeVisible = if (activeFilters.contains("All")) {
                    true
                } else {
                    activeFilters.contains(category)
                }

                val isVisible = labTestsPage.isTestCardVisible(card.code)

                // Assert
                if (isVisible != shouldBeVisible) {
                    logger.info { "Mismatch: Card '${card.name}' ($category) [raw: ${card.rawSampleType}] - Expected: $shouldBeVisible, Actual: $isVisible" }
                    StepHelper.step("Mismatch: Card '${card.name}' ($category) [raw: ${card.rawSampleType}] - Expected: $shouldBeVisible, Actual: $isVisible")
                }
                assert(isVisible == shouldBeVisible) {
                    "Visibility mismatch for card '${card.name}' ($category) [raw: ${card.rawSampleType}]. Active Filters: $activeFilters. Expected Visible: $shouldBeVisible, Actual: $isVisible"
                }
            }
            logger.info { "Verified visibility successfully." }
            StepHelper.step("Verified visibility successfully.")
        }

        // Helper to simulate clicking a filter
        fun toggleFilter(filterName: String) {
            logger.info { ">> Clicking filter: $filterName" }
            StepHelper.step(">> Clicking filter: $filterName")
            labTestsPage.clickFilter(filterName)

            if (filterName == "All") {
                activeFilters.clear()
                activeFilters.add(filterName)
            } else {
                if (activeFilters.contains("All")) {
                    activeFilters.remove("All")
                    activeFilters.add(filterName)
                } else {
                    if (activeFilters.contains(filterName)) {
                        activeFilters.remove(filterName)
                    } else {
                        activeFilters.add(filterName)
                    }
                }
                // Handle empty state - if no filters selected, what shows?
                // Assuming checking specific additive flows where at least one is active or reverting to All logic if app does it.
                // In our sequence, we aren't emptying it completely without expectation.
            }
            // Give a moment for UI to update animations
            page.waitForTimeout(1000.0)
        }

        // Test Sequence from User request (Permutations & Combinations)

        // 1. Click Blood -> Select Blood (All removed)
        toggleFilter("Blood")
        verifyVisibility()

        // 2. Click Gene -> Select Blood + Gene
        toggleFilter("Gene")
        verifyVisibility()

        // 3. Click Blood (Deselect) -> Select Gene only
        toggleFilter("Blood")
        verifyVisibility()

        // 4. Click Gut -> Select Gene + Gut
        toggleFilter("Gut")
        verifyVisibility()

        // 5. Click All -> Reset to All
        toggleFilter("All")
        verifyVisibility()

        // 6. Click Gut -> Select Gut only
        toggleFilter("Gut")
        verifyVisibility()

        // 7. Click Gene -> Select Gut + Gene
        toggleFilter("Gene")
        verifyVisibility()

        // 8. Click Blood -> Select Gut + Gene + Blood (Should be all visible)
        toggleFilter("Blood")
        verifyVisibility()

        logger.info { "Filter permutation test completed successfully." }
        StepHelper.step("Filter permutation test completed successfully.")
    }
    @Test
    @Order(3)
    fun `verify detail page components`() {
        logger.info { "Starting test: verify detail page components" }
        StepHelper.step("Starting test: verify detail page components")

        // Capture the API response during navigation
        logger.info { "Navigating to diagnostics page and capturing API response..." }
        StepHelper.step("Navigating to diagnostics page and capturing API response...")
        val responseObj = labTestsPage.labTestData ?: throw AssertionError("Failed to capture Lab Test API response")

        val targetCode = "GENE10001" // "GENE10001" //"GUT10002" //"P250" //"GENE10001" // "PROJ1056379" //"DH_LONGEVITY_PANEL"

        // Parse list response to find the target item
        val productList = responseObj.data?.diagnostic_product_list ?: throw AssertionError("diagnostic_product_list not found")
        
        val targetPackage = productList.packages?.find { it.code == targetCode }
            ?: productList.test_profiles?.find { it.code == targetCode }
            ?: productList.tests?.find { it.code == targetCode }
            ?: throw AssertionError("Item with code $targetCode not found in API response")

        val content = targetPackage.let { 
            when (it) {
                is model.LabTestPackage -> it.content
                is model.LabTestProfile -> it.content
                is model.LabTestItem -> it.content
                else -> null
            }
        } ?: throw AssertionError("Content not found for $targetCode")

        // Extract descriptions
        val whatMeasuredDesc = content.what_measured_description ?: ""
        val whatToExpectDesc = content.what_to_expect_description ?: ""

        // 'who' is a list in the model
        val whoDesc = content.who?.firstOrNull() ?: ""

        logger.info { "Expected What's Measured: $whatMeasuredDesc" }
        StepHelper.step("Expected What's Measured: $whatMeasuredDesc")
        logger.info { "Expected Who: $whoDesc" }
        StepHelper.step("Expected Who: $whoDesc")
        logger.info { "Expected What to Expect: $whatToExpectDesc" }
        StepHelper.step("Expected What to Expect: $whatToExpectDesc")

        // Logic to determine expected highlights based on React frontend logic
        val rawHighlights = content.highlights ?: emptyList()
        val expectedHighlights = mutableListOf<String>()

        // 1. highlights[0]
        if (rawHighlights.isNotEmpty()) expectedHighlights.add(rawHighlights[0])

        val hasHighlight5 = rawHighlights.size > 5 && rawHighlights[5].isNotBlank()

        // 2. Prep/Fasting block (Only if !hasHighlight5)
        if (!hasHighlight5) {
            val code = when (targetPackage) {
                is model.LabTestPackage -> targetPackage.code
                is model.LabTestProfile -> targetPackage.code
                is model.LabTestItem -> targetPackage.code
                else -> null
            }
            val vendorProductId = when (targetPackage) {
                is model.LabTestPackage -> targetPackage.product?.vendor_product_id
                is model.LabTestProfile -> targetPackage.product?.vendor_product_id
                is model.LabTestItem -> targetPackage.product?.vendor_product_id
                else -> null
            }

            if (code == "DH_LONGEVITY_PANEL" || vendorProductId == "DH_LONGEVITY_PANEL" ||
                code == "DH_METABOLIC_PANEL" || vendorProductId == "DH_METABOLIC_PANEL") {

                if (code == "DH_METABOLIC_PANEL" || vendorProductId == "DH_METABOLIC_PANEL") {
                    expectedHighlights.add("Two Blood Draws Required: Fasting and Post-Breakfast")
                } else {
                    expectedHighlights.add("Fasting Required")
                    expectedHighlights.add("Post-Meal Blood Draw Required")
                }

            } else {
                val sampleType = when (targetPackage) {
                    is model.LabTestPackage -> targetPackage.sample_type
                    is model.LabTestProfile -> targetPackage.sample_type
                    is model.LabTestItem -> targetPackage.sample_type
                    else -> null
                }?.lowercase()
                val preparation = content.preparation
                val isFastingRequired = when (targetPackage) {
                    is model.LabTestPackage -> targetPackage.is_fasting_required
                    is model.LabTestProfile -> targetPackage.is_fasting_required
                    is model.LabTestItem -> targetPackage.is_fasting_required
                    else -> null
                } == true

                if (sampleType == "stool" || sampleType == "saliva") {
                    expectedHighlights.add(preparation ?: "No preparation required")
                } else if (isFastingRequired) {
                    val fastingInfo = content.fasting_info
                    val cleanedFastingInfo = fastingInfo?.replace(Regex("fasting\\s*", RegexOption.IGNORE_CASE), "")?.trim() ?: "required"
                    expectedHighlights.add("Fasting $cleanedFastingInfo")
                } else {
                    expectedHighlights.add("No fasting required")
                }
            }
        }

        // 3. when_to_take
        val whenToTake = content.when_to_take
        if (!whenToTake.isNullOrBlank()) {
            expectedHighlights.add(whenToTake)
        }

        // 4. highlights[2]
        if (rawHighlights.size > 2) expectedHighlights.add(rawHighlights[2])

        // 5. highlights[5] - if exists
        if (hasHighlight5) expectedHighlights.add(rawHighlights[5])

        // 6. highlights[3]
        if (rawHighlights.size > 3) expectedHighlights.add(rawHighlights[3])

        // 7. highlights[4]
        if (rawHighlights.size > 4) expectedHighlights.add(rawHighlights[4])

        logger.info { "Calculated Expected Highlights (Frontend Logic): $expectedHighlights" }
        StepHelper.step("Calculated Expected Highlights (Frontend Logic): $expectedHighlights")

        logger.info { "Clicking View Details for code $targetCode" }
        // Click View Details for the specific panel
        labTestsPage.clickViewDetails(targetCode)

        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)

        // Verify Header Info (Name, Short Description, About Description)
        testDetailPage.verifyTestHeaderInfo(targetCode)
        testDetailPage.verifyHighlights(expectedHighlights)
        // Verify "How it Works?" section
        logger.info { "Verifying How it Works section..." }
        StepHelper.step("Verifying How it Works section...")
        val howItWorksBySampleType = mapOf(
            "saliva" to mapOf(
                "title" to "Get Gene Kit Delivered",
                "description" to "Your DNA kit arrives at your doorstep with simple cheek swab instructions."
            ),
            "stool" to mapOf(
                "title" to "Get Gut Kit Delivered",
                "description" to "Your gut test kit arrives at your doorstep with easy sample collection instructions."
            ),
            "dried_blood_spot" to mapOf(
                "title" to "Get Omega Test Kit Delivered",
                "description" to "Your Omega test kit arrives at your doorstep with an easy DBS tool."
            ),
            "saliva_stress" to mapOf(
                "title" to "Get Cortisol Test Kit Delivered",
                "description" to "Your cortisol test kit arrives at your doorstep with an easy saliva collection tube."
            )
        )

        val sampleCollectionDescription = mapOf(
            "saliva" to "Schedule a quick home visit — our technician collects your sample in minutes.",
            "stool" to "Collect your stool sample and schedule a quick pickup from home.",
            "blood" to "Schedule the blood sample collection from the comfort of your home.",
            "dried_blood_spot" to "Do easy DBS test by yourself and schedule a quick pickup from home.",
            "saliva_stress" to "Collect your saliva sample as per the instructions and schedule a quick pickup from home."
        )

        val consultDescription = mapOf(
            "saliva" to "Chat with our experts to understand your results and get personalised guidance.",
            "stool" to "Discuss your gut health report with our experts and get personalised guidance.",
            "blood" to "See how your antibody levels connect with your symptoms by talking to our experts.",
            "dried_blood_spot" to "Discuss your Omega panel report with our experts and get personalised guidance.",
            "saliva_stress" to "Discuss your stress and cortisol report with our experts and get personalised guidance."
        )

        val getResultsDescription = mapOf(
            "saliva" to "Your sample is analysed in a certified lab, and your report goes live on your dashboard.",
            "stool" to "Your sample is analysed in a certified lab, and results are shared on your dashboard.",
            "blood" to "Your sample is processed at a certified lab, and your report is ready online in 72 hours.", // Note: Blood handled dynamically usually, but logic map exists
            "dried_blood_spot" to "Your sample is analysed in a certified lab, and results are shared on your dashboard.",
            "saliva_stress" to "Your sample is analysed in a certified lab, and results are shared on your dashboard."
        )

        val sampleType = when (targetPackage) {
            is model.LabTestPackage -> targetPackage.sample_type
            is model.LabTestProfile -> targetPackage.sample_type
            is model.LabTestItem -> targetPackage.sample_type
            else -> ""
        }?.lowercase() ?: ""

        val reportGenHr = when (targetPackage) {
            is model.LabTestPackage -> targetPackage.report_generation_hr
            is model.LabTestProfile -> targetPackage.report_generation_hr
            is model.LabTestItem -> targetPackage.report_generation_hr
            else -> "72 hours"
        } ?: "72 hours"

        val highlightsList = content.highlights ?: emptyList()

        val baseSteps = mutableListOf<Map<String, String>>()

        // Item 1 (Base/Blood)
        val title1 = if (listOf("saliva", "blood").contains(sampleType)) "At-Home Sample Collection" else "At-Home Self-Test Kit"
        val desc1 = sampleCollectionDescription[sampleType] ?: "Schedule the ${
            highlightsList.getOrNull(0) ?: "blood sample"
        } collection from the comfort of your home."
        baseSteps.add(mapOf("title" to title1, "description" to desc1))

        // Item 2 (Base/Blood)
        val title2 = highlightsList.getOrNull(3) ?: "Get Results in 72 Hours"
        val desc2 = if (sampleType == "blood") {
            "Your sample is processed at a certified lab, and your report is ready online in  $reportGenHr."
        } else {
            getResultsDescription[sampleType] ?: ""
        }
        baseSteps.add(mapOf("title" to title2, "description" to desc2))

        // Item 3 (Base/Blood)
        val title3 = "1-on-1 Expert Consultation"
        val desc3 = consultDescription[sampleType] ?: "See how your antibody levels connect with your symptoms by talking to our experts."
        baseSteps.add(mapOf("title" to title3, "description" to desc3))

        // Item 4 (Blood only append)
        if (sampleType == "blood") {
            baseSteps.add(mapOf(
                "title" to "Track Progress Overtime",
                "description" to "Monitor these markers over time to understand changes and treatment response."
            ))
        }

        val expectedHowItWorksSteps = mutableListOf<Map<String, String>>()

        if (sampleType != "blood" && sampleType.isNotEmpty()) {
            val prepend = howItWorksBySampleType[sampleType]
            if (prepend != null) {
                expectedHowItWorksSteps.add(prepend)
            }
            // Append base steps
            expectedHowItWorksSteps.addAll(baseSteps)
        } else {
            // Blood or others not in map
            expectedHowItWorksSteps.addAll(baseSteps)
        }

        // Click and verify buttons with text
        testDetailPage.expandAndVerifySection("What’s measured?", whatMeasuredDesc)
        testDetailPage.expandAndVerifySection("Who should take this test?", whoDesc)
        testDetailPage.expandAndVerifySection("What to expect?", whatToExpectDesc)

//        testDetailPage.verifyHowItWorks(expectedHowItWorksSteps)
//        testDetailPage.verifyCertifiedLabsSection()

        // Extract and format price
        val rawPrice = when (targetPackage) {
            is model.LabTestPackage -> targetPackage.product?.price?.toDoubleOrNull() ?: 0.0
            is model.LabTestProfile -> targetPackage.product?.price?.toDoubleOrNull() ?: 0.0
            is model.LabTestItem -> targetPackage.product?.price?.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
        numberFormat.maximumFractionDigits = 0
        val formattedPrice = "₹" + numberFormat.format(rawPrice)

        logger.info { "Expected Price: $formattedPrice" }
        StepHelper.step("Expected Price: $formattedPrice")

        // Verify Price and Booking Button
        testDetailPage.verifyPriceAndBookingButton(targetCode, formattedPrice)

        logger.info { "Test completed successfully." }
        StepHelper.step("Test completed successfully.")

        logger.info { "Verifying How It Works section..." }
        StepHelper.step("Verifying How It Works section...")
        val finalSampleType = when (targetPackage) {
            is model.LabTestPackage -> targetPackage.sample_type
            is model.LabTestProfile -> targetPackage.sample_type
            is model.LabTestItem -> targetPackage.sample_type
            else -> ""
        } ?: ""
        val finalReportGenHr = when (targetPackage) {
            is model.LabTestPackage -> targetPackage.report_generation_hr
                    is model.LabTestProfile -> targetPackage.report_generation_hr
            is model.LabTestItem -> targetPackage.report_generation_hr
            else -> null
        }
        val firstHighlight = if (rawHighlights.isNotEmpty()) rawHighlights[0] else null
        testDetailPage.verifyHowItWorksSection(finalSampleType, targetCode, finalReportGenHr, firstHighlight)

        logger.info { "Verifying Certified Labs section..." }
        StepHelper.step("Verifying Certified Labs section...")
        testDetailPage.verifyCertifiedLabsSection()

        logger.info { "Test completed successfully." }
        StepHelper.step("Test completed successfully.")
    }

    @Test
    @Order(4)
    fun `verify test scheduling`() {
        logger.info { "Starting test: verify test scheduling" }
        StepHelper.step("Starting test: verify test scheduling")

        // Capture the API response during navigation
        StepHelper.step("Navigating to diagnostics page and capturing API response...")
        val responseObj = labTestsPage.labTestData ?: throw AssertionError("Failed to capture Lab Test API response")
        val targetCode = "PROJ1056379"

        logger.info { "Clicking View Details for code $targetCode" }
        StepHelper.step("Clicking View Details for code $targetCode")
        labTestsPage.clickViewDetails(targetCode)

        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)

        val testSchedulingPage = TestSchedulingPage(page)
        logger.info { "Capturing address list and verifying scheduling page..." }
        StepHelper.step("Capturing address list and verifying scheduling page...")

        testSchedulingPage.captureAddressData {
            testDetailPage.clickBookNow(targetCode)
            // Fetch order details immediately after Book Now triggering order creation
            testSchedulingPage.callBloodDataReports()
        }

        testSchedulingPage.verifySampleCollectionAddressHeading()
        logger.info { "Testing 'Add New Address' functionality..." }
        StepHelper.step("Testing 'Add New Address' functionality...")
        testSchedulingPage.clickAddNewAddress()
        assert(testSchedulingPage.isNewAddressDialogVisible()) { "Add new address dialog is not visible" }
        testSchedulingPage.assertAddressFormFieldsVisible()
        testSchedulingPage.clickAddNewAddress()
        testSchedulingPage.addAddressAndValidate()
        assertDoesNotThrow { testSchedulingPage.assertAddressesFromApi() }

        logger.info { "Testing 'Edit Address' functionality..." }
        StepHelper.step("Testing 'Edit Address' functionality...")
        val addressCount = testSchedulingPage.getAddressCount()
        val randomIndex = (0 until addressCount).random()
        logger.info { "Selecting random address at index $randomIndex" }
        StepHelper.step("Selecting random address at index $randomIndex")
        testSchedulingPage.editUserAddress(randomIndex)
        // Extract price for the targetCode from responseObj
        val productList = responseObj.data?.diagnostic_product_list ?: throw AssertionError("diagnostic_product_list not found")

        val targetProduct = productList.packages?.find { it.code == targetCode }
            ?: productList.test_profiles?.find { it.code == targetCode }
            ?: productList.tests?.find { it.code == targetCode }
            ?: throw AssertionError("Product with code $targetCode not found in API response")

        val rawPrice = when (targetProduct) {
            is model.LabTestPackage -> targetProduct.product?.price?.toDoubleOrNull() ?: 0.0
            is model.LabTestProfile -> targetProduct.product?.price?.toDoubleOrNull() ?: 0.0
            is model.LabTestItem -> targetProduct.product?.price?.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        logger.info { "Verifying price details on address selection page..." }
        StepHelper.step("Verifying price details on address selection page...")
        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)

        logger.info { "Verifying footer actions on address selection page..." }
        StepHelper.step("Verifying footer actions on address selection page...")
        testSchedulingPage.verifyFooterActions()

        logger.info { "Clicking Proceed and navigating to Slot Selection page..." }
        StepHelper.step("Clicking Proceed and navigating to Slot Selection page...")
        testSchedulingPage.clickProceed()

        logger.info { "Verifying Slot Selection Page items..." }
        StepHelper.step("Verifying Slot Selection Page items...")
        // Assuming 'id' is available in the common interface or checking the specific types
        val productId = when (targetProduct) {
            is model.LabTestPackage -> targetProduct.product?.product_id
            is model.LabTestProfile -> targetProduct.product?.product_id
            is model.LabTestItem -> targetProduct.product?.product_id
            else -> throw RuntimeException("Unknown product type")
        }
        testSchedulingPage.verifySlotSelectionPage(code = targetCode, productId = productId)

        logger.info { "Verifying Price Details on Slot Selection page..." }
        StepHelper.step("Verifying Price Details on Slot Selection page...")
        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)

        logger.info { "Verifying Footer Actions on Slot Selection page..." }
        StepHelper.step("Verifying Footer Actions on Slot Selection page...")
        testSchedulingPage.verifyFooterActions()
        testSchedulingPage.clickProceed()
        testSchedulingPage.verifyOrderSummaryPage(expectedSubtotal = rawPrice, expectedDiscount = 0.0)
        
        // Finalize the order automation by calling the workflow API
        testSchedulingPage.callAutomateOrderWorkflow(isKit = false)

        logger.info { "Test completed successfully." }
        StepHelper.step("Test completed successfully.")
    }
    @Test
    @Order(5)
    fun `verify summary page edit flow`() {
        val targetCode = "P037"
        logger.info { "Starting test: verify summary page edit flow" }
        StepHelper.step("Starting test: verify summary page edit flow")

        labTestsPage.labTestData ?: throw AssertionError("Failed to capture Lab Test API response")
        labTestsPage.clickViewDetails(targetCode)

        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)
        val testSchedulingPage = TestSchedulingPage(page)

        testSchedulingPage.captureAddressData {
            testDetailPage.clickBookNow(targetCode)
        }

        // Initial setup to reach summary page
        val responseObj = labTestsPage.labTestData ?: throw AssertionError("Failed to capture Lab Test API response")
        val productList = responseObj.data?.diagnostic_product_list ?: throw AssertionError("diagnostic_product_list not found")
        val targetProduct = productList.packages?.find { it.code == targetCode }
            ?: productList.test_profiles?.find { it.code == targetCode }
            ?: productList.tests?.find { it.code == targetCode }
            ?: throw AssertionError("Product with code $targetCode not found in API response")

        val productId = when (targetProduct) {
            is model.LabTestPackage -> targetProduct.product?.product_id
            is model.LabTestProfile -> targetProduct.product?.product_id
            is model.LabTestItem -> targetProduct.product?.product_id
            else -> throw RuntimeException("Unknown product type")
        }

        testSchedulingPage.clickProceed()
        testSchedulingPage.verifySlotSelectionPage(code = targetCode, productId = productId)
        testSchedulingPage.clickProceed()

        // 1. Test Address Edit from Summary
        logger.info { "Editing address from summary..." }
        testSchedulingPage.clickEditAddressFromSummary()
        
        val addressCount = testSchedulingPage.getAddressCount()
        val randomIndex = (0 until addressCount).random()
        logger.info { "Selecting random address at index $randomIndex" }
        StepHelper.step("Selecting random address at index $randomIndex")
        testSchedulingPage.editUserAddress(randomIndex)
        
        testSchedulingPage.clickProceed() // go to slot selection
        testSchedulingPage.verifySlotSelectionPage(code = targetCode, productId = productId)
        testSchedulingPage.clickProceed() // go to summary

        // 2. Test Slot Edit from Summary
        logger.info { "Editing slot from summary..." }
        testSchedulingPage.clickEditSlotFromSummary()
        testSchedulingPage.verifySlotSelectionPage(code = targetCode, productId = productId)
        testSchedulingPage.clickProceed()

        // Final Verification
        logger.info { "Final verification on summary page..." }
        StepHelper.step("Final verification on summary page...")
        // Re-extract price for validation
        // We already extracted targetProduct and productId earlier in this test
        val rawPrice = when (targetProduct) {
            is model.LabTestPackage -> targetProduct.product?.price?.toDoubleOrNull() ?: 0.0
            is model.LabTestProfile -> targetProduct.product?.price?.toDoubleOrNull() ?: 0.0
            is model.LabTestItem -> targetProduct.product?.price?.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        testSchedulingPage.verifyOrderSummaryPage(expectedSubtotal = rawPrice, expectedDiscount = 0.0)
        logger.info { "Edit flow test completed successfully." }
        StepHelper.step("Edit flow test completed successfully.")
    }
}
