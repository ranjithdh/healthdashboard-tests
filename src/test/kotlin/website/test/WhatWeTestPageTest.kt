package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.WhatWeTestPage

/**
 * Test cases for the What We Test Page (https://www.deepholistics.com/what-we-test)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WhatWeTestPageTest {

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
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    // ---------------------- Page Load Tests ----------------------

    @Test
    fun `should display page heading`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isPageHeadingVisible()) { "Page heading should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-page-heading")
    }

    // ---------------------- Biomarker Description Tests ----------------------

    @Test
    fun `should display biomarkers description`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isBiomarkersDescriptionVisible()) { "Biomarkers description should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-biomarkers-description")
    }

    // ---------------------- Test Categories Tests ----------------------

    @Test
    fun `should display test categories`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        // At least one category should be visible
        val hasCategories = whatWeTestPage.isBloodTestCategoryVisible() ||
                           whatWeTestPage.isGeneticTestCategoryVisible() ||
                           whatWeTestPage.isGutTestCategoryVisible()

        assert(hasCategories) { "At least one test category should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-categories")
    }

    // ---------------------- Biomarker Sections Tests ----------------------

    @Test
    fun `should display biomarker sections`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        // At least one biomarker section should be visible
        val hasSections = whatWeTestPage.isHeartHealthSectionVisible() ||
                         whatWeTestPage.isThyroidSectionVisible() ||
                         whatWeTestPage.isLiverSectionVisible() ||
                         whatWeTestPage.isKidneySectionVisible() ||
                         whatWeTestPage.isNutrientsSectionVisible()

        assert(hasSections) { "At least one biomarker section should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-biomarker-sections")
    }

    // ---------------------- Header Elements Tests ----------------------

    @Test
    fun `should display header navigation elements`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isLogoVisible()) { "Logo should be visible" }
        assert(whatWeTestPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
        assert(whatWeTestPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
        assert(whatWeTestPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
        assert(whatWeTestPage.isFaqLinkVisible()) { "FAQ link should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-header-elements")
    }

    // ---------------------- CTA Tests ----------------------

    @Test
    fun `should display Book Now CTA`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isBookNowCtaVisible()) { "Book Now CTA should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-book-now-cta")
    }
}
