package website.test

import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.LandingPage

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HeaderAndFooterTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

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

    // ---------------------- Header Tests ----------------------

    @Test
    fun `should navigate to How It Works page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isHowItWorksPageVisible())

        val howItWorksPage = landingPage.headerSection.navigateToHowItWorksPage()
        assert(howItWorksPage.isPageHeadingVisible())
    }

    @Test
    fun `should navigate to What We Test page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isWhatWeTestPageVisible())

        val whatWeTestPage = landingPage.headerSection.navigateToWhatWeTestPage()
        assert(whatWeTestPage.isPageHeadingVisible()) { "What We Test heading should be visible" }
    }

    @Test
    fun `should navigate to Our Why page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isOurWhyPageVisible())

        val ourWhyPage = landingPage.headerSection.navigateToOurWhyPage()
        assert(ourWhyPage.isPageHeadingVisible()) { "Our Why heading should be visible" }
    }

    @Test
    fun `should navigate to FAQ page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isFaqPageVisible())

        val faqPage = landingPage.headerSection.navigateToFaq()
        assert(faqPage.isPageHeadingVisible()) { "FAQ heading should be visible" }
    }

    @Test
    fun `should navigate to Login page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isLoginVisible())

        landingPage.headerSection.navigateToLogin()
        page.waitForURL(TestConfig.Urls.LOGIN_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.LOGIN_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

    @Test
    fun `should navigate to book now page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isBookNowVisible())

        landingPage.headerSection.navigateToBookNow()
        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

    @Test
    fun `should navigate to landing page from header`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.headerSection.isLandingPageVisible())

        val homePage = landingPage.headerSection.navigateToLanding()
        assert(homePage.isHeroHeadingVisible()) { "Home heading should be visible" }
    }


    // ---------------------- Cross-Page Navigation Tests ----------------------

    @Test
    fun `should navigate between all pages in sequence`() {
        // Start at landing page
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        // Landing -> How It Works
        val howItWorksPage = landingPage.headerSection.navigateToHowItWorksPage()
        assert(howItWorksPage.isPageHeadingVisible()) { "How It Works heading should be visible" }

        // How It Works -> What We Test
        val whatWeTestPage = landingPage.headerSection.navigateToWhatWeTestPage()
        assert(whatWeTestPage.isPageHeadingVisible()) { "What We Test heading should be visible" }

        // What We Test -> Our Why
        val ourWhyPage = landingPage.headerSection.navigateToOurWhyPage()
        assert(ourWhyPage.isPageHeadingVisible()) { "Our Why heading should be visible" }

        // Our Why -> FAQ
        val faqPage = landingPage.headerSection.navigateToFaq()
        assert(faqPage.isPageHeadingVisible()) { "FAQ heading should be visible" }

        val backToLandingPage = landingPage.headerSection.navigateToLanding()
        assert(backToLandingPage.isHeroHeadingVisible()) { "Back to Landing heading should be visible" }
    }



    // ---------------------- Footer Tests ----------------------

    @Test
    fun `should display footer elements`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.footerSection.isDeeHolisticsNameVisible()) { "Should display deepholistics" }
        assert(landingPage.footerSection.isInstagramNameVisible()) { "Should display instagram" }
        assert(landingPage.footerSection.isLinkedInVisible()) { "Should display linkedin" }
        assert(landingPage.footerSection.isSingaporeAddressVisible()) { "Should display singapore" }
        assert(landingPage.footerSection.isBengaluruAddressVisible()) { "Should display bengaluru" }
        assert(landingPage.footerSection.isTermsOfServiceVisible()) { "Should display terms of service" }
        assert(landingPage.footerSection.isPrivacyPolicyVisible()) { "Should display privacy policy" }
        assert(landingPage.footerSection.isAboutDeeHolisticsVisible()) { "Should display about deeholistics" }
        assert(landingPage.footerSection.isAllRightsReservedVisible()) { "Should display all rights reserved" }
        assert(landingPage.footerSection.isHowItWorksMenuVisible()) { "Should display how it works" }
        assert(landingPage.footerSection.isWhatWhatWeTestMenuVisible()) { "Should display what what" }
        assert(landingPage.footerSection.isOurWhyMenuVisible()) { "Should display our why" }
        assert(landingPage.footerSection.isMyDashBoardVisible()) { "Should display my dash board" }
        assert(landingPage.footerSection.isBookNowVisible()) { "Should display book" }
        assert(landingPage.footerSection.isCareersVisible()) { "Should display careers" }
    }

    @Test
    fun `should navigate to how it works page from footer menu`() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val howItWorksPage = landingPage.footerSection.navigateToHowItWorksPage()
        assert(howItWorksPage.isPageHeadingVisible()) { "howItWorksPage heading should be visible" }
    }

    @Test
    fun `should navigate to what we test page from footer menu`() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val whatWeTestPage = landingPage.footerSection.navigateToWhatWeTestPage()
        assert(whatWeTestPage.isPageHeadingVisible()) { "whatWeTestPage heading should be visible" }
    }

    @Test
    fun `should navigate to our why page from footer menu`() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val ourWhyPage = landingPage.footerSection.navigateToOurWhyPage()
        assert(ourWhyPage.isPageHeadingVisible()) { "ourWhyPage heading should be visible" }

    }

    @Test
    fun `should navigate to dashboard from footer menu`() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        landingPage.footerSection.navigateToMyDashboard()

        page.waitForURL(TestConfig.Urls.LOGIN_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.LOGIN_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

    @Test
    fun `should navigate to book now page from footer menu`() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        landingPage.footerSection.navigateToBookNow()

        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

}
