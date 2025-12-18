package forWeb.diagnostics.page

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import model.LabTestResponse
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

/**
 * Lab Tests Page - handles interactions with the diagnostics/lab tests page
 */
class LabTestsPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.DIAGNOSTICS_PATH

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = true
        encodeDefaults = true
    }

    private var labTestData: LabTestResponse? = null

    /**
     * Wait for page to load and verify key elements
     */
    fun waitForPageLoad(): LabTestsPage {
        byText("Book Lab Tests").waitFor()
        logger.info { "Lab Tests page loaded" }
        return this
    }

    fun waitForConfirmation() {
        // Use waitForResponse with callback (matching HomePage.kt pattern)
        // This will capture the API response that comes during URL wait
        try {
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()
                        ?.contains(TestConfig.Urls.LAB_TEST_API_URL) == true && response.status() == 200
                },
                {
                    // Callback: wait for diagnostics URL
                    // The API response will come during this wait
                    page.waitForURL("https://app.stg.deepholistics.com/diagnostics")
                }
            )

            // Process the captured response
            val responseBodyBytes = response.body()
            if (responseBodyBytes != null && responseBodyBytes.isNotEmpty()) {
                val responseBody = String(responseBodyBytes)
                try {
                    val responseObj = json.decodeFromString<LabTestResponse>(responseBody)
                    labTestData = responseObj
                    logger.info { "API data captured during waitForConfirmation" }
                } catch (e: Exception) {
                    logger.warn { "Failed to parse API response: ${e.message}" }
                }
            }
        } catch (e: Exception) {
            logger.warn { "Could not capture API response in waitForConfirmation: ${e.message}" }
            // Fallback: ensure URL is loaded even if API capture failed
            page.waitForURL("https://app.stg.deepholistics.com/diagnostics")
        }
    }

    /**
     * Process API response that was captured externally (e.g., via onResponse listener)
     */
    fun processApiResponse(response: Response) {
        try {
            val responseBodyBytes = response.body()
            if (responseBodyBytes != null && responseBodyBytes.isNotEmpty()) {
                val responseBody = String(responseBodyBytes)
                val responseObj = json.decodeFromString<LabTestResponse>(responseBody)
                labTestData = responseObj
                logger.info { "API data processed from captured response" }
            }
        } catch (e: Exception) {
            logger.warn { "Failed to process captured API response: ${e.message}" }
        }
    }

    /**
     * Fetch lab test data from API
     * Returns cached data if available, otherwise waits for API response
     */
    fun fetchLabTestDataFromApi(): LabTestResponse? {
        // Return cached data if already fetched
        if (labTestData != null) {
            logger.info { "Using cached API data" }
            return labTestData
        }

        // Wait for API response using Runnable callback pattern (matching HomePage.kt)
        try {
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()
                        ?.contains(TestConfig.Urls.LAB_TEST_API_URL) == true && response.status() == 200
                },
                {
                    // Callback - wait for diagnostics URL to ensure page is loaded
                    page.waitForURL(TestConfig.Urls.DIAGNOSTICS_URL)
                }
            )

            val responseBodyBytes = response.body()
            if (responseBodyBytes == null || responseBodyBytes.isEmpty()) {
                logger.info { "API response body is empty" }
                return null
            }

            logger.info { "API response received" }

            try {
                val responseBody = String(responseBodyBytes)
                val responseObj = json.decodeFromString<LabTestResponse>(responseBody)
                labTestData = responseObj
                return responseObj
            } catch (e: Exception) {
                logger.error { "Failed to parse API response: ${e.message}" }
                return null
            }
        } catch (e: Exception) {
            logger.warn { "Could not wait for API response (may have already been received): ${e.message}" }
            // If waitForResponse times out, the response may have already been received
            // In this case, we'll return null and the test should handle it gracefully
            return null
        }
    }

    /**
     * Wait for test panels to load
     */
//    fun waitForTestPanelsToLoad(): LabTestsPage {
//        page.waitForSelector("[data-testid='test-panel'], .test-card, [class*='test-card']",
//            Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
//        return this
//    }
    /**
     * Wait for test panels to load - waits for filter switches instead
     */
