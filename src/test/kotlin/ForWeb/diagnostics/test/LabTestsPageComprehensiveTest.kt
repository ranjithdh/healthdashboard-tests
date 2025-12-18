package forWeb.diagnostics.test

import com.microsoft.playwright.*
import com.microsoft.playwright.Page  // ← Add this explicit import
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import forWeb.diagnostics.page.LabTestsPage
import login.page.LoginPage
import model.LabTestPackage
import model.LabTestProfile
import model.LabTestItem
import mu.KotlinLogging
import org.junit.jupiter.api.*

private val logger = KotlinLogging.logger {}

/**
 * Comprehensive test cases for Lab Tests Page
 * Verifies UI elements match backend JSON response data
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabTestsPageComprehensiveTest {

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

    /**
     * Helper method to navigate to diagnostics page
     */
    private fun navigateToDiagnosticsPage(): LabTestsPage {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        return loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToLabTestForWeb(testUser.otp)
    }

    // ---------------------- Page Load and Basic Elements Tests ----------------------

    @Test
    fun `should load lab tests page successfully`() { // passed
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        
        assert(labTestsPage.isTestPanelNameVisible("Book Lab Tests")) { 
            "Book Lab Tests heading should be visible" 
        }
        
        labTestsPage.takeScreenshot("lab-tests-page-loaded")
    }

    @Test
    fun `should display all static page elements`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        
        // Verify "Book Lab Tests" heading is visible
        val bookLabTestsHeading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Book Lab Tests"))
        bookLabTestsHeading.waitFor()
        assert(bookLabTestsHeading.isVisible) {
            "Static element 'Book Lab Tests' heading should be visible"
        }
        logger.info { "✓ 'Book Lab Tests' heading is visible" }
        
        // Verify "Get tested from the comfort of your home" heading is visible
        val heroHeading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Get tested from the comfort"))
        heroHeading.waitFor()
        assert(heroHeading.isVisible) {
            "Static element 'Get tested from the comfort of your home' heading should be visible"
        }
        logger.info { "✓ 'Get tested from the comfort of your home' heading is visible" }
        
        // Verify "With flexible testing options..." paragraph is visible
        val flexibleOptionsParagraph = page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("With flexible testing options"))
        flexibleOptionsParagraph.waitFor()
        assert(flexibleOptionsParagraph.isVisible) {
            "Static element 'With flexible testing options' paragraph should be visible"
        }
        logger.info { "✓ 'With flexible testing options' paragraph is visible" }
        
        labTestsPage.takeScreenshot("static-elements-verified")
        logger.info { "All 3 static elements verified successfully" }
    }

    @Test
    fun `should display search textbox`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()

        labTestsPage.clickSearchTextBox()

        // Use clear() instead of fill("")
        val searchInput = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in lab tests"))
        searchInput.clear()

        labTestsPage.takeScreenshot("search-textbox-clicked")
    }

    @Test
    fun `should display main promotional heading`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        
        labTestsPage.clickGetTestedHeading()
        labTestsPage.clickFlexibleTestingOptionsParagraph()
        
        labTestsPage.takeScreenshot("promotional-section-clicked")
    }

    @Test
    fun `should interact with all filter switches`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        // waitForTestPanelsToLoad() will fetch API data during page load
        labTestsPage.waitForTestPanelsToLoad()

        // Check if "Recommended for You" filter should be available based on API data
        // Filter should be shown if any item has content.why_test with length > 0
        // This uses cached API data from waitForTestPanelsToLoad()
        val hasRecommended = labTestsPage.hasRecommendedFilterAvailable()
        
        // Verify UI matches API logic: if API says it should be available, it should be visible
        val isRecommendedVisible = labTestsPage.isRecommendedFilterVisible()
        assert(hasRecommended == isRecommendedVisible) {
            "Recommended filter visibility should match API data. API indicates available: $hasRecommended, UI shows visible: $isRecommendedVisible"
        }

        // Now interact with all filter switches
        labTestsPage
            .clickFilterSwitch("All")
            .clickFilterSwitch("Blood")
            .clickFilterSwitch("Gene")
            .clickFilterSwitch("Gut")
        
        // Only click "Recommended for You" if it's available (matches web logic)
        if (hasRecommended && isRecommendedVisible) {
            labTestsPage.clickFilterSwitch("Recommended for You")
        } else {
            logger.info { "Skipping 'Recommended for You' filter - not available based on API data" }
        }

        labTestsPage.takeScreenshot("all-filters-clicked")
    }

    @Test
    fun `should verify all test panel cards match backend data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        // Get all backend items (packages + test_profiles + tests)
        val backendItems = labTestsPage.getAllBackendItems()
        logger.info { "Backend items count: ${backendItems.size}" }
        logger.info { "Backend items: $backendItems" }
        // Verify backend has at least one item
        assert(backendItems.isNotEmpty()) {
            "Expected at least one item from backend, but got ${backendItems.size}"
        }
        // Cards should already be loaded from waitForTestPanelsToLoad()
        // Get all test panel cards from UI (excluding featured Longevity Panel)
        val uiCards = labTestsPage.getAllTestPanelCards()
        logger.info { "UI cards count: ${uiCards.size}" }
        // If no cards found, try alternative approach
        if (uiCards.isEmpty()) {
            logger.warn { "No cards found with getAllTestPanelCards(), trying alternative approach..." }
            // Try to find cards by View Details buttons
            try {
                val viewDetailsButtons = page.locator("button:has-text('View Details')").all()
                logger.info { "Found ${viewDetailsButtons.size} View Details buttons" }
            } catch (e: Exception) {
                logger.error { "Could not find View Details buttons: ${e.message}" }
            }
        }
        // Verify UI shows the same number of cards as backend items (excluding featured Longevity Panel)
        assert(uiCards.size == backendItems.size) {
            "Expected ${backendItems.size} test panel cards in UI (matching backend count), but got ${uiCards.size}. " +
            "Page URL: ${page.url()}"
        }
        // Normalize backend names for comparison (handle UI vs backend name differences)
        val normalizedBackendItems = backendItems.map { name ->
            when {
                name == "Advanced Gut Microbiome Analysis" -> "Advanced Gut Microbiome"
                name == "Stress and Cortisol Rhythm Panel" -> "Stress and Cortisol Rhythm"
                else -> name
            }
        }
        // Single loop: Verify each UI card matches backend data (dynamic template approach)
        // All cards have same template structure, just different values from backend
        var matchedCount = 0
        var mismatchCount = 0
        var viewDetailsButtonIssues = 0
        val foundBackendItems = mutableSetOf<String>()
        uiCards.forEachIndexed { index, card ->
            // Extract ALL card data in ONE pass (minimizes DOM queries - single textContent() call)
            val cardData = labTestsPage.extractAllCardData(card, index)
            val cardName = cardData.name

            if (cardName == null) {
                logger.warn { "Card $index: Could not extract name" }
                mismatchCount++
                return@forEachIndexed
            }

            // Normalize card name for comparison
            val normalizedCardName = when {
                cardName.contains("Advanced Gut Microbiome") -> "Advanced Gut Microbiome"
                cardName.contains("Stress and Cortisol") -> "Stress and Cortisol Rhythm"
                else -> cardName
            }

            // Find matching backend item (dynamic lookup)
            val backendItem = labTestsPage.getBackendItemByName(cardName)
            if (backendItem == null) {
                logger.warn { "Backend item not found for UI card: '$cardName'" }
                mismatchCount++
                return@forEachIndexed
            }

            val (pkg, profile, test) = backendItem
            
            // Extract backend values dynamically (same template, different values)
            val backendDescription = when {
                pkg != null -> pkg.description
                profile != null -> profile.description
                test != null -> test.description
                else -> null
            }
            val backendPrice = when {
                pkg != null -> pkg.product?.price
                profile != null -> profile.product?.price
                test != null -> test.product?.price
                else -> null
            }
            val backendSampleType = when {
                pkg != null -> pkg.sample_type
                profile != null -> profile.sample_type
                test != null -> test.sample_type
                else -> null
            }
            val backendCode = when {
                pkg != null -> pkg.code
                profile != null -> profile.code
                test != null -> test.code
                else -> null
            }

            // Track found backend items
            foundBackendItems.add(normalizedCardName)

            // Verify all card elements using extracted data (no additional DOM queries)
            // 1. Verify image exists
            if (!cardData.hasImage) {
                logger.warn { "Image not found for '$cardName'" }
                mismatchCount++
            }
            
            // 2. Verify description
            val uiDescription = cardData.description
            val isSampleTypeOrBadge = uiDescription?.let { desc ->
                desc.equals("Blood test", ignoreCase = true) ||
                desc.equals("Stool test", ignoreCase = true) ||
                desc.equals("Cheek swab test", ignoreCase = true) ||
                desc.equals("At-Home Test Kit", ignoreCase = true) ||
                desc.equals("Recommended for you", ignoreCase = true) ||
                desc.length < 20
            } ?: false
            
            if (!isSampleTypeOrBadge && uiDescription != null && backendDescription != null) {
                val backendDescSnippet = backendDescription.take(50)
                val uiDescSnippet = uiDescription.take(50)
                val descriptionMatches = uiDescription.contains(backendDescSnippet) || 
                                         backendDescription.contains(uiDescSnippet) ||
                                         uiDescSnippet.contains(backendDescSnippet) ||
                                         backendDescSnippet.contains(uiDescSnippet)
                
                if (!descriptionMatches) {
                    logger.warn { "Description mismatch for '$cardName': UI='$uiDescription', Backend='$backendDescription'" }
                    mismatchCount++
                }
            }

            // 3. Verify price
            val uiPrice = cardData.price
            if (uiPrice != null && backendPrice != null) {
                val uiPriceNum = uiPrice.replace(Regex("[₹, ]"), "").replace(".00", "")
                val backendPriceNum = backendPrice.replace(".00", "")
                
                if (uiPriceNum != backendPriceNum) {
                    logger.warn { "Price mismatch for '$cardName': UI='$uiPrice' ($uiPriceNum), Backend='₹$backendPrice' ($backendPriceNum)" }
                    mismatchCount++
                }
            }

            // 4. Verify sample type (with web logic)
            val uiType = cardData.sampleType
            if (uiType != null && backendSampleType != null) {
                val expectedType = labTestsPage.getSampleTypeDisplayText(backendSampleType, backendCode)
                if (uiType != expectedType) {
                    logger.warn { "Sample type mismatch for '$cardName': UI='$uiType', Expected='$expectedType'" }
                    mismatchCount++
                }
            }

            // 5. Verify "Recommended for you" badge visibility (based on backend why_test)
            val shouldHaveBadge = labTestsPage.shouldHaveRecommendedBadge(pkg, profile, test)
            val hasBadge = cardData.hasRecommendedBadge
            
            if (shouldHaveBadge != hasBadge) {
                if (shouldHaveBadge && !hasBadge) {
                    logger.warn { "Missing 'Recommended for you' badge for '$cardName' (backend has why_test)" }
                    mismatchCount++
                } else if (!shouldHaveBadge && hasBadge) {
                    logger.warn { "Unexpected 'Recommended for you' badge for '$cardName' (backend has no why_test)" }
                    mismatchCount++
                }
            } else {
                logger.debug { "Badge visibility correct for '$cardName': shouldHave=$shouldHaveBadge, has=$hasBadge" }
            }

            // 6. Verify View Details button is visible and enabled
            val viewDetailsButton = cardData.viewDetailsButton
            if (viewDetailsButton == null) {
                logger.warn { "View Details button NOT found for '$cardName' (index: $index)" }
                viewDetailsButtonIssues++
            } else {
                try {
                    val isVisible = viewDetailsButton.isVisible
                    val isEnabled = viewDetailsButton.isEnabled
                    
                    if (!isVisible) {
                        logger.warn { "View Details button NOT visible for '$cardName' (index: $index)" }
                        viewDetailsButtonIssues++
                    }
                    
                    if (!isEnabled) {
                        logger.warn { "View Details button NOT enabled for '$cardName' (index: $index)" }
                        viewDetailsButtonIssues++
                    }
                } catch (e: Exception) {
                    logger.error { "Failed to verify View Details button for '$cardName' (index: $index): ${e.message}" }
                    viewDetailsButtonIssues++
                }
            }

            matchedCount++
            logger.info { "✓ Verified card: $cardName" }
        }

        // Verify all backend items were found in UI
        val missingItems = normalizedBackendItems.filter { backendName ->
            !foundBackendItems.contains(backendName)
        }

        if (missingItems.isNotEmpty()) {
            logger.error { "Backend items not found in UI: $missingItems" }
            logger.error { "Backend items: $normalizedBackendItems" }
            logger.error { "Found UI items: $foundBackendItems" }
            mismatchCount += missingItems.size
        }

        logger.info { "Matched cards: $matchedCount, Data mismatches: $mismatchCount, View Details button issues: $viewDetailsButtonIssues" }
        
        labTestsPage.takeScreenshot("all-cards-verified")
        
        assert(mismatchCount == 0) {
            "Found $mismatchCount mismatches between UI cards and backend data"
        }
        
        assert(viewDetailsButtonIssues == 0) {
            "Found $viewDetailsButtonIssues View Details button issues (not visible or not enabled)"
        }
        
        assert(matchedCount == backendItems.size) {
            "Expected to match all ${backendItems.size} cards from backend, but only matched $matchedCount"
        }
    }

    @Test
    fun `should click View Details button for all 14 test panel cards`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        // Get all backend items (packages + test_profiles + tests)
        val backendItems = labTestsPage.getAllBackendItems()
        logger.info { "Backend items: $backendItems" }

        // Verify backend has 14 items
        assert(backendItems.size == 14) {
            "Expected 14 items from backend, but got ${backendItems.size}"
        }

        // Expected order of cards based on Playwright code
        // This matches the order in which View Details buttons appear
        val expectedCardOrder = listOf(
            "Longevity Panel",
            "Advanced Thyroid Panel",
            "Autoimmune Panel",
            "Advanced Genetic Analysis",
            "Advanced Gut Microbiome Analysis",
            "Advanced Heart Health Panel",
            "Essential Nutrients Panel",
            "Thyroid Health Panel",
            "Omega Profile Panel",
            "Stress and Cortisol Rhythm Panel",
            "Liver Health Panel",
            "Toxic Metals Panel",
            "Blood Health Panel",
            "Allergies Test Panel"
        )

        logger.info { "Clicking View Details buttons for all 14 cards" }

        var clickedCount = 0
        val failedCards = mutableListOf<String>()

        // Use backend items count to determine how many buttons to check
        val totalCards = backendItems.size
        
        (0 until totalCards).forEach { index ->
            try {
                logger.info { "Checking View Details button for card $index" }

                // Verify button is enabled before clicking
                val isEnabled = labTestsPage.isViewDetailsButtonEnabled(index)
                
                if (!isEnabled) {
                    failedCards.add("Card $index (disabled)")
                    logger.warn { "✗ View Details button is NOT enabled for card at index: $index" }
                } else {
                    // Click the View Details button
                    labTestsPage.clickViewDetailsButton(index)

                    // Click back button to return to the list
                    try {
                        page.getByText("BackBook Lab TestsWith").click()
                    } catch (e: Exception) {
                        logger.debug { "Back button not found for card $index, continuing..." }
                    }

                    clickedCount++
                    logger.info { "✓ Successfully clicked View Details button for card at index: $index" }
                }

            } catch (e: Exception) {
                failedCards.add("Card $index (error: ${e.message})")
                logger.error { "Failed to click View Details button for card at index $index: ${e.message}" }
            }
        }

        logger.info { "View Details button click summary:" }
        logger.info { "  Total cards from backend: $totalCards" }
        logger.info { "  Successfully clicked: $clickedCount" }
        logger.info { "  Failed: ${failedCards.size}" }
        logger.info { "  Failed cards: $failedCards" }

        labTestsPage.takeScreenshot("view-details-buttons-clicked")

        // Assert that all buttons were clicked successfully
        assert(clickedCount == totalCards) {
            "Expected to click all $totalCards View Details buttons, but only clicked $clickedCount. Failed cards: $failedCards"
        }

        assert(failedCards.isEmpty()) {
            "Found ${failedCards.size} View Details buttons that failed. Failed cards: $failedCards"
        }
    }

    @Test
    fun `should verify View Details button is enabled for all test panel cards`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        // Get all backend items (packages + test_profiles + tests)
        val backendItems = labTestsPage.getAllBackendItems()
        logger.info { "Backend items: $backendItems" }
        logger.info { "Backend items count: ${backendItems.size}" }

        // Verify backend has at least one item
        assert(backendItems.isNotEmpty()) {
            "Expected at least one item from backend, but got ${backendItems.size}"
        }

        val totalCards = backendItems.size
        logger.info { "Verifying View Details buttons for all $totalCards cards" }

        var enabledCount = 0
        var disabledCount = 0
        val failedCards = mutableListOf<String>()

        (0 until totalCards).forEach { index ->
            try {
                logger.info { "Checking View Details button for card at index: $index" }

                // Verify the View Details button exists and is enabled
                val isEnabled = labTestsPage.isViewDetailsButtonEnabled(index)

                if (isEnabled) {
                    enabledCount++
                    logger.info { "✓ View Details button is enabled for card at index: $index" }
                } else {
                    disabledCount++
                    failedCards.add("Card $index (disabled)")
                    logger.warn { "✗ View Details button is NOT enabled for card at index: $index" }
                }

                // Also verify the button is visible
                try {
                    val button = labTestsPage.getViewDetailsButton(index)
                    val isVisible = button.isVisible
                    assert(isVisible) {
                        "View Details button for card at index $index should be visible"
                    }
                    logger.info { "  Button visibility: $isVisible for card at index: $index" }
                } catch (e: Exception) {
                    logger.warn { "Could not verify visibility for card at index $index: ${e.message}" }
                    failedCards.add("Card $index (visibility check failed)")
                }

            } catch (e: Exception) {
                disabledCount++
                failedCards.add("Card $index (error: ${e.message})")
                logger.error { "Failed to check View Details button for card at index $index: ${e.message}" }
            }
        }

        logger.info { "View Details button verification summary:" }
        logger.info { "  Total cards from backend: $totalCards" }
        logger.info { "  Enabled: $enabledCount" }
        logger.info { "  Disabled/Failed: $disabledCount" }
        logger.info { "  Failed cards: $failedCards" }

        labTestsPage.takeScreenshot("view-details-buttons-verification")

        // Assert that all buttons are enabled
        assert(enabledCount == totalCards) {
            "Expected all $totalCards View Details buttons to be enabled, but only $enabledCount were enabled. Failed cards: $failedCards"
        }

        assert(disabledCount == 0) {
            "Found $disabledCount View Details buttons that are disabled or failed verification. Failed cards: $failedCards"
        }
    }
    // ---------------------- Longevity Panel Tests ----------------------

    @Test
    fun `should verify Longevity Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        // Fetch API data
        val apiData = labTestsPage.fetchLabTestDataFromApi()
        assert(apiData != null) { "API data should be fetched successfully" }

        val longevityPanel = labTestsPage.getTestPanelFromApi("Longevity Panel")
        assert(longevityPanel != null) { "Longevity Panel should exist in API data" }

        // Verify UI elements
        assert(labTestsPage.isTestPanelNameVisible("Longevity Panel")) {
            "Longevity Panel heading should be visible"
        }
        assert(labTestsPage.isTestPanelImageVisible("Longevity Panel")) {
            "Longevity Panel image should be visible"
        }

        // Verify price matches API
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Longevity Panel")
        assert(priceMatches) {
            "Longevity Panel price should match API data. API: ${longevityPanel?.product?.price}"
        }

        // Verify description matches API
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Longevity Panel")
        assert(descriptionMatches) {
            "Longevity Panel description should match API data"
        }

        // Verify sample type matches API
        val sampleTypeMatches = labTestsPage.verifyPanelSampleTypeMatchesApi("Longevity Panel")
        assert(sampleTypeMatches) {
            "Longevity Panel sample type should match API data. API: ${longevityPanel?.sample_type}"
        }

        labTestsPage.takeScreenshot("longevity-panel-verified")
    }

    @Test
    fun `should interact with Longevity Panel featured card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()

        labTestsPage.clickLongevityPanelImage()
        labTestsPage.click102Biomarkers()
        labTestsPage.clickLongevityPanelParagraph()
        labTestsPage.clickGiveYourFamilyTheSame()
        labTestsPage.clickLongevityBookNow()

        labTestsPage.takeScreenshot("longevity-featured-card-interacted")
    }

    @Test
    fun `should interact with Longevity Panel grid card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickLongevityPanelHeading()
        labTestsPage.clickLongevityPanelDescription()
        labTestsPage.clickLongevityPanelPrice()
        labTestsPage.clickLongevityPanelViewDetails()
        labTestsPage.clickLongevityPanelImageByRole()
        
        labTestsPage.takeScreenshot("longevity-grid-card-interacted")
    }

    // ---------------------- Advanced Thyroid Panel Tests ----------------------

    @Test
    fun `should verify Advanced Thyroid Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val thyroidPanel = labTestsPage.getTestPanelFromApi("Advanced Thyroid Panel")
        assert(thyroidPanel != null) { "Advanced Thyroid Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Advanced Thyroid Panel")) { 
            "Advanced Thyroid Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Advanced Thyroid Panel")) { 
            "Advanced Thyroid Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Advanced Thyroid Panel")
        assert(priceMatches) { 
            "Advanced Thyroid Panel price should match API data. API: ${thyroidPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Advanced Thyroid Panel")
        assert(descriptionMatches) { 
            "Advanced Thyroid Panel description should match API data" 
        }
        
        val sampleTypeMatches = labTestsPage.verifyPanelSampleTypeMatchesApi("Advanced Thyroid Panel")
        assert(sampleTypeMatches) { 
            "Advanced Thyroid Panel sample type should match API data" 
        }
        
        labTestsPage.takeScreenshot("advanced-thyroid-panel-verified")
    }

    @Test
    fun `should interact with Advanced Thyroid Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItem3()
        labTestsPage.clickAdvancedThyroidPanelImage()
        labTestsPage.clickRecommendedForYouNth(1)
        labTestsPage.clickAdvancedThyroidPanelHeading()
        labTestsPage.clickBloodTestNth(2)
        labTestsPage.clickAdvancedAtHomeAntibody()
        labTestsPage.clickAdvancedThyroidPanelPrice()
        labTestsPage.clickViewDetailsByIndex(1)
        
        labTestsPage.takeScreenshot("advanced-thyroid-panel-interacted")
    }

    // ---------------------- Autoimmune Panel Tests ----------------------

    @Test
    fun `should verify Autoimmune Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val autoimmunePanel = labTestsPage.getTestPanelFromApi("Autoimmune Panel")
        assert(autoimmunePanel != null) { "Autoimmune Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Autoimmune Panel")) { 
            "Autoimmune Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Autoimmune Panel")) { 
            "Autoimmune Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Autoimmune Panel")
        assert(priceMatches) { 
            "Autoimmune Panel price should match API data. API: ${autoimmunePanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Autoimmune Panel")
        assert(descriptionMatches) { 
            "Autoimmune Panel description should match API data" 
        }
        
        labTestsPage.takeScreenshot("autoimmune-panel-verified")
    }

    @Test
    fun `should interact with Autoimmune Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(4)
        labTestsPage.clickAutoimmunePanelImage()
        labTestsPage.clickAutoimmunePanelHeading()
        labTestsPage.clickBloodTestNth(3)
        labTestsPage.clickAutoimmunePanelDescription()
        labTestsPage.clickPriceByNth(2)
        labTestsPage.clickViewDetailsByIndex(2)
        
        labTestsPage.takeScreenshot("autoimmune-panel-interacted")
    }

    // ---------------------- Advanced Genetic Analysis Tests ----------------------

    @Test
    fun `should verify Advanced Genetic Analysis UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val geneticPanel = labTestsPage.getTestPanelFromApi("Advanced Genetic Analysis")
        assert(geneticPanel != null) { "Advanced Genetic Analysis should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Advanced Genetic Analysis")) { 
            "Advanced Genetic Analysis heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Advanced Genetic Analysis")) { 
            "Advanced Genetic Analysis image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Advanced Genetic Analysis")
        assert(priceMatches) { 
            "Advanced Genetic Analysis price should match API data. API: ${geneticPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Advanced Genetic Analysis")
        assert(descriptionMatches) { 
            "Advanced Genetic Analysis description should match API data" 
        }
        
        val sampleTypeMatches = labTestsPage.verifyPanelSampleTypeMatchesApi("Advanced Genetic Analysis")
        assert(sampleTypeMatches) { 
            "Advanced Genetic Analysis sample type should match API data. API: ${geneticPanel?.sample_type}" 
        }
        
        labTestsPage.takeScreenshot("advanced-genetic-analysis-verified")
    }

    @Test
    fun `should interact with Advanced Genetic Analysis card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(5)
        labTestsPage.clickAdvancedGeneticAnalysisImage()
        labTestsPage.clickRecommendedForYouNth(2)
        labTestsPage.clickAdvancedGeneticAnalysisHeading()
        labTestsPage.clickCheekSwabTest()
        labTestsPage.clickCuttingEdgeCheekSwabDNA()
        labTestsPage.clickAdvancedGeneticAnalysisPrice()
        labTestsPage.clickViewDetailsByIndex(3)
        
        labTestsPage.takeScreenshot("advanced-genetic-analysis-interacted")
    }

    // ---------------------- Advanced Gut Microbiome Analysis Tests ----------------------

    @Test
    fun `should verify Advanced Gut Microbiome Analysis UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val gutPanel = labTestsPage.getTestPanelFromApi("Advanced Gut Microbiome Analysis")
        assert(gutPanel != null) { "Advanced Gut Microbiome Analysis should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Advanced Gut Microbiome")) { 
            "Advanced Gut Microbiome heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Advanced Gut Microbiome")) { 
            "Advanced Gut Microbiome image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Advanced Gut Microbiome Analysis")
        assert(priceMatches) { 
            "Advanced Gut Microbiome Analysis price should match API data. API: ${gutPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Advanced Gut Microbiome Analysis")
        assert(descriptionMatches) { 
            "Advanced Gut Microbiome Analysis description should match API data" 
        }
        
        val sampleTypeMatches = labTestsPage.verifyPanelSampleTypeMatchesApi("Advanced Gut Microbiome Analysis")
        assert(sampleTypeMatches) { 
            "Advanced Gut Microbiome Analysis sample type should match API data. API: ${gutPanel?.sample_type}" 
        }
        
        labTestsPage.takeScreenshot("advanced-gut-microbiome-verified")
    }

    @Test
    fun `should interact with Advanced Gut Microbiome Analysis card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(6)
        labTestsPage.clickAdvancedGutMicrobiomeImage()
        labTestsPage.clickRecommendedForYouNth(3)
        labTestsPage.clickAdvancedGutMicrobiomeHeading()
        labTestsPage.clickStoolTestExact()
        labTestsPage.clickAdvancedAtHomeStool()
        labTestsPage.clickAdvancedGutMicrobiomePrice()
        labTestsPage.clickViewDetailsByIndex(4)
        
        labTestsPage.takeScreenshot("advanced-gut-microbiome-interacted")
    }

    // ---------------------- Advanced Heart Health Panel Tests ----------------------

    @Test
    fun `should verify Advanced Heart Health Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val heartPanel = labTestsPage.getTestPanelFromApi("Advanced Heart Health Panel")
        assert(heartPanel != null) { "Advanced Heart Health Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Advanced Heart Health Panel")) { 
            "Advanced Heart Health Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Advanced Heart Health Panel")) { 
            "Advanced Heart Health Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Advanced Heart Health Panel")
        assert(priceMatches) { 
            "Advanced Heart Health Panel price should match API data. API: ${heartPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Advanced Heart Health Panel")
        assert(descriptionMatches) { 
            "Advanced Heart Health Panel description should match API data" 
        }
        
        labTestsPage.takeScreenshot("advanced-heart-health-panel-verified")
    }

    @Test
    fun `should interact with Advanced Heart Health Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(7)
        labTestsPage.clickAdvancedHeartHealthPanelImage()
        labTestsPage.clickAdvancedHeartHealthPanelHeading()
        labTestsPage.clickBloodTestNth(4)
        labTestsPage.clickAdvancedHeartHealthPanelDescription()
        labTestsPage.clickPriceByNth(5)
        labTestsPage.clickViewDetailsByIndex(5)
        
        labTestsPage.takeScreenshot("advanced-heart-health-panel-interacted")
    }

    // ---------------------- Essential Nutrients Panel Tests ----------------------

    @Test
    fun `should verify Essential Nutrients Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val nutrientsPanel = labTestsPage.getTestPanelFromApi("Essential Nutrients Panel")
        assert(nutrientsPanel != null) { "Essential Nutrients Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Essential Nutrients Panel")) { 
            "Essential Nutrients Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Essential Nutrients Panel")) { 
            "Essential Nutrients Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Essential Nutrients Panel")
        assert(priceMatches) { 
            "Essential Nutrients Panel price should match API data. API: ${nutrientsPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Essential Nutrients Panel")
        assert(descriptionMatches) { 
            "Essential Nutrients Panel description should match API data" 
        }
        
        labTestsPage.takeScreenshot("essential-nutrients-panel-verified")
    }

    @Test
    fun `should interact with Essential Nutrients Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(8)
        labTestsPage.clickEssentialNutrientsPanelImage()
        labTestsPage.clickEssentialNutrientsPanelHeading()
        labTestsPage.clickBloodTestExactNth(4)
        labTestsPage.clickEssentialNutrientsPanelDescription()
        labTestsPage.clickEssentialNutrientsPanelPrice()
        labTestsPage.clickViewDetailsInGridItem(8)
        
        labTestsPage.takeScreenshot("essential-nutrients-panel-interacted")
    }

    // ---------------------- Thyroid Health Panel Tests ----------------------

    @Test
    fun `should verify Thyroid Health Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val thyroidHealthPanel = labTestsPage.getTestPanelFromApi("Thyroid Health Panel")
        assert(thyroidHealthPanel != null) { "Thyroid Health Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Thyroid Health Panel")) { 
            "Thyroid Health Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Thyroid Health Panel")) { 
            "Thyroid Health Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Thyroid Health Panel")
        assert(priceMatches) { 
            "Thyroid Health Panel price should match API data. API: ${thyroidHealthPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Thyroid Health Panel")
        assert(descriptionMatches) { 
            "Thyroid Health Panel description should match API data" 
        }
        
        labTestsPage.takeScreenshot("thyroid-health-panel-verified")
    }

    @Test
    fun `should interact with Thyroid Health Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(9)
        labTestsPage.clickThyroidHealthPanelImage()
        labTestsPage.clickThyroidHealthPanelHeading()
        labTestsPage.clickBloodTestExactNth(5)
        labTestsPage.clickTargetedBloodTestThat()
        labTestsPage.clickThyroidHealthPanelPrice()
        labTestsPage.clickViewDetailsInGridItem(9)
        
        labTestsPage.takeScreenshot("thyroid-health-panel-interacted")
    }

    // ---------------------- Omega Profile Panel Tests ----------------------

    @Test
    fun `should verify Omega Profile Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val omegaPanel = labTestsPage.getTestPanelFromApi("Omega Profile Panel")
        assert(omegaPanel != null) { "Omega Profile Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Omega Profile Panel")) { 
            "Omega Profile Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Omega Profile Panel")) { 
            "Omega Profile Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Omega Profile Panel")
        assert(priceMatches) { 
            "Omega Profile Panel price should match API data. API: ${omegaPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Omega Profile Panel")
        assert(descriptionMatches) { 
            "Omega Profile Panel description should match API data" 
        }
        
        val sampleTypeMatches = labTestsPage.verifyPanelSampleTypeMatchesApi("Omega Profile Panel")
        assert(sampleTypeMatches) { 
            "Omega Profile Panel sample type should match API data. API: ${omegaPanel?.sample_type}" 
        }
        
        labTestsPage.takeScreenshot("omega-profile-panel-verified")
    }

    @Test
    fun `should interact with Omega Profile Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(10)
        labTestsPage.clickOmegaProfilePanelImage()
        labTestsPage.clickOmegaProfilePanelHeading()
        labTestsPage.clickAtHomeTestKitFirst()
        labTestsPage.clickAdvancedFattyAcidBlood()
        labTestsPage.clickOmegaProfilePanelPrice()
        labTestsPage.clickViewDetailsInGridItem(10)
        
        labTestsPage.takeScreenshot("omega-profile-panel-interacted")
    }

    // ---------------------- Stress and Cortisol Rhythm Panel Tests ----------------------

    @Test
    fun `should verify Stress and Cortisol Rhythm Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val cortisolPanel = labTestsPage.getTestPanelFromApi("Stress and Cortisol Rhythm Panel")
        assert(cortisolPanel != null) { "Stress and Cortisol Rhythm Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Stress and Cortisol Rhythm")) { 
            "Stress and Cortisol Rhythm heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Stress and Cortisol Rhythm")) { 
            "Stress and Cortisol Rhythm image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Stress and Cortisol Rhythm Panel")
        assert(priceMatches) { 
            "Stress and Cortisol Rhythm Panel price should match API data. API: ${cortisolPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Stress and Cortisol Rhythm Panel")
        assert(descriptionMatches) { 
            "Stress and Cortisol Rhythm Panel description should match API data" 
        }
        
        val sampleTypeMatches = labTestsPage.verifyPanelSampleTypeMatchesApi("Stress and Cortisol Rhythm Panel")
        assert(sampleTypeMatches) { 
            "Stress and Cortisol Rhythm Panel sample type should match API data. API: ${cortisolPanel?.sample_type}" 
        }
        
        labTestsPage.takeScreenshot("stress-cortisol-rhythm-panel-verified")
    }

    @Test
    fun `should interact with Stress and Cortisol Rhythm Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(11)
        labTestsPage.clickStressAndCortisolRhythmImage()
        labTestsPage.clickStressAndCortisolRhythmHeading()
        labTestsPage.clickAtHomeTestKitNth(1)
        labTestsPage.clickNonInvasiveSalivaTest()
        labTestsPage.clickStressAndCortisolRhythmPrice()
        labTestsPage.clickViewDetailsInGridItem(11)
        
        labTestsPage.takeScreenshot("stress-cortisol-rhythm-panel-interacted")
    }

    // ---------------------- Liver Health Panel Tests ----------------------

    @Test
    fun `should verify Liver Health Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val liverPanel = labTestsPage.getTestProfileFromApi("Liver Health Panel")
        assert(liverPanel != null) { "Liver Health Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Liver Health Panel")) { 
            "Liver Health Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Liver Health Panel")) { 
            "Liver Health Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Liver Health Panel")
        assert(priceMatches) { 
            "Liver Health Panel price should match API data. API: ${liverPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Liver Health Panel")
        assert(descriptionMatches) { 
            "Liver Health Panel description should match API data" 
        }
        
        labTestsPage.takeScreenshot("liver-health-panel-verified")
    }

    @Test
    fun `should interact with Liver Health Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(12)
        labTestsPage.clickLiverHealthPanelImage()
        labTestsPage.clickLiverHealthPanelHeading()
        labTestsPage.clickLiverHealthPanelSampleType()
        labTestsPage.clickLiverHealthPanelDescription()
        labTestsPage.clickLiverHealthPanelPrice()
        labTestsPage.clickViewDetailsInGridItem(12)
        
        labTestsPage.takeScreenshot("liver-health-panel-interacted")
    }

    // ---------------------- Toxic Metals Panel Tests ----------------------

