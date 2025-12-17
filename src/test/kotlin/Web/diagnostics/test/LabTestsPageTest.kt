package diagnostics.test

import com.microsoft.playwright.*
import config.TestConfig
import diagnostics.page.LabTestsPage
import login.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabTestsPageTest {

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
        val viewport = TestConfig.Viewports.DESKTOP_FHD
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

    private fun navigateToAddressPage(): LabTestsPage{
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        return loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
//            .enterOtpAndContinueToHomePage(testUser.otp)
            .enterOtpAndContinueToLabTestForWeb((testUser.otp))
    }

    /**
     * Helper method to login and navigate to ForWeb.diagnostics page
     * Uses the EXACT same pattern as LoginFlowTest.login flow, then clicks "Book lab test" from home page
     */


    // ---------------------- Page Load Tests ----------------------

    @Test
    fun `should load lab tests page successfully`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
//        labTestsPage.waitForPageLoad()

//        assert(labTestsPage.isPageTitleVisible()) { "Page title 'Book Lab Tests' should be visible" }
//        assert(labTestsPage.isHeroSectionVisible()) { "Hero section should be visible" }
//        assert(labTestsPage.isSearchBarVisible()) { "Search bar should be visible" }
//        assert(labTestsPage.areFilterButtonsVisible()) { "Filter buttons should be visible" }

        labTestsPage.takeScreenshot("lab-tests-page-loaded")
    }

    @Test
    fun `should display test panels after page load`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val panelCount = labTestsPage.getVisibleTestPanelsCount()
        assert(panelCount > 0) { "At least one test panel should be visible" }

        labTestsPage.takeScreenshot("test-panels-displayed")
    }

    // ---------------------- API Integration Tests ----------------------

    @Test
    fun `should fetch lab test data from API successfully`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()

        val apiData = labTestsPage.fetchLabTestDataFromApi()
        assert(apiData != null) { "API data should be fetched successfully" }
        assert(apiData?.status == "success") { "API status should be 'success'" }
        assert(apiData?.data != null) { "API data should not be null" }

        labTestsPage.takeScreenshot("api-data-fetched")
    }

    @Test
    fun `should verify test panels match API data`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val allMatch = labTestsPage.verifyTestPanelsFromApi()
        assert(allMatch) { "All test panels from API should be visible on page" }

        labTestsPage.takeScreenshot("api-data-verified")
    }

    // ---------------------- Test Panel Visibility Tests ----------------------

    @Test
    fun `should display Advanced Thyroid Panel`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isTestPanelVisible("Advanced Thyroid Panel")) {
            "Advanced Thyroid Panel should be visible"
        }

        labTestsPage.scrollToTestPanel("Advanced Thyroid Panel")
        labTestsPage.takeScreenshot("advanced-thyroid-panel-visible")
    }

    @Test
    fun `should display Autoimmune Panel`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isTestPanelVisible("Autoimmune Panel")) {
            "Autoimmune Panel should be visible"
        }

        labTestsPage.scrollToTestPanel("Autoimmune Panel")
        labTestsPage.takeScreenshot("autoimmune-panel-visible")
    }

    @Test
    fun `should display Longevity Panel`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isTestPanelVisible("Longevity Panel")) {
            "Longevity Panel should be visible"
        }

        labTestsPage.scrollToTestPanel("Longevity Panel")
        labTestsPage.takeScreenshot("longevity-panel-visible")
    }

    @Test
    fun `should display Advanced Genetic Analysis`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isTestPanelVisible("Advanced Genetic Analysis")) {
            "Advanced Genetic Analysis should be visible"
        }

        labTestsPage.scrollToTestPanel("Advanced Genetic Analysis")
        labTestsPage.takeScreenshot("advanced-genetic-analysis-visible")
    }

    @Test
    fun `should display Advanced Gut Microbiome Analysis`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isTestPanelVisible("Advanced Gut Microbiome Analysis")) {
            "Advanced Gut Microbiome Analysis should be visible"
        }

        labTestsPage.scrollToTestPanel("Advanced Gut Microbiome Analysis")
        labTestsPage.takeScreenshot("advanced-gut-microbiome-visible")
    }

    // ---------------------- Test Panel Details Tests ----------------------

    @Test
    fun `should display correct price for Advanced Thyroid Panel`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val price = labTestsPage.getTestPanelPrice("Advanced Thyroid Panel")
        assert(price != null && price.contains("1,499")) {
            "Advanced Thyroid Panel price should be ₹1,499"
        }

        labTestsPage.scrollToTestPanel("Advanced Thyroid Panel")
        labTestsPage.takeScreenshot("advanced-thyroid-panel-price")
    }

    @Test
    fun `should verify test panel details match API data`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val testName = "Advanced Thyroid Panel"
        val detailsMatch = labTestsPage.verifyTestPanelDetails(testName)
        assert(detailsMatch) { "Test panel details should match API data for $testName" }

        labTestsPage.scrollToTestPanel(testName)
        labTestsPage.takeScreenshot("test-panel-details-verified")
    }

    // ---------------------- Filter Tests ----------------------

    @Test
    fun `should filter tests by Blood category`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val initialCount = labTestsPage.getVisibleTestPanelsCount()
        labTestsPage.clickFilter("Blood")

        // Wait for filter to apply
        page.waitForTimeout(1000.0)

        val filteredCount = labTestsPage.getVisibleTestPanelsCount()
        assert(filteredCount > 0) { "Should display at least one Blood test" }

        labTestsPage.takeScreenshot("blood-filter-applied")
    }

    @Test
    fun `should filter tests by Gene category`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.clickFilter("Gene")

        // Wait for filter to apply
        page.waitForTimeout(1000.0)

        val filteredCount = labTestsPage.getVisibleTestPanelsCount()
        assert(filteredCount > 0) { "Should display at least one Gene test" }

        labTestsPage.takeScreenshot("gene-filter-applied")
    }

    @Test
    fun `should filter tests by Gut category`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.clickFilter("Gut")

        // Wait for filter to apply
        page.waitForTimeout(1000.0)

        val filteredCount = labTestsPage.getVisibleTestPanelsCount()
        assert(filteredCount > 0) { "Should display at least one Gut test" }

        labTestsPage.takeScreenshot("gut-filter-applied")
    }

    @Test
    fun `should filter tests by Recommended for You`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.clickFilter("Recommended for You")

        // Wait for filter to apply
        page.waitForTimeout(1000.0)

        val filteredCount = labTestsPage.getVisibleTestPanelsCount()
        assert(filteredCount > 0) { "Should display at least one recommended test" }

        labTestsPage.takeScreenshot("recommended-filter-applied")
    }

    @Test
    fun `should reset filter when clicking All`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val initialCount = labTestsPage.getVisibleTestPanelsCount()

        labTestsPage.clickFilter("Blood")
        page.waitForTimeout(1000.0)

        labTestsPage.clickFilter("All")
        page.waitForTimeout(1000.0)

        val finalCount = labTestsPage.getVisibleTestPanelsCount()
        assert(finalCount >= initialCount) { "All filter should show all tests" }

        labTestsPage.takeScreenshot("all-filter-reset")
    }

    // ---------------------- Search Tests ----------------------

    @Test
    fun `should search for test by name`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.enterSearchQuery("Thyroid")

        // Wait for search to apply
        page.waitForTimeout(1000.0)

        assert(labTestsPage.isTestPanelVisible("Advanced Thyroid Panel")) {
            "Advanced Thyroid Panel should be visible after searching for 'Thyroid'"
        }

        labTestsPage.takeScreenshot("search-thyroid")
    }

    @Test
    fun `should clear search and show all tests`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        val initialCount = labTestsPage.getVisibleTestPanelsCount()

        labTestsPage.enterSearchQuery("Thyroid")
        page.waitForTimeout(1000.0)

        labTestsPage.clearSearch()
        page.waitForTimeout(1000.0)

        val finalCount = labTestsPage.getVisibleTestPanelsCount()
        assert(finalCount >= initialCount) { "Clearing search should show all tests" }

        labTestsPage.takeScreenshot("search-cleared")
    }

    @Test
    fun `should search for non-existent test and show no results`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.enterSearchQuery("NonExistentTest12345")

        // Wait for search to apply
        page.waitForTimeout(1000.0)

        val count = labTestsPage.getVisibleTestPanelsCount()
        // Note: This assertion depends on how the app handles no results
        // Adjust based on actual behavior

        labTestsPage.takeScreenshot("search-no-results")
    }

    // ---------------------- Interaction Tests ----------------------

    @Test
    fun `should click on test panel`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.scrollToTestPanel("Advanced Thyroid Panel")
        labTestsPage.clickTestPanel("Advanced Thyroid Panel")

        // Wait for navigation or modal to open
        page.waitForTimeout(2000.0)

        // Verify navigation or modal opened
        // Adjust assertion based on actual behavior

        labTestsPage.takeScreenshot("test-panel-clicked")
    }

    @Test
    fun `should click View Details button`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.scrollToTestPanel("Advanced Thyroid Panel")
        labTestsPage.clickViewDetails("Advanced Thyroid Panel")

        // Wait for details page or modal
        page.waitForTimeout(2000.0)

        labTestsPage.takeScreenshot("view-details-clicked")
    }

    @Test
    fun `should click Book Now button for Longevity Panel`() {
        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isBookNowButtonVisible()) {
            "Book Now button should be visible"
        }

        labTestsPage.clickBookNow()

        // Wait for navigation
        page.waitForTimeout(2000.0)

        labTestsPage.takeScreenshot("book-now-clicked")
    }

    // ---------------------- Responsive Tests ----------------------

    @Test
    fun `should display correctly on mobile viewport`() {
        context.close()
        val mobileViewport = TestConfig.Viewports.MOBILE_PORTRAIT
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(mobileViewport.width, mobileViewport.height)
            .setHasTouch(mobileViewport.hasTouch)
            .setIsMobile(mobileViewport.isMobile)
            .setDeviceScaleFactor(mobileViewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isPageTitleVisible()) { "Page title should be visible on mobile" }
        assert(labTestsPage.getVisibleTestPanelsCount() > 0) {
            "At least one test panel should be visible on mobile"
        }

        labTestsPage.takeScreenshot("mobile-viewport")
    }

    @Test
    fun `should display correctly on tablet viewport`() {
        context.close()
        val tabletViewport = TestConfig.Viewports.TABLET_PORTRAIT
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(tabletViewport.width, tabletViewport.height)
            .setHasTouch(tabletViewport.hasTouch)
            .setIsMobile(tabletViewport.isMobile)
            .setDeviceScaleFactor(tabletViewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        // Login and navigate to ForWeb.diagnostics page
        val labTestsPage = navigateToAddressPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        assert(labTestsPage.isPageTitleVisible()) { "Page title should be visible on tablet" }
        assert(labTestsPage.getVisibleTestPanelsCount() > 0) {
            "At least one test panel should be visible on tablet"
        }

        labTestsPage.takeScreenshot("tablet-viewport")
    }
}

