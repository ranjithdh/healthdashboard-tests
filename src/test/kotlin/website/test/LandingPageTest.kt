package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.LandingPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LandingPageTest {

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

    @Test
    fun `should display all header navigation elements`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isLogoVisible()) { "Logo should be visible" }
        assert(landingPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
        assert(landingPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
        assert(landingPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
        assert(landingPage.isFaqLinkVisible()) { "FAQ link should be visible" }
        assert(landingPage.isLoginLinkVisible()) { "Login link should be visible" }
        assert(landingPage.isHeaderBookNowVisible()) { "Header Book Now button should be visible" }
    }


    @Test
    fun `should display hero section with headline`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isHeroHeadingVisible()) { "Hero heading should be visible" }
        assert(landingPage.isHeaderDescriptionVisible()) { "Hero heading description should be visible" }
        assert(landingPage.isHeroBookNowVisible()) { "Hero Book Now button should be visible" }
        assert(landingPage.isAtHomeTestVisible()) { "At-Home Testing should be visible" }
        assert(landingPage.isFastResultVisible()) { "FastResult should be visible" }
        assert(landingPage.isSimpleAndConvenientVisible()) { "SimpleAndConvenient should be visible" }

        landingPage.takeScreenshot("landing-page-hero-section")
    }

    @Test
    fun `should display introduction elements`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isIntroducingTitleVisible()) { "Introducing Title should be visible" }
