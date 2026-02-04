package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.LandingPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LandingPageTest : BaseTest() {

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
        val viewport = TestConfig.Viewports.DESKTOP_HD
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

//        assert(landingPage.isLogoVisible()) { "Logo should be visible" }
//        assert(landingPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
//        assert(landingPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
//        assert(landingPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
//        assert(landingPage.isFaqLinkVisible()) { "FAQ link should be visible" }
//        assert(landingPage.isLoginLinkVisible()) { "Login link should be visible" }
//        assert(landingPage.isHeaderBookNowVisible()) { "Header Book Now button should be visible" }

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
        assert(landingPage.isBaselineVisible()) { "Baseline Title should be visible" }
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


        assert(landingPage.addOnTestCards.isAddOnTestHeadingVisible()) { "AddOn Test Heading should be visible" }
        assert(landingPage.addOnTestCards.isAddOnTestDescriptionVisible()) { "AddOn Test Description should be visible" }

        assert(landingPage.addOnTestCards.isAllergyVisible()) { "Allergy test should be visible" }
        assert(landingPage.addOnTestCards.isGutMicrobiomeVisible()) { "GutTest should be visible" }
        assert(landingPage.addOnTestCards.isStressAndCortisolVisible()) { "StressAndCortisol should be visible" }
        assert(landingPage.addOnTestCards.isGeneVisible()) { "Gene should be visible" }
        assert(landingPage.addOnTestCards.isOmegaVisible()) { "Omega should be visible" }
        assert(landingPage.addOnTestCards.isToxicMetalsVisible()) { "ToxicTest should be visible" }
        assert(landingPage.addOnTestCards.isThyroidHealthVisible()) { "ThyroidHealth should be visible" }
        assert(landingPage.addOnTestCards.isWomensHealthVisible()) { "Women health should be visible" }
        assert(landingPage.addOnTestCards.isEssentialNutrientsVisible()) { "EssentialNutrients should be visible" }
        assert(landingPage.addOnTestCards.isAdvancedThyroidVisible()) { "Advanced thyroid should be visible" }
        assert(landingPage.addOnTestCards.isLiverHealthVisible()) { "Liver health should be visible" }
        assert(landingPage.addOnTestCards.isAutoImmuneVisible()) { "Auto immune should be visible" }
        assert(landingPage.addOnTestCards.isAdvancedHeartHealthVisible()) { "Advance heart health should be visible" }
        assert(landingPage.addOnTestCards.isWomensFertilityVisible()) { "Women fertility should be visible" }
        assert(landingPage.addOnTestCards.isBloodHealthVisible()) { "Blood health should be visible" }

        assert(landingPage.addOnTestCards.isViewAllAddOnTestButtonVisible()) { "ViewAllAddOnTest Button should be visible" }

        val allTestPage = landingPage.addOnTestCards.clickViewAllAddOnTestButton()
        assert(allTestPage.isPageHeadingVisible()) { "Page Heading should be visible" }
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

        landingPage.clickWhatIsIncludedSectionBookNowButton()
        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

    @Test
    fun `should show everything you need to know your baseline`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.everyThingYouNeedToKnowCard.isEverythingYouNeedToKnowHeadingVisible()) { "Everything you need to know heading should be visible" }
        assert(landingPage.everyThingYouNeedToKnowCard.isEverythingYouNeedToKnowDescriptionVisible()) { "Everything you need to know description should be visible" }
        assert(landingPage.everyThingYouNeedToKnowCard.isWhatsIncludedPointsVisible()) { "What's included points should be visible" }
        assert(landingPage.everyThingYouNeedToKnowCard.isEveryThingYouNeedToKnowBookNowVisible()) { "Everything you need to know book now should be visible" }


        landingPage.everyThingYouNeedToKnowCard.clickEveryThingYouNeedToKnowBookNow()
        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
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
    fun `should display all the elements in the word from our founder section`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.isWordFromOurFounderHeadingVisible()) { "Word from the our founder section should be visible" }
        assert(landingPage.isWhyWeBuiltTextVisible()) { "Why we built-text should be visible" }
        assert(landingPage.isCeoNameVisible()) { "CeoName should be visible" }
        assert(landingPage.isWordFromOurFounderSectionReadOurWhyButtonVisible()) { "Word from the our founder section should be visible" }

        val ourWhyPage = landingPage.clickWordFromOurFounderSectionReadOurWhyButtonVisible()

        assert(ourWhyPage.isPageHeadingVisible())
    }

    @Test
    fun `should navigate to app when clicking hero Book Now button`() {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        landingPage.clickHeroBookNow()

        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

    @Test
    fun `should show stop guessing elements`() {

        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()

        assert(landingPage.stopGuessingStartWithClaritySection.stopGuessingSectionElementsVisible())
        assert(landingPage.stopGuessingStartWithClaritySection.stopGuessingBookNowButtonVisible())

        landingPage.stopGuessingStartWithClaritySection.clickStopGuessingBookNowButtonVisible()

        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

}