//    fun waitForTestPanelsToLoad(): LabTestsPage {
//        // Wait for filter switches to be visible (they're always on the page)
//        byRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("All")).waitFor()
//        logger.info { "Filter switches are ready" }
//        return this
//    }
    /**
     * Wait for test panels to load and fetch API data
     */
    fun waitForTestPanelsToLoad(): LabTestsPage {
        // Wait for filter switches instead - they're more reliable
        byRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("All")).waitFor()

        // Fetch API data during page load (before it times out)
        // This ensures we capture the API response that happens during initial page load
        try {
            fetchLabTestDataFromApi()
        } catch (e: Exception) {
            logger.warn { "Could not fetch API data during page load: ${e.message}" }
        }

        // Optionally check for test panels, but don't fail if they don't exist
        try {
            page.waitForSelector("[data-testid='test-panel'], .test-card, [class*='test-card']",
                Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000.0)) // Shorter timeout
        } catch (e: Exception) {
            logger.warn { "Test panels selector not found, but continuing..." }
        }
        return this
    }

    // ---------------------- Page Element Interactions ----------------------

    /**
     * Click on "Book Lab Tests" heading
     */
    fun clickBookLabTestsHeading(): LabTestsPage {
        logger.info { "Clicking 'Book Lab Tests' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Book Lab Tests")).click()
        return this
    }

    /**
     * Click on search textbox
     */
    fun clickSearchTextBox(): LabTestsPage {
        logger.info { "Clicking search textbox" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in lab tests")).click()
        return this
    }

    /**
     * Enter search query
     */
    fun enterSearchQuery(query: String): LabTestsPage {
        logger.info { "Entering search query: $query" }
        val searchInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in lab tests"))
        searchInput.fill(query)
        return this
    }

    /**
     * Click on "Get tested from the comfort" heading
     */
    fun clickGetTestedHeading(): LabTestsPage {
        logger.info { "Clicking 'Get tested from the comfort' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Get tested from the comfort")).click()
        return this
    }

    /**
     * Click on paragraph with "With flexible testing options" text
     */
    fun clickFlexibleTestingOptionsParagraph(): LabTestsPage {
        logger.info { "Clicking paragraph with 'With flexible testing options'" }
        byRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("With flexible testing options")).click()
        return this
    }

    /**
     * Click filter switch (All, Blood, Gene, Gut, Recommended for You)
     */
    fun clickFilterSwitch(filterName: String): LabTestsPage {
        logger.info { "Clicking filter switch: $filterName" }
        byRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName(filterName)).click()
        return this
    }

    fun verifyFilterSelected(filterName: String): LabTestsPage {
        val filter = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName(filterName)
        )
        Assertions.assertEquals(
            "true",
            filter.getAttribute("aria-pressed"),
            "$filterName filter should be active"
        )
        return this
    }

    fun clickFilter(filterName: String): LabTestsPage {
        val filter = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName(filterName)
        )
        filter.waitFor()
        filter.click()
        return this
    }
    // ---------------------- Longevity Panel Functions ----------------------

    /**
     * Click on first image (Longevity Panel featured card)
     */
    fun clickLongevityPanelImage(): LabTestsPage {
        logger.info { "Clicking Longevity Panel image" }
        page.locator("img").first().click()
        return this
    }

    /**
     * Click on "102 BIOMARKERS" text
     */
    fun click102Biomarkers(): LabTestsPage {
        logger.info { "Clicking '102 BIOMARKERS' text" }
        byText("102 BIOMARKERS").click()
        return this
    }

    /**
     * Click on paragraph with "Longevity Panel" text
     */
    fun clickLongevityPanelParagraph(): LabTestsPage {
        logger.info { "Clicking paragraph with 'Longevity Panel'" }
        byRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("Longevity Panel")).click()
        return this
    }

    /**
     * Click on "Give your family the same" text
     */
    fun clickGiveYourFamilyTheSame(): LabTestsPage {
        logger.info { "Clicking 'Give your family the same' text" }
        byText("Give your family the same").click()
        return this
    }

    /**
     * Click Book Now button for Longevity Panel using test ID
     */
    fun clickLongevityBookNow(): LabTestsPage {
        logger.info { "Clicking Longevity Panel Book Now button" }
        byTestId("longevity-card-book-now").click()
        return this
    }

    /**
     * Click on rounded-lg element (back button or close)
     */
    fun clickRoundedLgFirst(): LabTestsPage {
        logger.info { "Clicking first rounded-lg element" }
        page.locator(".rounded-lg").first().click()
        return this
    }

    /**
     * Click on "Longevity Panel" heading
     */
    fun clickLongevityPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Longevity Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Longevity Panel")).click()
        return this
    }

    /**
     * Click on Longevity Panel description text
     */
    fun clickLongevityPanelDescription(): LabTestsPage {
        logger.info { "Clicking Longevity Panel description" }
        byText("A comprehensive blood test that measures over 100 biomarkers across organs,").click()
        return this
    }

    /**
     * Click on Longevity Panel price "₹9,999"
     */
    fun clickLongevityPanelPrice(): LabTestsPage {
        logger.info { "Clicking Longevity Panel price ₹9,999" }
        byText("₹9,999").click()
        return this
    }

    /**
     * Click View Details button for Longevity Panel (first)
     */
    fun clickLongevityPanelViewDetails(): LabTestsPage {
        logger.info { "Clicking View Details for Longevity Panel" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View Details")).first().click()
        return this
    }

    /**
     * Click on Longevity Panel image by role
     */
    fun clickLongevityPanelImageByRole(): LabTestsPage {
        logger.info { "Clicking Longevity Panel image by role" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Longevity Panel")).click()
        return this
    }

    /**
     * Click on grid item (nth-child(3))
     */
    fun clickGridItem3(): LabTestsPage {
        logger.info { "Clicking grid item 3" }
        page.locator(".grid > div:nth-child(3)").click()
        return this
    }

    // ---------------------- Advanced Thyroid Panel Functions ----------------------

    /**
     * Click on Advanced Thyroid Panel image
     */
    fun clickAdvancedThyroidPanelImage(): LabTestsPage {
        logger.info { "Clicking Advanced Thyroid Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Advanced Thyroid Panel")).click()
        return this
    }

    /**
     * Click on "Recommended for you" tag (nth(1))
     */
    fun clickRecommendedForYouNth(index: Int): LabTestsPage {
        logger.info { "Clicking 'Recommended for you' tag at index: $index" }
        byText("Recommended for you").nth(index).click()
        return this
    }

    /**
     * Click on "Advanced Thyroid Panel" heading
     */
    fun clickAdvancedThyroidPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Advanced Thyroid Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Thyroid Panel")).click()
        return this
    }

    /**
     * Click on "Blood test" text (nth(2))
     */
    fun clickBloodTestNth(index: Int): LabTestsPage {
        logger.info { "Clicking 'Blood test' at index: $index" }
        byText("Blood test").nth(index).click()
        return this
    }

    /**
     * Click on "An advanced at-home antibody" text
     */
    fun clickAdvancedAtHomeAntibody(): LabTestsPage {
        logger.info { "Clicking 'An advanced at-home antibody' text" }
        byText("An advanced at-home antibody").click()
        return this
    }

    /**
     * Click on Advanced Thyroid Panel price "₹1,499"
     */
    fun clickAdvancedThyroidPanelPrice(): LabTestsPage {
        logger.info { "Clicking Advanced Thyroid Panel price ₹1,499" }
        byText("₹1,499").click()
        return this
    }

    /**
     * Click View Details button by index
     */
    fun clickViewDetailsByIndex(index: Int): LabTestsPage {
        logger.info { "Clicking View Details button at index: $index" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View Details")).nth(index).click()
        return this
    }

    // ---------------------- Autoimmune Panel Functions ----------------------

    /**
     * Click on Autoimmune Panel image
     */
    fun clickAutoimmunePanelImage(): LabTestsPage {
        logger.info { "Clicking Autoimmune Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Autoimmune Panel")).click()
        return this
    }

    /**
     * Click on "Autoimmune Panel" heading
     */
    fun clickAutoimmunePanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Autoimmune Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Autoimmune Panel")).click()
        return this
    }

    /**
     * Click on Autoimmune Panel description
     */
    fun clickAutoimmunePanelDescription(): LabTestsPage {
        logger.info { "Clicking Autoimmune Panel description" }
        byText("A comprehensive at-home test that screens for autoimmune activity by measuring").click()
        return this
    }

    /**
     * Click on price by nth index
     */
    fun clickPriceByNth(index: Int): LabTestsPage {
        logger.info { "Clicking price at index: $index" }
        byText("₹").nth(index).click()
        return this
    }

    // ---------------------- Advanced Genetic Analysis Functions ----------------------

    /**
     * Click on Advanced Genetic Analysis image
     */
    fun clickAdvancedGeneticAnalysisImage(): LabTestsPage {
        logger.info { "Clicking Advanced Genetic Analysis image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Advanced Genetic Analysis")).click()
        return this
    }

    /**
     * Click on "Advanced Genetic Analysis" heading
     */
    fun clickAdvancedGeneticAnalysisHeading(): LabTestsPage {
        logger.info { "Clicking 'Advanced Genetic Analysis' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Genetic Analysis")).click()
        return this
    }

    /**
     * Click on "Cheek swab test" text
     */
    fun clickCheekSwabTest(): LabTestsPage {
        logger.info { "Clicking 'Cheek swab test' text" }
        byText("Cheek swab test").click()
        return this
    }

    /**
     * Click on "A cutting-edge cheek swab DNA" text
     */
    fun clickCuttingEdgeCheekSwabDNA(): LabTestsPage {
        logger.info { "Clicking 'A cutting-edge cheek swab DNA' text" }
        byText("A cutting-edge cheek swab DNA").click()
        return this
    }

    /**
     * Click on Advanced Genetic Analysis price "₹14,999"
     */
    fun clickAdvancedGeneticAnalysisPrice(): LabTestsPage {
        logger.info { "Clicking Advanced Genetic Analysis price ₹14,999" }
        byText("₹14,999").click()
        return this
    }

    // ---------------------- Advanced Gut Microbiome Analysis Functions ----------------------

    /**
     * Click on Advanced Gut Microbiome image
     */
    fun clickAdvancedGutMicrobiomeImage(): LabTestsPage {
        logger.info { "Clicking Advanced Gut Microbiome image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Advanced Gut Microbiome")).click()
        return this
    }

    /**
     * Click on "Advanced Gut Microbiome" heading
     */
    fun clickAdvancedGutMicrobiomeHeading(): LabTestsPage {
        logger.info { "Clicking 'Advanced Gut Microbiome' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Gut Microbiome")).click()
        return this
    }

    /**
     * Click on "Stool test" text (exact)
     */
    fun clickStoolTestExact(): LabTestsPage {
        logger.info { "Clicking 'Stool test' text (exact)" }
        page.getByText("Stool test", Page.GetByTextOptions().setExact(true)).click()
        return this
    }

    /**
     * Click on "An advanced at-home stool" text
     */
    fun clickAdvancedAtHomeStool(): LabTestsPage {
        logger.info { "Clicking 'An advanced at-home stool' text" }
        byText("An advanced at-home stool").click()
        return this
    }

    /**
     * Click on Advanced Gut Microbiome price "₹8,999"
     */
    fun clickAdvancedGutMicrobiomePrice(): LabTestsPage {
        logger.info { "Clicking Advanced Gut Microbiome price ₹8,999" }
        byText("₹8,999").click()
        return this
    }

    // ---------------------- Advanced Heart Health Panel Functions ----------------------

    /**
     * Click on Advanced Heart Health Panel image
     */
    fun clickAdvancedHeartHealthPanelImage(): LabTestsPage {
        logger.info { "Clicking Advanced Heart Health Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Advanced Heart Health Panel")).click()
        return this
    }

    /**
     * Click on "Advanced Heart Health Panel" heading
     */
    fun clickAdvancedHeartHealthPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Advanced Heart Health Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Heart Health Panel")).click()
        return this
    }

    /**
     * Click on Advanced Heart Health Panel description
     */
    fun clickAdvancedHeartHealthPanelDescription(): LabTestsPage {
        logger.info { "Clicking Advanced Heart Health Panel description" }
        byText("A comprehensive blood test that measures advanced cardiac biomarkers to assess").click()
        return this
    }

    // ---------------------- Essential Nutrients Panel Functions ----------------------

    /**
     * Click on Essential Nutrients Panel image
     */
    fun clickEssentialNutrientsPanelImage(): LabTestsPage {
        logger.info { "Clicking Essential Nutrients Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Essential Nutrients Panel")).click()
        return this
    }

    /**
     * Click on "Essential Nutrients Panel" heading
     */
    fun clickEssentialNutrientsPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Essential Nutrients Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Essential Nutrients Panel")).click()
        return this
    }

    /**
     * Click on "Blood test" text (exact, nth(4))
     */
    fun clickBloodTestExactNth(index: Int): LabTestsPage {
        logger.info { "Clicking 'Blood test' (exact) at index: $index" }
        page.getByText("Blood test", Page.GetByTextOptions().setExact(true)).nth(index).click()
        return this
    }

    /**
     * Click on Essential Nutrients Panel description
     */
    fun clickEssentialNutrientsPanelDescription(): LabTestsPage {
        logger.info { "Clicking Essential Nutrients Panel description" }
        byText("A focused blood test that measures key vitamins and minerals essential for").click()
        return this
    }

    /**
     * Click on Essential Nutrients Panel price "₹1,299" (nth(2))
     */
    fun clickEssentialNutrientsPanelPrice(): LabTestsPage {
        logger.info { "Clicking Essential Nutrients Panel price ₹1,299" }
        byText("₹1,299").nth(2).click()
        return this
    }

    /**
     * Click on View Details button in grid item
     */
    fun clickViewDetailsInGridItem(divIndex: Int): LabTestsPage {
        logger.info { "Clicking View Details in grid item: $divIndex" }
        page.locator("div:nth-child($divIndex) > .p-0 > .flex.flex-col.gap-8 > .flex > .inline-flex").click()
        return this
    }

    // ---------------------- Thyroid Health Panel Functions ----------------------

    /**
     * Click on Thyroid Health Panel image
     */
    fun clickThyroidHealthPanelImage(): LabTestsPage {
        logger.info { "Clicking Thyroid Health Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Thyroid Health Panel")).click()
        return this
    }

    /**
     * Click on "Thyroid Health Panel" heading
     */
    fun clickThyroidHealthPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Thyroid Health Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Thyroid Health Panel")).click()
        return this
    }

    /**
     * Click on "A targeted blood test that" text
     */
    fun clickTargetedBloodTestThat(): LabTestsPage {
        logger.info { "Clicking 'A targeted blood test that' text" }
        byText("A targeted blood test that").click()
        return this
    }

    /**
     * Click on Thyroid Health Panel price "₹749"
     */
    fun clickThyroidHealthPanelPrice(): LabTestsPage {
        logger.info { "Clicking Thyroid Health Panel price ₹749" }
        byText("₹749").click()
        return this
    }

    // ---------------------- Omega Profile Panel Functions ----------------------

    /**
     * Click on Omega Profile Panel image
     */
    fun clickOmegaProfilePanelImage(): LabTestsPage {
        logger.info { "Clicking Omega Profile Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Omega Profile Panel")).click()
        return this
    }

    /**
     * Click on "Omega Profile Panel" heading
     */
    fun clickOmegaProfilePanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Omega Profile Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Omega Profile Panel")).click()
        return this
    }

    /**
     * Click on "At-Home Test Kit" text (first)
     */
    fun clickAtHomeTestKitFirst(): LabTestsPage {
        logger.info { "Clicking 'At-Home Test Kit' text (first)" }
        byText("At-Home Test Kit").first().click()
        return this
    }

    /**
     * Click on "An advanced fatty acid blood" text
     */
    fun clickAdvancedFattyAcidBlood(): LabTestsPage {
        logger.info { "Clicking 'An advanced fatty acid blood' text" }
        byText("An advanced fatty acid blood").click()
        return this
    }

    /**
     * Click on Omega Profile Panel price "₹3,999"
     */
    fun clickOmegaProfilePanelPrice(): LabTestsPage {
        logger.info { "Clicking Omega Profile Panel price ₹3,999" }
        byText("₹3,999").click()
        return this
    }

    // ---------------------- Stress and Cortisol Rhythm Panel Functions ----------------------

    /**
     * Click on Stress and Cortisol Rhythm image
     */
    fun clickStressAndCortisolRhythmImage(): LabTestsPage {
        logger.info { "Clicking Stress and Cortisol Rhythm image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Stress and Cortisol Rhythm")).click()
        return this
    }

    /**
     * Click on "Stress and Cortisol Rhythm" heading
     */
    fun clickStressAndCortisolRhythmHeading(): LabTestsPage {
        logger.info { "Clicking 'Stress and Cortisol Rhythm' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Stress and Cortisol Rhythm")).click()
        return this
    }

    /**
     * Click on "At-Home Test Kit" text (nth(1))
     */
    fun clickAtHomeTestKitNth(index: Int): LabTestsPage {
        logger.info { "Clicking 'At-Home Test Kit' at index: $index" }
        byText("At-Home Test Kit").nth(index).click()
        return this
    }

    /**
     * Click on "A non-invasive saliva test" text
     */
    fun clickNonInvasiveSalivaTest(): LabTestsPage {
        logger.info { "Clicking 'A non-invasive saliva test' text" }
        byText("A non-invasive saliva test").click()
        return this
    }

    /**
     * Click on Stress and Cortisol Rhythm price "₹7,499"
     */
    fun clickStressAndCortisolRhythmPrice(): LabTestsPage {
        logger.info { "Clicking Stress and Cortisol Rhythm price ₹7,499" }
        byText("₹7,499").click()
        return this
    }

    // ---------------------- Liver Health Panel Functions ----------------------

    /**
     * Click on Liver Health Panel image
     */
    fun clickLiverHealthPanelImage(): LabTestsPage {
        logger.info { "Clicking Liver Health Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Liver Health Panel")).click()
        return this
    }

    /**
     * Click on "Liver Health Panel" heading
     */
    fun clickLiverHealthPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Liver Health Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Liver Health Panel")).click()
        return this
    }

    /**
     * Click on Liver Health Panel sample type
     */
    fun clickLiverHealthPanelSampleType(): LabTestsPage {
        logger.info { "Clicking Liver Health Panel sample type" }
        page.locator("div:nth-child(12) > .p-0 > .relative > .flex.flex-col > .text-muted-foreground").click()
        return this
    }

    /**
     * Click on Liver Health Panel description
     */
    fun clickLiverHealthPanelDescription(): LabTestsPage {
        logger.info { "Clicking Liver Health Panel description" }
        byText("A focused blood test that evaluates liver enzymes, proteins, and bilirubin").click()
        return this
    }

    /**
     * Click on Liver Health Panel price "₹1,299" (nth(3))
     */
    fun clickLiverHealthPanelPrice(): LabTestsPage {
        logger.info { "Clicking Liver Health Panel price ₹1,299" }
        byText("₹1,299").nth(3).click()
        return this
    }

    // ---------------------- Toxic Metals Panel Functions ----------------------

    /**
     * Click on Toxic Metals Panel image
     */
    fun clickToxicMetalsPanelImage(): LabTestsPage {
        logger.info { "Clicking Toxic Metals Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Toxic Metals Panel")).click()
        return this
    }

    /**
     * Click on "Toxic Metals Panel" heading
     */
    fun clickToxicMetalsPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Toxic Metals Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Toxic Metals Panel")).click()
        return this
    }

    /**
     * Click on Toxic Metals Panel sample type
     */
    fun clickToxicMetalsPanelSampleType(): LabTestsPage {
        logger.info { "Clicking Toxic Metals Panel sample type" }
        page.locator("div:nth-child(13) > .p-0 > .relative > .flex.flex-col > .text-muted-foreground").click()
        return this
    }

    /**
     * Click on Toxic Metals Panel description
     */
    fun clickToxicMetalsPanelDescription(): LabTestsPage {
        logger.info { "Clicking Toxic Metals Panel description" }
        byText("A comprehensive at-home test that screens for toxic and heavy metal exposure,").click()
        return this
    }

    /**
     * Click on Toxic Metals Panel price "₹2,499"
     */
    fun clickToxicMetalsPanelPrice(): LabTestsPage {
        logger.info { "Clicking Toxic Metals Panel price ₹2,499" }
        byText("₹2,499").click()
        return this
    }

    // ---------------------- Blood Health Panel Functions ----------------------

    /**
     * Click on Blood Health Panel image
     */
    fun clickBloodHealthPanelImage(): LabTestsPage {
        logger.info { "Clicking Blood Health Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Blood Health Panel")).click()
        return this
    }

    /**
     * Click on "Blood Health Panel" heading
     */
    fun clickBloodHealthPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Blood Health Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Blood Health Panel")).click()
        return this
    }

    /**
     * Click on Blood Health Panel sample type
     */
    fun clickBloodHealthPanelSampleType(): LabTestsPage {
        logger.info { "Clicking Blood Health Panel sample type" }
        page.locator("div:nth-child(14) > .p-0 > .relative > .flex.flex-col > .text-muted-foreground").click()
        return this
    }

    /**
     * Click on Blood Health Panel description
     */
    fun clickBloodHealthPanelDescription(): LabTestsPage {
        logger.info { "Clicking Blood Health Panel description" }
        byText("A complete blood panel that").click()
        return this
    }

    /**
     * Click on Blood Health Panel price "₹499"
     */
    fun clickBloodHealthPanelPrice(): LabTestsPage {
        logger.info { "Clicking Blood Health Panel price ₹499" }
        byText("₹499").click()
        return this
    }

    // ---------------------- Allergies Test Panel Functions ----------------------

    /**
     * Click on Allergies Test Panel image
     */
    fun clickAllergiesTestPanelImage(): LabTestsPage {
        logger.info { "Clicking Allergies Test Panel image" }
        byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Allergies Test Panel")).click()
        return this
    }

    /**
     * Click on "Allergies Test Panel" heading
     */
    fun clickAllergiesTestPanelHeading(): LabTestsPage {
        logger.info { "Clicking 'Allergies Test Panel' heading" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Allergies Test Panel")).click()
        return this
    }

    /**
     * Click on Allergies Test Panel sample type
     */
    fun clickAllergiesTestPanelSampleType(): LabTestsPage {
        logger.info { "Clicking Allergies Test Panel sample type" }
        page.locator("div:nth-child(15) > .p-0 > .relative > .flex.flex-col > .text-muted-foreground").click()
        return this
    }

    /**
     * Click on Allergies Test Panel description
     */
    fun clickAllergiesTestPanelDescription(): LabTestsPage {
        logger.info { "Clicking Allergies Test Panel description" }
        byText("A detailed IgE-based allergy").click()
        return this
    }

    /**
     * Click on Allergies Test Panel price "₹12,999"
     */
    fun clickAllergiesTestPanelPrice(): LabTestsPage {
        logger.info { "Clicking Allergies Test Panel price ₹12,999" }
        byText("₹12,999").click()
        return this
    }

    // ---------------------- Grid Item Click Functions ----------------------

    /**
     * Click on grid item by div index
     */
    fun clickGridItemByIndex(index: Int): LabTestsPage {
        logger.info { "Clicking grid item at index: $index" }
        page.locator("div:nth-child($index)").click()
        return this
    }

    /**
     * Click on grid element
     */
    fun clickGridElement(): LabTestsPage {
        logger.info { "Clicking grid element" }
        page.locator(".grid.grid-cols-12").click()
        return this
    }

    // ---------------------- Verification Functions ----------------------

    /**
     * Verify test panel name is visible
     */
    fun isTestPanelNameVisible(panelName: String): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(panelName)).isVisible
    }

    /**
     * Verify test panel image is visible
     */
    fun isTestPanelImageVisible(panelName: String): Boolean {
        return byRole(AriaRole.IMG, Page.GetByRoleOptions().setName(panelName)).isVisible
    }

    /**
     * Verify price is visible
     */
    fun isPriceVisible(price: String): Boolean {
        return byText(price).isVisible
    }

    /**
     * Get price text for a test panel
     */
    fun getPriceForPanel(panelName: String): String? {
        // Try to find price near the panel
        val panel = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(panelName))
        val parent = panel.locator("..")
        val priceText = parent.locator("text=/₹[0-9,]+/").first().textContent()
        return priceText
    }

    /**
     * Verify description text is visible
     */
    fun isDescriptionVisible(descriptionText: String): Boolean {
        return byText(descriptionText).isVisible
    }

    /**
     * Get test panel from API data by name
     */
    fun getTestPanelFromApi(panelName: String): model.LabTestPackage? {
        val apiData = labTestData ?: fetchLabTestDataFromApi()
        if (apiData == null || apiData.data == null) {
            return null
        }
        
        val packages = apiData.data.diagnostic_product_list?.packages ?: emptyList()
        return packages.find { it.name == panelName }
    }

    /**
     * Get test profile from API data by name
     */
    fun getTestProfileFromApi(profileName: String): model.LabTestProfile? {
        val apiData = labTestData ?: fetchLabTestDataFromApi()
        if (apiData == null || apiData.data == null) {
            return null
        }
        
        val profiles = apiData.data.diagnostic_product_list?.test_profiles ?: emptyList()
        return profiles.find { it.name == profileName }
    }

    /**
     * Verify panel price matches API data
     */
    fun verifyPanelPriceMatchesApi(panelName: String): Boolean {
        val packagePanel = getTestPanelFromApi(panelName)
        val profilePanel = getTestProfileFromApi(panelName)
        
        val apiPrice = when {
            packagePanel != null -> packagePanel.product?.price ?: ""
            profilePanel != null -> profilePanel.product?.price ?: ""
            else -> {
                logger.warn { "Panel '$panelName' not found in API data" }
                return false
            }
        }
        
        val displayedPrice = getPriceForPanel(panelName)
        
        if (displayedPrice == null) {
            logger.warn { "Price not found on page for '$panelName'" }
            return false
        }

        // Format API price to match displayed format (e.g., "1499.00" -> "₹1,499")
        val formattedApiPrice = formatPrice(apiPrice)
        val matches = displayedPrice.contains(formattedApiPrice) || formattedApiPrice.contains(displayedPrice.replace("₹", "").replace(",", ""))
        
        logger.info { "Panel: $panelName, API Price: $apiPrice, Displayed: $displayedPrice, Matches: $matches" }
        return matches
    }

    /**
     * Format price from API format to display format
     */
    private fun formatPrice(price: String): String {
        return try {
            val priceNum = price.replace(".00", "").toDoubleOrNull() ?: return price
            "₹${String.format("%,.0f", priceNum)}"
        } catch (e: Exception) {
            price
        }
    }

    /**
     * Verify panel description matches API data
     */
    fun verifyPanelDescriptionMatchesApi(panelName: String): Boolean {
        val packagePanel = getTestPanelFromApi(panelName)
        val profilePanel = getTestProfileFromApi(panelName)
        
        val apiDescription = when {
            packagePanel != null -> packagePanel.description ?: packagePanel.content?.short_description ?: ""
            profilePanel != null -> profilePanel.description ?: profilePanel.content?.short_description ?: ""
            else -> {
                logger.warn { "Panel '$panelName' not found in API data" }
                return false
            }
        }
        
        if (apiDescription.isBlank()) {
            return true // No description to verify
        }

        // Check if description text is visible on page
        val descriptionVisible = isDescriptionVisible(apiDescription) || 
                                 isDescriptionVisible(apiDescription.take(50)) // Check partial match
        
        logger.info { "Panel: $panelName, Description visible: $descriptionVisible" }
        return descriptionVisible
    }

    /**
     * Verify panel sample type matches API data
     */
    fun verifyPanelSampleTypeMatchesApi(panelName: String): Boolean {
        val packagePanel = getTestPanelFromApi(panelName)
        val profilePanel = getTestProfileFromApi(panelName)
        
        val apiSampleType = when {
            packagePanel != null -> packagePanel.sample_type ?: ""
            profilePanel != null -> profilePanel.sample_type ?: ""
            else -> {
                logger.warn { "Panel '$panelName' not found in API data" }
                return false
            }
        }
        
        if (apiSampleType.isBlank()) {
            return true
        }

        // Map API sample types to displayed text
        val sampleTypeMap = mapOf(
            "blood" to "Blood test",
            "saliva" to "Cheek swab test",
            "stool" to "Stool test",
            "dried_blood_spot" to "At-Home Test Kit",
            "saliva_stress" to "At-Home Test Kit"
        )

        val expectedText = sampleTypeMap[apiSampleType] ?: apiSampleType
        val isVisible = byText(expectedText).isVisible
        
        logger.info { "Panel: $panelName, API Sample Type: $apiSampleType, Expected: $expectedText, Visible: $isVisible" }
        return isVisible
    }

    // ---------------------- Helper Methods for Test Panels ----------------------

    /**
     * Get all test panels displayed on the page
     */
    fun getAllTestPanels(): List<Locator> {
        // Test panels are typically in cards or grid items
        // Adjust selector based on actual HTML structure
        return page.locator("[data-testid='test-panel'], .test-card, [class*='test-card'], [class*='test-panel'], [class*='grid'] > div").all()
    }

    /**
     * Get test panel by name
     */
    fun getTestPanelByName(testName: String): Locator? {
        return try {
            // Try to find by heading first, then by text
            val heading = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(testName))
            if (heading.isVisible) {
                return heading.locator("..")
            }
            page.locator("text=$testName").first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Click on a test panel by name
     */
    fun clickTestPanel(testName: String): LabTestsPage {
        logger.info { "Clicking on test panel: $testName" }
        val panel = getTestPanelByName(testName)
        panel?.click()
        return this
    }

    /**
     * Click "View Details" button for a test panel
     */
    fun clickViewDetails(testName: String): LabTestsPage {
        logger.info { "Clicking View Details for: $testName" }
        val panel = getTestPanelByName(testName)
        panel?.locator("button:has-text('View Details'), a:has-text('View Details')")?.click()
        return this
    }

    /**
     * Click filter button (All, Blood, Gene, Gut, Recommended for You)
     */
//    fun clickFilter(filterName: String): LabTestsPage {
//        logger.info { "Clicking filter: $filterName" }
//        clickFilterSwitch(filterName)
//        return this
//    }

    /**
     * Clear search query
     */
    fun clearSearch(): LabTestsPage {
        logger.info { "Clearing search" }
        val searchInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in lab tests"))
        searchInput.clear()
        return this
    }

    /**
     * Check if page title is visible
     */
    fun isPageTitleVisible(): Boolean {
        return isTestPanelNameVisible("Book Lab Tests")
    }

    /**
     * Check if test panel is visible by name
     */
    fun isTestPanelVisible(testName: String): Boolean {
        return getTestPanelByName(testName)?.isVisible ?: false
    }

    /**
     * Get test panel price by test name
     */
    fun getTestPanelPrice(testName: String): String? {
        val panel = getTestPanelByName(testName)
        return try {
            panel?.locator("text=/₹[0-9,]+/")?.first()?.textContent()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get test panel description by test name
     */
    fun getTestPanelDescription(testName: String): String? {
        val panel = getTestPanelByName(testName)
        return try {
            panel?.locator("[class*='description'], p")?.first()?.textContent()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get test panel type (Blood test, Cheek swab test, etc.) by test name
     */
    fun getTestPanelType(testName: String): String? {
        val panel = getTestPanelByName(testName)
        return try {
            panel?.locator("[class*='type'], [class*='sample-type']")?.first()?.textContent()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if "Book Now" button is visible for Longevity Panel
     */
    fun isBookNowButtonVisible(): Boolean {
        return byTestId("longevity-card-book-now").isVisible || 
               byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).isVisible
    }

    /**
     * Click "Book Now" button
     */
    fun clickBookNow(): LabTestsPage {
        logger.info { "Clicking Book Now button" }
        if (byTestId("longevity-card-book-now").isVisible) {
            clickLongevityBookNow()
        } else {
            byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).click()
        }
        return this
    }

    /**
     * Get count of visible test panels
     */
    fun getVisibleTestPanelsCount(): Int {
        return getAllTestPanels().count { it.isVisible }
    }

    /**
     * Verify API data matches displayed test panels
     */
    fun verifyTestPanelsFromApi(): Boolean {
        val apiData = fetchLabTestDataFromApi()
        if (apiData == null || apiData.data == null) {
            logger.error { "Failed to fetch API data" }
            return false
        }

        val packages = apiData.data.diagnostic_product_list?.packages ?: emptyList()
        val testProfiles = apiData.data.diagnostic_product_list?.test_profiles ?: emptyList()
        
        val allTestNames = mutableListOf<String>()
        packages.forEach { pkg -> pkg.name?.let { allTestNames.add(it) } }
        testProfiles.forEach { profile -> profile.name?.let { allTestNames.add(it) } }

        logger.info { "Found ${allTestNames.size} tests in API response" }

        // Verify each test from API is displayed on page
        var allVisible = true
        allTestNames.forEach { testName ->
            val isVisible = isTestPanelVisible(testName)
            if (!isVisible) {
                logger.warn { "Test panel '$testName' from API is not visible on page" }
                allVisible = false
            }
        }

        return allVisible
    }

    /**
     * Verify test panel details match API data
     */
    fun verifyTestPanelDetails(testName: String): Boolean {
        val apiData = labTestData ?: fetchLabTestDataFromApi()
        if (apiData == null || apiData.data == null) {
            return false
        }

        val packages = apiData.data.diagnostic_product_list?.packages ?: emptyList()
        val testProfiles = apiData.data.diagnostic_product_list?.test_profiles ?: emptyList()

        // Find test in packages first, then in test profiles
        val testPackage = packages.find { it.name == testName }
        val testProfile = testProfiles.find { it.name == testName }
        
        if (testPackage == null && testProfile == null) {
            logger.error { "Test '$testName' not found in API data" }
            return false
        }

        // Verify price matches
        val displayedPrice = getTestPanelPrice(testName)
        val apiPrice = when {
            testPackage != null -> testPackage.product?.price
            testProfile != null -> testProfile.product?.price
            else -> null
        }

        if (displayedPrice != null && apiPrice != null) {
            // Extract numeric price from displayed text (e.g., "₹1,499" -> "1499")
            val displayedPriceNum = displayedPrice.replace(Regex("[₹, ]"), "")
            val apiPriceNum = apiPrice.replace(".00", "")

            if (displayedPriceNum != apiPriceNum) {
                logger.error { "Price mismatch for '$testName': Displayed=$displayedPriceNum, API=$apiPriceNum" }
                return false
            }
        }

        return true
    }

    /**
     * Scroll to test panel
     */
    fun scrollToTestPanel(testName: String): LabTestsPage {
        logger.info { "Scrolling to test panel: $testName" }
        val panel = getTestPanelByName(testName)
        panel?.scrollIntoViewIfNeeded()
        return this
    }

    /**
     * Check if "Recommended for You" filter should be available
     * Based on web logic: filter should be shown if any item has content.why_test with length > 0
     */
    fun hasRecommendedFilterAvailable(): Boolean {
        val apiData = labTestData ?: fetchLabTestDataFromApi()
        if (apiData == null || apiData.data == null) {
            logger.warn { "API data not available, cannot determine if Recommended filter should be shown" }
            return false
        }

        val diagnosticProductList = apiData.data.diagnostic_product_list ?: return false
        
        // Check packages - filter should be shown if any item has content.why_test with length > 0
        val packages = diagnosticProductList.packages ?: emptyList()
        val hasRecommendedInPackages = packages.any { packageItem ->
            !packageItem.content?.why_test.isNullOrEmpty()
        }
        
        // Check test_profiles
        val testProfiles = diagnosticProductList.test_profiles ?: emptyList()
        val hasRecommendedInProfiles = testProfiles.any { profile ->
            !profile.content?.why_test.isNullOrEmpty()
        }
        
        // Check tests
        val tests = diagnosticProductList.tests ?: emptyList()
        val hasRecommendedInTests = tests.any { test ->
            !test.content?.why_test.isNullOrEmpty()
        }
        
        val hasRecommended = hasRecommendedInPackages || hasRecommendedInProfiles || hasRecommendedInTests
        
        logger.info { "Recommended filter available: $hasRecommended (packages: $hasRecommendedInPackages, profiles: $hasRecommendedInProfiles, tests: $hasRecommendedInTests)" }
        
        return hasRecommended
    }

    /**
     * Check if "Recommended for You" filter switch is visible on the page
     */
    fun isRecommendedFilterVisible(): Boolean {
        return try {
            val recommendedSwitch = byRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Recommended for You"))
            recommendedSwitch.isVisible
        } catch (e: Exception) {
            false
        }
    }
}