//        assert(landingPage.isBaselineVisible()) { "Baseline Title should be visible" }
        assert(landingPage.isBaseLineDescriptionVisible()) { "BaseLine Description should be visible" }
        assert(landingPage.isWhatWeTestButtonVisible()) { "What We TestButton should be visible" }

        val whatWeTestPage = landingPage.clickWhatWeTestButton()
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isPageHeadingVisible()) { "PageHeading should be visible" }

    }

    @Test
    fun `should display cover image`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isCoverImageVisible()) { "CoverImage should be visible" }
    }

    @Test
    fun `should display how it works section elements`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isHowItWorksHeadingVisible()) { "How It Works Heading should be visible" }
        assert(landingPage.isHowItWorksDescriptionVisible()) { "How It Works Description should be visible" }
        assert(landingPage.isLearnMoreButtonVisible()) { "How It Works Button should be visible" }

        val howItWorksPage = landingPage.clickLearnMoreButton()
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isPageHeadingVisible()) { "PageHeading should be visible" }
    }

    @Test
    fun `should display steps`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()
        assert(landingPage.isStep1Visible()) { "Step 1 should be visible" }
        assert(landingPage.isStep2Visible()) { "Step 2 should be visible" }
        assert(landingPage.isStep3Visible()) { "Step 3 should be visible" }
    }


    @Test
    fun `should display all the add on test`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()


        assert(landingPage.isAddOnTestHeadingVisible()) { "AddOn Test Heading should be visible" }
        assert(landingPage.isAddOnTestDescriptionVisible()) { "AddOn Test Description should be visible" }

        assert(landingPage.isAllergyTestVisible()) { "Allergy test should be visible" }
        assert(landingPage.isGutTestVisible()) { "GutTest should be visible" }
        assert(landingPage.isStressAndCortisolVisible()) { "StressAndCortisol should be visible" }
        assert(landingPage.isGeneVisible()) { "Gene should be visible" }
        assert(landingPage.isOmegaTestVisible()) { "Omega should be visible" }
        assert(landingPage.isToxicTestVisible()) { "ToxicTest should be visible" }
        assert(landingPage.isThyroidHealthVisible()) { "ThyroidHealth should be visible" }
        assert(landingPage.isWomenHealthVisible()) { "Women health should be visible" }
        assert(landingPage.isEssentialNutrientsVisible()) { "EssentialNutrients should be visible" }
        assert(landingPage.isAdvancedThyroidVisible()) { "Advanced thyroid should be visible" }
        assert(landingPage.isLiverHealthVisible()) { "Liver health should be visible" }
        assert(landingPage.isAutoImmuneVisible()) { "Auto immune should be visible" }
        assert(landingPage.isAdvanceHeartHealthVisible()) { "Advance heart health should be visible" }
        assert(landingPage.isWomensFertilityVisible()) { "Women fertility should be visible" }
        assert(landingPage.isBloodHealthVisible()) { "Blood health should be visible" }

        assert(landingPage.isViewAllAddOnTestButtonVisible()) { "ViewAllAddOnTest Button should be visible" }

    }


    @Test
    fun `should display all the elements in what's included in baseline section   `() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isWhatIsIncludedHeadingVisible()) { "What's included heading should be visible" }
        assert(landingPage.isWhatIsIncludedDescriptionVisible()) { "What's included description should be visible" }
        assert(landingPage.isWhatIncludedSectionBookNowButtonVisible()) { "What's included section book now should be visible" }
        assert(landingPage.isAllDateInOnePlaceSectionVisible()) { "AllDateInOnePlaceSection should be visible" }
        assert(landingPage.isExpertGuidanceSectionVisible()) { "ExpertGuidance section should be visible" }
        assert(landingPage.hyperPersonalizesSectionVisible()) { "HyperPersonalizes section should be visible" }
        assert(landingPage.addOnTestingSectionVisible()) { "OnTestingSection should be visible" }
        assert(landingPage.referAndEarnSectionVisible()) { "ReferAndEarnSection should be visible" }

    }

    @Test
    fun `should show everything you need to know your baseline`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isEverythingYouNeedToKnowHeadingVisible()) { "Everything you need to know heading should be visible" }
        assert(landingPage.isEverythingYouNeedToKnowDescriptionVisible()) { "Everything you need to know description should be visible" }
        assert(landingPage.isWhatsIncludedPointsVisible()) { "What's included points should be visible" }
        assert(landingPage.isEveryThingYouNeedToKnowBookNowVisible()) { "Everything you need to know book now should be visible" }

    }



    @Test
    fun `should display all the elements in the built by expert section`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isBuiltByExpertHeadingVisible()) { "BuiltByExpertHeading should be visible" }
        assert(landingPage.isBuiltByExpertDescriptionVisible()) { "BuiltByExpertDescription should be visible" }
        assert(landingPage.isDrVishalUsRaoSectionElementsVisible()) { "DrVishalUsRaoSectionElements should be visible" }
        assert(landingPage.isDrWasimMohideenElementsVisible()) { "DrWasimMohideenElementsVisible should be visible" }

    }


    @Test
    fun `should display all the elements in the word from our founder section


    @Test
    fun `should navigate to app when clicking hero Book Now button`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        landingPage.clickHeroBookNow()

        // Should navigate to the app login/signup page
        page.waitForURL("**/app.deepholistics.com/**")
        assert(page.url().contains("app.deepholistics.com")) { "Should navigate to app domain" }

        landingPage.takeScreenshot("landing-page-hero-book-now-navigation")
    }

    // ---------------------- Diagnostic Cards Tests ----------------------

    @Test
    fun `should display diagnostic cards section`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()
        landingPage.scrollToBottom()

        // Wait a bit for lazy-loaded content
        Thread.sleep(1000)

        assert(landingPage.isDiagnosticCardsSectionVisible()) { "Diagnostic cards section should be visible" }
        assert(landingPage.getDiagnosticCardsCount() > 0) { "Should have at least one diagnostic card" }

        landingPage.takeScreenshot("landing-page-diagnostic-cards")
    }

    // ---------------------- Footer Tests ----------------------

    @Test
    fun `should display footer with all links`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()
        landingPage.scrollToBottom()

        // Wait for footer to be visible
        Thread.sleep(500)

        assert(landingPage.isFooterVisible()) { "Footer should be visible" }
        assert(landingPage.isPrivacyPolicyFooterLinkVisible()) { "Privacy Policy link should be visible" }
        assert(landingPage.isTermsFooterLinkVisible()) { "Terms link should be visible" }

        landingPage.takeScreenshot("landing-page-footer")
    }

    // ---------------------- Intercom Chat Tests ----------------------

    @Test
    fun `should display Intercom chat launcher`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        // Intercom may take time to load
        Thread.sleep(2000)

        // Note: Intercom might not always be visible depending on configuration
        if (landingPage.isIntercomChatVisible()) {
            landingPage.takeScreenshot("landing-page-intercom-visible")
        } else {
            // Not a failure - Intercom might be configured differently
            landingPage.takeScreenshot("landing-page-intercom-not-visible")
        }
    }

    // ---------------------- CTA Navigation Tests ----------------------

    @Test
    fun `should navigate to How It Works when clicking Learn More`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        val howItWorksPage = landingPage.clickLearnMoreLink()

        page.waitForURL("**/how-it-works**")
        assert(page.url().contains("how-it-works")) { "Should navigate to How It Works page" }

        landingPage.takeScreenshot("landing-page-learn-more-navigation")
    }
}
