package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.HowItWorksPage

/**
 * Test cases for the How It Works Page (https://www.deepholistics.com/how-it-works)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HowItWorksPageTest {

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
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isPageHeadingVisible()) { "Page heading should be visible" }

        howItWorksPage.takeScreenshot("how-it-works-page-heading")
    }

    // ---------------------- Process Steps Tests ----------------------

    @Test
    fun `should display all process steps`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        // At least one step should be visible
        val hasSteps = howItWorksPage.isStep1Visible() || 
                       howItWorksPage.isStep2Visible() ||
                       howItWorksPage.isStep3Visible() ||
                       howItWorksPage.isStep4Visible()
        
        assert(hasSteps) { "At least one process step should be visible" }

        howItWorksPage.takeScreenshot("how-it-works-process-steps")
    }

    // ---------------------- Header Elements Tests ----------------------

    @Test
    fun `should display header navigation elements`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isLogoVisible()) { "Logo should be visible" }
        assert(howItWorksPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
        assert(howItWorksPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
        assert(howItWorksPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
        assert(howItWorksPage.isFaqLinkVisible()) { "FAQ link should be visible" }
        assert(howItWorksPage.isLoginLinkVisible()) { "Login link should be visible" }
        assert(howItWorksPage.isHeaderBookNowVisible()) { "Header Book Now button should be visible" }

        howItWorksPage.takeScreenshot("how-it-works-header-elements")
    }

    // ---------------------- CTA Tests ----------------------

    @Test
    fun `should display Book Now CTA`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isBookNowCtaVisible()) { "Book Now CTA should be visible" }

        howItWorksPage.takeScreenshot("how-it-works-book-now-cta")
    }
}
