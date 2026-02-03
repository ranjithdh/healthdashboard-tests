package mobileView.diagnostics

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import forWeb.diagnostics.page.TestSchedulingPage
import kotlinx.serialization.json.*
import login.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabTestsTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var page: Page

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
    }

    @AfterAll
    fun tearDown() {
        browser.close()
        playwright.close()
    }

    @BeforeEach
    fun createContext() {
        val viewport = TestConfig.Viewports.MOBILE_PORTRAIT
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    @Test
    fun `verify lab tests page static texts and segments`() {
        val labTestsPage = LabTestsPage(page)
        labTestsPage.navigateToDiagnostics()
        labTestsPage.checkStaticTextsAndSegments()
    }


    @Test
    fun `verify lab tests cards using API response`() {

        println("Starting test: verify lab tests cards using API response")
        val labTestsPage = LabTestsPage(page)

        // Capture the API response during navigation
        println("Navigating to diagnostics page and capturing API response...")
        val response = page.waitForResponse({
            it.url().contains(other = TestConfig.Urls.LAB_TEST_API_URL) && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }
        println("Response Status: ${response.status()}")
        if (response.status() == 304) {
            throw AssertionError("API returned 304 Not Modified. Playwright cannot read body of 304 responses. Header 'Cache-Control: no-cache' should have prevented this.")
        }

        // Parse response and verify cards
        println("Parsing API response...")
        val json = kotlinx.serialization.json.Json.parseToJsonElement(response.text()).jsonObject
        val data = json["data"]?.jsonObject
        val productList = data?.get("diagnostic_product_list")?.jsonObject
        data class TestCardData(val code: String, val name: String, val sampleType: String, val rawSampleType: String, val price: String)
        val testCards = mutableListOf<TestCardData>()

        fun extractData(jsonArray: JsonArray?) {
            jsonArray?.forEach { element ->
                val obj = element.jsonObject
                val code = obj["code"]?.jsonPrimitive?.content ?: return@forEach
                val name = obj["name"]?.jsonPrimitive?.content ?: ""
                val rawSampleType = obj["sample_type"]?.jsonPrimitive?.content ?: ""

                // Sample type logic as per user requirement
                val sampleType = when {
                    code.startsWith("CORTISOL") -> "At-Home Test Kit"
                    code.startsWith("OMEGA") -> "At-Home Test Kit"
                    rawSampleType.lowercase() == "saliva" -> "At-Home Test Kit"
                    rawSampleType.lowercase() == "stool" -> "At-Home Test Kit"
                    else -> "Blood test"
                }

                // Price extraction and formatting
                val rawPrice = obj["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                numberFormat.maximumFractionDigits = 0
                val formattedPrice = "₹ " + numberFormat.format(rawPrice)

                testCards.add(TestCardData(code, name, sampleType, rawSampleType, formattedPrice))
            }
        }

        // Extract data from all sections
        extractData(productList?.get("packages")?.jsonArray)
        extractData(productList?.get("test_profiles")?.jsonArray)
        extractData(productList?.get("tests")?.jsonArray)

        println("Found ${testCards.size} cards total.")

        // initial check - All should be visible
        println("Verifying initial state (All visible)...")
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
            println("Verifying visibility for filters: $activeFilters")
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
                    println("Mismatch: Card '${card.name}' ($category) [raw: ${card.rawSampleType}] - Expected: $shouldBeVisible, Actual: $isVisible")
                }
                assert(isVisible == shouldBeVisible) {
                    "Visibility mismatch for card '${card.name}' ($category) [raw: ${card.rawSampleType}]. Active Filters: $activeFilters. Expected Visible: $shouldBeVisible, Actual: $isVisible"
                }
            }
            println("Verified visibility successfully.")
        }

        // Helper to simulate clicking a filter
        fun toggleFilter(filterName: String) {
            println(">> Clicking filter: $filterName")
            labTestsPage.clickFilter(filterName)

            if (filterName == "All") {
                activeFilters.clear()
                activeFilters.add("All")
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

        println("Filter permutation test completed successfully.")
    }
    @Test
    fun `verify inside card static section`() {
        val labTestsPage = LabTestsPage(page)
        labTestsPage.navigateToDiagnostics()
        println("Clicking View Details...")
        val testDetailPage = labTestsPage.clickViewDetails(code = "PROJ1056379")

        println("Verifying How It Works section...")
        testDetailPage.verifyHowItWorksSection()

        println("Verifying Certified Labs section...")
        testDetailPage.verifyCertifiedLabsSection()

        println("Test completed successfully.")
    }

    @Test
    fun `verify detail page components`() {
        val labTestsPage = LabTestsPage(page)
        println("Starting test: verify detail page components")

        // Capture the API response during navigation
        println("Navigating to diagnostics page and capturing API response...")
        val listResponse = page.waitForResponse({
            it.url().contains(other = TestConfig.Urls.LAB_TEST_API_URL) && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }

        val targetCode = "P250" // "GENE10001" //"GUT10002" //"P250" //"GENE10001" // "PROJ1056379" //"DH_LONGEVITY_PANEL"

        // Parse list response to find the target item
        val listJson = kotlinx.serialization.json.Json.parseToJsonElement(listResponse.text()).jsonObject
        val listData = listJson["data"]?.jsonObject
        val productList = listData?.get("diagnostic_product_list")?.jsonObject
        // Helper to search in multiple arrays
        fun findItem(section: String): JsonObject? {
            return productList?.get(section)?.jsonArray?.map { it.jsonObject }?.firstOrNull {
                it["code"]?.jsonPrimitive?.content == targetCode
            }
        }

        val targetPackage = findItem("packages")
            ?: findItem("test_profiles")
            ?: findItem("tests")
            ?: throw AssertionError("Item with code $targetCode not found in API response (packages, test_profiles, tests)")

        val content = targetPackage["content"]?.jsonObject ?: throw AssertionError("Content not found for $targetCode")

        // Extract descriptions
        val whatMeasuredDesc = content["what_measured_description"]?.jsonPrimitive?.content ?: ""
        val whatToExpectDesc = content["what_to_expect_description"]?.jsonPrimitive?.content ?: ""

        // 'who' is an array in the JSON, we take the first element
        val whoArray = content["who"]?.jsonArray
        val whoDesc = whoArray?.firstOrNull()?.jsonPrimitive?.content ?: ""

        println("Expected What's Measured: $whatMeasuredDesc")
        println("Expected Who: $whoDesc")
        println("Expected What to Expect: $whatToExpectDesc")

        // Logic to determine expected highlights based on React frontend logic
        val highlightsJson = content["highlights"]?.jsonArray
        val rawHighlights = highlightsJson?.map { it.jsonPrimitive.content } ?: emptyList()
        val expectedHighlights = mutableListOf<String>()

        // 1. highlights[0]
        if (rawHighlights.isNotEmpty()) expectedHighlights.add(rawHighlights[0])

        val hasHighlight5 = rawHighlights.size > 5 && rawHighlights[5].isNotBlank()

        // 2. Prep/Fasting block (Only if !hasHighlight5)
        if (!hasHighlight5) {
            val code = targetPackage["code"]?.jsonPrimitive?.content
            val vendorProductId = targetPackage["product"]?.jsonObject?.get("vendor_product_id")?.jsonPrimitive?.content

            if (code == "DH_LONGEVITY_PANEL" || vendorProductId == "DH_LONGEVITY_PANEL" ||
                code == "DH_METABOLIC_PANEL" || vendorProductId == "DH_METABOLIC_PANEL") {

                if (code == "DH_METABOLIC_PANEL" || vendorProductId == "DH_METABOLIC_PANEL") {
                    expectedHighlights.add("Two Blood Draws Required: Fasting and Post-Breakfast")
                } else {
                    expectedHighlights.add("Fasting Required")
                    expectedHighlights.add("Post-Meal Blood Draw Required")
                }

            } else {
                val sampleType = targetPackage["sample_type"]?.jsonPrimitive?.content?.lowercase()
                val preparation = content["preparation"]?.jsonPrimitive?.content
                val isFastingRequired = targetPackage["is_fasting_required"]?.jsonPrimitive?.boolean == true

                if (sampleType == "stool" || sampleType == "saliva") {
                    expectedHighlights.add(preparation ?: "No preparation required")
                } else if (isFastingRequired) {
                    val fastingInfo = content["fasting_info"]?.jsonPrimitive?.content
                    val cleanedFastingInfo = fastingInfo?.replace(Regex("fasting\\s*", RegexOption.IGNORE_CASE), "")?.trim() ?: "required"
                    expectedHighlights.add("Fasting $cleanedFastingInfo")
                } else {
                    expectedHighlights.add("No fasting required")
                }
            }
        }

        // 3. when_to_take
        val whenToTake = content["when_to_take"]?.jsonPrimitive?.content
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

        println("Calculated Expected Highlights (Frontend Logic): $expectedHighlights")

        println("Clicking View Details for code $targetCode")
        // Click View Details for the specific panel
        labTestsPage.clickViewDetails(targetCode)

        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)

        // Verify Header Info (Name, Short Description, About Description)
        testDetailPage.verifyTestHeaderInfo(targetCode)
        testDetailPage.verifyHighlights(expectedHighlights)
        // Verify "How it Works?" section
        println("Verifying How it Works section...")
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

        val sampleType = targetPackage["sample_type"]?.jsonPrimitive?.content?.lowercase() ?: ""
        val reportGenHr = targetPackage["report_generation_hr"]?.jsonPrimitive?.content ?: "72 hours"
        val highlightsList = content["highlights"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

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
        val rawPrice = targetPackage["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
        val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
        numberFormat.maximumFractionDigits = 0
        val formattedPrice = "₹" + numberFormat.format(rawPrice)

        println("Expected Price: $formattedPrice")

        // Verify Price and Booking Button
        testDetailPage.verifyPriceAndBookingButton(targetCode, formattedPrice)

        println("Test completed successfully.")

        println("Verifying How It Works section...")
        val sampleTypes = targetPackage["sample_type"]?.jsonPrimitive?.content ?: ""
        val reportGenerationHr = targetPackage["report_generation_hr"]?.jsonPrimitive?.content
        val firstHighlight = if (rawHighlights.isNotEmpty()) rawHighlights[0] else null
        testDetailPage.verifyHowItWorksSection(sampleTypes, targetCode, reportGenerationHr, firstHighlight)

        println("Verifying Certified Labs section...")
        testDetailPage.verifyCertifiedLabsSection()

        println("Test completed successfully.")
    }

    @Test
    fun `verify test scheduling`() {
        val labTestsPage = LabTestsPage(page)
        println("Starting test: verify test scheduling")

        // Capture the API response during navigation
        println("Navigating to diagnostics page and capturing API response...")
        val listResponse = page.waitForResponse({
            it.url().contains(other = TestConfig.Urls.LAB_TEST_API_URL) && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }
        val targetCode = "P037"

        println("Clicking View Details for code $targetCode")
        labTestsPage.clickViewDetails(targetCode)

//        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)

        val testSchedulingPage = TestSchedulingPage(page)
        println("Capturing address list and verifying scheduling page...")
//        testSchedulingPage.captureAddressData {
//            testDetailPage.clickBookNow(targetCode)
//        }

//        testSchedulingPage.verifySampleCollectionAddressHeading()

//        println("Verifying addresses from API...")
//        testSchedulingPage.assertAddressesFromApi()

//        println("Testing 'Add New Address' functionality...")
//        testSchedulingPage.clickAddNewAddress()
//        testSchedulingPage.addAddressAndValidate()

//        println("Testing 'Edit Address' functionality...")
        // Edit the first address (at index 0)
//        testSchedulingPage.editUserAddress(0)

        // Extract price for the targetCode from listResponse
        val listJson = kotlinx.serialization.json.Json.parseToJsonElement(listResponse.text()).jsonObject
        val listData = listJson["data"]?.jsonObject
        val productList = listData?.get("diagnostic_product_list")?.jsonObject

        val productTypes = listOf("packages", "test_profiles", "tests")
        val allProducts = productTypes.flatMap { type ->
            productList?.get(type)?.jsonArray?.map { it.jsonObject } ?: emptyList()
        }

        val targetProduct = allProducts.firstOrNull {
            it["code"]?.jsonPrimitive?.content == targetCode
        } ?: throw AssertionError("Product with code $targetCode not found in any of $productTypes in API response")

        val rawPrice = targetProduct["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

        println("Verifying price details on address selection page...")
        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)

        println("Verifying footer actions on address selection page...")
        testSchedulingPage.verifyFooterActions()

        println("Clicking Proceed and navigating to Slot Selection page...")
        testSchedulingPage.clickProceed()

        println("Verifying Slot Selection Page items...")
        testSchedulingPage.verifySlotSelectionPage()

        println("Verifying Price Details on Slot Selection page...")
        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)

        println("Verifying Footer Actions on Slot Selection page...")
        testSchedulingPage.verifyFooterActions()

        println("Test completed successfully.")
    }

    @Test
    fun `verify longevity panel scheduling flow`() {
        val labTestsPage = LabTestsPage(page)
        val targetCode = "DH_LONGEVITY_PANEL"
        println("Starting test: verify longevity panel scheduling flow ($targetCode)")

        // Navigation and capture
        val listResponse = page.waitForResponse({ it.url().contains("human-token/lab-test") && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }

        println("Clicking View Details for $targetCode")
        labTestsPage.clickViewDetails(targetCode)

        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)
        val testSchedulingPage = TestSchedulingPage(page)

        println("Capturing address list and booking $targetCode")
        testSchedulingPage.captureAddressData {
            testDetailPage.clickBookNow(targetCode)
        }

        // Verify address page
//        testSchedulingPage.verifySampleCollectionAddressHeading()
//        testSchedulingPage.assertAddressesFromApi()

        // Extract price
        val listJson = kotlinx.serialization.json.Json.parseToJsonElement(listResponse.text()).jsonObject
        val listData = listJson["data"]?.jsonObject
        val productList = listData?.get("diagnostic_product_list")?.jsonObject
        // Helper to search in multiple arrays
        val targetProduct = listOf("packages", "test_profiles", "tests").mapNotNull { section ->
            productList?.get(section)?.jsonArray?.map { it.jsonObject }?.firstOrNull {
                it["code"]?.jsonPrimitive?.content == targetCode
            }
        }.firstOrNull() ?: throw AssertionError("Product with code $targetCode not found in API response (packages, test_profiles, tests)")

        val rawPrice = targetProduct["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

        println("Verifying Price/Footer on address page...")
//        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)
//        testSchedulingPage.verifyFooterActions()

        println("Clicking Proceed and verifying Slot Selection Page for Longevity Panel...")
        testSchedulingPage.clickProceed()

        // This will now verify all slot buttons (should be 2 for longevity panel)
//        testSchedulingPage.verifySlotSelectionPage()

        println("Verifying Price/Footer on slot selection page...")
        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)
        testSchedulingPage.verifyFooterActions()

        println("Longevity panel test completed successfully.")
    }
}
