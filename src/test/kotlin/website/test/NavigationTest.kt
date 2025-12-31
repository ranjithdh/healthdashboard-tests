package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.LandingPage

/**
 * Navigation Tests for DeepHolistics Marketing Website
 * Tests header and footer navigation across all pages
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NavigationTest {

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

    // ---------------------- Header Navigation Tests ----------------------

    @Test
    fun `should navigate to How It Works page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val howItWorksPage = landingPage.clickHowItWorksLink()
        
        page.waitForURL("**/how-it-works**")
        assert(page.url().contains("how-it-works")) { "Should be on How It Works page" }
        assert(howItWorksPage.isPageHeadingVisible()) { "How It Works heading should be visible" }

        howItWorksPage.takeScreenshot("navigation-to-how-it-works")
    }

    @Test
    fun `should navigate to What We Test page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val whatWeTestPage = landingPage.clickWhatWeTestLink()
        
        page.waitForURL("**/what-we-test**")
        assert(page.url().contains("what-we-test")) { "Should be on What We Test page" }
        assert(whatWeTestPage.isPageHeadingVisible()) { "What We Test heading should be visible" }

        whatWeTestPage.takeScreenshot("navigation-to-what-we-test")
    }

    @Test
    fun `should navigate to Our Why page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val ourWhyPage = landingPage.clickOurWhyLink()
        
        page.waitForURL("**/our-why**")
        ourWhyPage.waitForPageLoad()

        assert(page.url().contains("our-why")) { "Should be on Our Why page" }
        assert(ourWhyPage.isHederVisible()) { "Our Why heading should be visible" }

        ourWhyPage.takeScreenshot("navigation-to-our-why")
    }

    @Test
    fun `should navigate to FAQ page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val faqPage = landingPage.clickFaqLink()
        
        page.waitForURL("**/faq**")
        assert(page.url().contains("faq")) { "Should be on FAQ page" }
        assert(faqPage.isPageHeadingVisible()) { "FAQ heading should be visible" }

        faqPage.takeScreenshot("navigation-to-faq")
    }

    @Test
    fun `should navigate to Login page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        landingPage.clickLoginLink()
        
        page.waitForURL("**/login**")
        assert(page.url().contains("login")) { "Should be on Login page" }

        landingPage.takeScreenshot("navigation-to-login")
    }

    @Test
    fun `should navigate to home when clicking logo`() {
        // Start from a different page
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()
        
        val howItWorksPage = landingPage.clickHowItWorksLink()
        page.waitForURL("**/how-it-works**")

        // Now click the logo to go back home
        howItWorksPage.clickLogo()
        
        page.waitForURL("**/deepholistics.com/**")
        // Home page URL should be the base marketing URL (without path or just /)
        val currentUrl = page.url()
        assert(
            currentUrl.endsWith("deepholistics.com/") || 
            currentUrl.endsWith("deepholistics.com") ||
            !currentUrl.contains("/how-it-works")
        ) { "Should be on home page, but was: $currentUrl" }

        landingPage.takeScreenshot("navigation-logo-to-home")
    }

    // ---------------------- Header Book Now Tests ----------------------

    @Test
    fun `should navigate to app when clicking header Book Now`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        landingPage.clickHeaderBookNow()
        
        page.waitForURL("**/app.deepholistics.com/**")
        assert(page.url().contains("app.deepholistics.com")) { "Should navigate to app domain" }

        landingPage.takeScreenshot("navigation-header-book-now")
    }

    // ---------------------- Cross-Page Navigation Tests ----------------------

    @Test
    fun `should navigate between all pages in sequence`() {
        // Start at landing page
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()
        
        // Landing -> How It Works
        val howItWorksPage = landingPage.clickHowItWorksLink()
        page.waitForURL("**/how-it-works**")
        assert(howItWorksPage.isPageHeadingVisible()) { "How It Works heading should be visible" }
        
        // How It Works -> What We Test
        val whatWeTestPage = howItWorksPage.clickWhatWeTestLink()
        page.waitForURL("**/what-we-test**")
        assert(whatWeTestPage.isPageHeadingVisible()) { "What We Test heading should be visible" }
        
        // What We Test -> Our Why
        val ourWhyPage = whatWeTestPage.clickOurWhyLink()
        page.waitForURL("**/our-why**")
        ourWhyPage.waitForPageLoad()
        assert(ourWhyPage.isHederVisible()) { "Our Why heading should be visible" }
        
        // Our Why -> FAQ
        val faqPage = ourWhyPage.clickFaqLink()
        page.waitForURL("**/faq**")
        assert(faqPage.isPageHeadingVisible()) { "FAQ heading should be visible" }
        
        // FAQ -> Home (via logo)
        faqPage.clickLogo()
        page.waitForURL("**/deepholistics.com/**")

        landingPage.takeScreenshot("navigation-full-sequence")
    }
}
