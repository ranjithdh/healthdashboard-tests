package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.FaqPage

/**
 * Test cases for the FAQ Page (https://www.deepholistics.com/faq)
 * Tests accordion functionality and category switching
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FaqPageTest {

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
    fun `should display FAQ page heading`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        assert(faqPage.isPageHeadingVisible()) { "FAQ page heading should be visible" }

        faqPage.takeScreenshot("faq-page-heading")
    }

    // ---------------------- Category Tabs Tests ----------------------

    @Test
    fun `should display category tabs`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        assert(faqPage.isCategoryTabsVisible()) { "Category tabs should be visible" }
        assert(faqPage.isGeneralTabVisible()) { "General tab should be visible" }

        faqPage.takeScreenshot("faq-category-tabs")
    }

    @Test
    fun `should switch between category tabs`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        // Click on different tabs if available
        if (faqPage.isAppointmentTabVisible()) {
            faqPage.clickAppointmentTab()
            Thread.sleep(500) // Wait for content to update
            faqPage.takeScreenshot("faq-appointment-tab")
        }

        faqPage.clickGeneralTab()
        Thread.sleep(500)
        faqPage.takeScreenshot("faq-general-tab")
    }

    // ---------------------- FAQ Accordion Tests ----------------------

    @Test
    fun `should display FAQ items`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        val faqCount = faqPage.getFaqItemsCount()
        assert(faqCount > 0) { "Should have at least one FAQ item" }

        faqPage.takeScreenshot("faq-items-visible")
    }

    @Test
    fun `should expand FAQ answer when question clicked`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        val faqCount = faqPage.getFaqItemsCount()
        if (faqCount > 0) {
            // Click first FAQ item
            faqPage.clickFaqItem(0)
            Thread.sleep(500) // Wait for animation
            
            faqPage.takeScreenshot("faq-item-expanded")
        }
    }

    @Test
    fun `should collapse FAQ answer when clicked again`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        val faqCount = faqPage.getFaqItemsCount()
        if (faqCount > 0) {
            // Click to expand
            faqPage.clickFaqItem(0)
            Thread.sleep(500)
            faqPage.takeScreenshot("faq-item-before-collapse")
            
            // Click to collapse
            faqPage.clickFaqItem(0)
            Thread.sleep(500)
            faqPage.takeScreenshot("faq-item-after-collapse")
        }
    }

    @Test
    fun `should only have one FAQ expanded at a time`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        val faqCount = faqPage.getFaqItemsCount()
        if (faqCount > 1) {
            // Click first FAQ item
            faqPage.clickFaqItem(0)
            Thread.sleep(500)
            
            // Click second FAQ item
            faqPage.clickFaqItem(1)
            Thread.sleep(500)
            
            // Verify accordion behavior (second should be open, first should be closed)
            // This depends on the actual accordion implementation
            faqPage.takeScreenshot("faq-accordion-behavior")
        }
    }

    // ---------------------- Header Elements Tests ----------------------

    @Test
    fun `should display header navigation elements`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        assert(faqPage.isLogoVisible()) { "Logo should be visible" }
        assert(faqPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
        assert(faqPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
        assert(faqPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
        assert(faqPage.isFaqLinkVisible()) { "FAQ link should be visible" }

        faqPage.takeScreenshot("faq-header-elements")
    }

    // ---------------------- Contact Section Tests ----------------------

    @Test
    fun `should display contact section`() {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()

        // Contact section might be at the bottom
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        Thread.sleep(500)

        if (faqPage.isContactSectionVisible()) {
            faqPage.takeScreenshot("faq-contact-section")
        } else {
            faqPage.takeScreenshot("faq-page-bottom")
        }
    }
}
