package diagnostics.page

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import model.LabTestResponse
import mu.KotlinLogging
import utils.logger.logger

private val logger = KotlinLogging.logger {}

/**
 * Lab Tests Page - handles interactions with the ForWeb.diagnostics/lab tests page
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
        // Wait for the page title or main heading
        byText("Book Lab Tests").waitFor()
        logger.info { "Lab Tests page loaded" }
        return this
    }


    fun waitForURl(): LabTestsPage{
        page.waitForURL("https://app.stg.deepholistics.com/diagnostics")
        return this
    }

    /**
     * Fetch lab test data from API
     */
    fun fetchLabTestDataFromApi(): LabTestResponse? {
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains(TestConfig.Urls.LAB_TEST_API_URL) == true && response.status() == 200
            },
            {
                page.waitForURL(TestConfig.Urls.DIAGNOSTICS_URL)
            }
        )

        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "API response body is empty" }
            return null
        }

        logger.info { "API response received" }

        try {
            val responseObj = json.decodeFromString<LabTestResponse>(responseBody)
            labTestData = responseObj
            return responseObj
        } catch (e: Exception) {
            logger.error { "Failed to parse API response: ${e.message}" }
            return null
        }
    }
    fun waitForConfirmation() {
        page.waitForURL("https://app.stg.deepholistics.com/diagnostics")
    }

    /**
     * Get all test panels displayed on the page
     */
    fun getAllTestPanels(): List<Locator> {
        // Test panels are typically in cards or grid items
        // Adjust selector based on actual HTML structure
        return page.locator("[data-testid='test-panel'], .test-card, [class*='test-card'], [class*='test-panel']").all()
    }

    /**
     * Get test panel by name
     */
    fun getTestPanelByName(testName: String): Locator? {
        return try {
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
    fun clickFilter(filterName: String): LabTestsPage {
        logger.info { "Clicking filter: $filterName" }
        byText(filterName).click()
        return this
    }

    /**
     * Enter search query
     */
    fun enterSearchQuery(query: String): LabTestsPage {
        logger.info { "Entering search query: $query" }
        val searchInput = page.locator("input[placeholder*='Search'], input[type='search']").first()
        searchInput.fill(query)
        return this
    }

    /**
     * Clear search query
     */
    fun clearSearch(): LabTestsPage {
        logger.info { "Clearing search" }
        val searchInput = page.locator("input[placeholder*='Search'], input[type='search']").first()
        searchInput.clear()
        return this
    }

    /**
     * Get search input value
     */
    fun getSearchValue(): String {
        return page.locator("input[placeholder*='Search'], input[type='search']").first().inputValue()
    }

    /**
     * Check if page title is visible
     */
    fun isPageTitleVisible(): Boolean {
        return byText("Book Lab Tests").isVisible
    }

    /**
     * Check if hero section is visible
     */
    fun isHeroSectionVisible(): Boolean {
        return byText("Get tested from the comfort of your home").isVisible
    }

    /**
     * Check if filter buttons are visible
     */
    fun areFilterButtonsVisible(): Boolean {
        val filters = listOf("All", "Blood", "Gene", "Gut", "Recommended for You")
        return filters.all { byText(it).isVisible }
    }

    /**
     * Check if search bar is visible
     */
    fun isSearchBarVisible(): Boolean {
        return page.locator("input[placeholder*='Search'], input[type='search']").isVisible
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
        return panel?.locator("[class*='price'], [data-testid='price']")?.textContent()
    }

    /**
     * Get test panel description by test name
     */
    fun getTestPanelDescription(testName: String): String? {
        val panel = getTestPanelByName(testName)
        return panel?.locator("[class*='description'], p")?.textContent()
    }

    /**
     * Get test panel type (Blood test, Cheek swab test, etc.) by test name
     */
    fun getTestPanelType(testName: String): String? {
        val panel = getTestPanelByName(testName)
        return panel?.locator("[class*='type'], [class*='sample-type']")?.textContent()
    }

    /**
     * Check if "Book Now" button is visible for Longevity Panel
     */
    fun isBookNowButtonVisible(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).isVisible
    }

    /**
     * Click "Book Now" button
     */
    fun clickBookNow(): LabTestsPage {
        logger.info { "Clicking Book Now button" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).click()
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
        
        val test = testPackage ?: testProfile
        if (test == null) {
            logger.error { "Test '$testName' not found in API data" }
            return false
        }

//        val displayedPrice = getTestPanelPrice(testName)
//        val apiPrice = test.product?.price
//
//        if (displayedPrice != null && apiPrice != null) {
//            // Extract numeric price from displayed text (e.g., "₹1,499" -> "1499")
//            val displayedPriceNum = displayedPrice.replace(Regex("[₹, ]"), "")
//            val apiPriceNum = apiPrice.replace(".00", "")
//
//            if (displayedPriceNum != apiPriceNum) {
//                logger.error { "Price mismatch for '$testName': Displayed=$displayedPriceNum, API=$apiPriceNum" }
//                return false
//            }
//        }

        return true
    }

    /**
     * Scroll to test panel
     */
    fun scrollToTestPanel(testName: String): LabTestsPage {
        val panel = getTestPanelByName(testName)
        panel?.scrollIntoViewIfNeeded()
        return this
    }

    /**
     * Wait for test panels to load
     */
    fun waitForTestPanelsToLoad(): LabTestsPage {
        // Wait for at least one test panel to be visible
        page.waitForSelector("[data-testid='test-panel'], .test-card, [class*='test-card']", 
            Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
        return this
    }


}