//    @Test
//    fun `should verify Toxic Metals Panel UI matches API data`() {
//        val labTestsPage = navigateToDiagnosticsPage()
//        labTestsPage.waitForPageLoad()
//        labTestsPage.waitForTestPanelsToLoad()
//
//        val toxicMetalsPanel = labTestsPage.getTestProfileFromApi("Toxic Metals Panel")
//        assert(toxicMetalsPanel != null) { "Toxic Metals Panel should exist in API data" }
//
//        assert(labTestsPage.isTestPanelNameVisible("Toxic Metals Panel")) {
//            "Toxic Metals Panel heading should be visible"
//        }
//        assert(labTestsPage.isTestPanelImageVisible("Toxic Metals Panel")) {
//            "Toxic Metals Panel image should be visible"
//        }
//
//        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Toxic Metals Panel")
//        assert(priceMatches) {
//            "Toxic Metals Panel price should match API data. API: ${toxicMetalsPanel?.product?.price}"
//        }
//
//        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Toxic Metals Panel")
//        assert(descriptionMatches) {
//            "Toxic Metals Panel description should match API data"
//        }
//
//        labTestsPage.takeScreenshot("toxic-metals-panel-verified")
//    }

    @Test
    fun `should interact with Toxic Metals Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(13)
        labTestsPage.clickToxicMetalsPanelImage()
        labTestsPage.clickToxicMetalsPanelHeading()
        labTestsPage.clickToxicMetalsPanelSampleType()
        labTestsPage.clickToxicMetalsPanelDescription()
        labTestsPage.clickToxicMetalsPanelPrice()
        labTestsPage.clickViewDetailsInGridItem(13)
        
        labTestsPage.takeScreenshot("toxic-metals-panel-interacted")
    }

    // ---------------------- Blood Health Panel Tests ----------------------

    @Test
    fun `should verify Blood Health Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        val bloodHealthPanel = labTestsPage.getTestProfileFromApi("Blood Health Panel")
        assert(bloodHealthPanel != null) { "Blood Health Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Blood Health Panel")) { 
            "Blood Health Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Blood Health Panel")) { 
            "Blood Health Panel image should be visible" 
        }
        
        val priceMatches = labTestsPage.verifyPanelPriceMatchesApi("Blood Health Panel")
        assert(priceMatches) { 
            "Blood Health Panel price should match API data. API: ${bloodHealthPanel?.product?.price}" 
        }
        
        val descriptionMatches = labTestsPage.verifyPanelDescriptionMatchesApi("Blood Health Panel")
        assert(descriptionMatches) { 
            "Blood Health Panel description should match API data" 
        }
        
        labTestsPage.takeScreenshot("blood-health-panel-verified")
    }

    @Test
    fun `should interact with Blood Health Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(14)
        labTestsPage.clickBloodHealthPanelImage()
        labTestsPage.clickBloodHealthPanelHeading()
        labTestsPage.clickBloodHealthPanelSampleType()
        labTestsPage.clickBloodHealthPanelDescription()
        labTestsPage.clickBloodHealthPanelPrice()
        labTestsPage.clickViewDetailsInGridItem(14)
        
        labTestsPage.takeScreenshot("blood-health-panel-interacted")
    }

    // ---------------------- Allergies Test Panel Tests ----------------------

    @Test
    fun `should verify Allergies Test Panel UI matches API data`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        // Allergies Test Panel is in tests array, not packages or test_profiles
        // We'll verify it exists in the API response
        val apiData = labTestsPage.fetchLabTestDataFromApi()
        assert(apiData != null) { "API data should be fetched successfully" }
        
        val allergiesTest = apiData?.data?.diagnostic_product_list?.tests?.find { 
            it.name == "Allergies Test Panel" 
        }
        assert(allergiesTest != null) { "Allergies Test Panel should exist in API data" }
        
        assert(labTestsPage.isTestPanelNameVisible("Allergies Test Panel")) { 
            "Allergies Test Panel heading should be visible" 
        }
        assert(labTestsPage.isTestPanelImageVisible("Allergies Test Panel")) { 
            "Allergies Test Panel image should be visible" 
        }
        
        // Verify price if available
        val price = allergiesTest?.product?.price
        if (price != null) {
            assert(labTestsPage.isPriceVisible("₹12,999") || labTestsPage.isPriceVisible(formatPrice(price))) { 
                "Allergies Test Panel price should be visible. API: $price" 
            }
        }
        
        labTestsPage.takeScreenshot("allergies-test-panel-verified")
    }

    @Test
    fun `should interact with Allergies Test Panel card`() {
        val labTestsPage = navigateToDiagnosticsPage()
        labTestsPage.waitForPageLoad()
        labTestsPage.waitForTestPanelsToLoad()
        
        labTestsPage.clickGridItemByIndex(15)
        labTestsPage.clickAllergiesTestPanelImage()
        labTestsPage.clickAllergiesTestPanelHeading()
        labTestsPage.clickAllergiesTestPanelSampleType()
        labTestsPage.clickAllergiesTestPanelDescription()
        labTestsPage.clickAllergiesTestPanelPrice()
        labTestsPage.clickViewDetailsInGridItem(15)
        
        labTestsPage.takeScreenshot("allergies-test-panel-interacted")
    }

    // ---------------------- Helper Functions ----------------------

    private fun formatPrice(price: String): String {
        return try {
            val priceNum = price.replace(".00", "").toDoubleOrNull() ?: return price
            "₹${String.format("%,.0f", priceNum)}"
        } catch (e: Exception) {
            price
        }
    }
}

