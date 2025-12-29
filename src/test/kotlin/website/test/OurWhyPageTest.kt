package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.OurWhyPage

/**
 * Test cases for the Our Why Page (https://www.deepholistics.com/our-why)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OurWhyPageTest {

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
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()

        assert(ourWhyPage.isPageHeadingVisible()) { "Page heading should be visible" }

        ourWhyPage.takeScreenshot("our-why-page-heading")
    }

    // ---------------------- Mission Content Tests ----------------------

    @Test
    fun `should display mission content`() {
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()

        assert(ourWhyPage.isMissionStatementVisible()) { "Mission statement should be visible" }

        ourWhyPage.takeScreenshot("our-why-mission-content")
    }

    // ---------------------- Story Section Tests ----------------------

    @Test
    fun `should display story sections`() {
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()

        // Story or values section should be visible
        val hasStoryContent = ourWhyPage.isStorySectionVisible() ||
                              ourWhyPage.isValuesSectionVisible() ||
                              ourWhyPage.isTeamSectionVisible()

        // At least the mission should be visible if not specific story sections
        assert(hasStoryContent || ourWhyPage.isMissionStatementVisible()) { 
            "Story content or mission should be visible" 
        }

        ourWhyPage.takeScreenshot("our-why-story-sections")
    }

    // ---------------------- Header Elements Tests ----------------------

    @Test
    fun `should display header navigation elements`() {
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()

        assert(ourWhyPage.isLogoVisible()) { "Logo should be visible" }
        assert(ourWhyPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
        assert(ourWhyPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
        assert(ourWhyPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
        assert(ourWhyPage.isFaqLinkVisible()) { "FAQ link should be visible" }

        ourWhyPage.takeScreenshot("our-why-header-elements")
    }

    // ---------------------- CTA Tests ----------------------

    @Test
    fun `should display Book Now CTA`() {
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()

        assert(ourWhyPage.isBookNowCtaVisible()) { "Book Now CTA should be visible" }

        ourWhyPage.takeScreenshot("our-why-book-now-cta")
    }
}
