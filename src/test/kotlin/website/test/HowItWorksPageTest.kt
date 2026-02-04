package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.HowItWorksPage

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HowItWorksPageTest : BaseTest() {

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


    @Test
    fun `should display page heading and description`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isPageHeadingVisible()) { "Page heading should be visible" }
        assert(howItWorksPage.isFromTestToClarifyDescriptionVisible()) { "Page Description should be visible" }
    }


    @Test
    fun `should display step 1 elements`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        howItWorksPage.waitForStep1Title()
        assert(howItWorksPage.isStep1Visible()) { "Step 1 should be visible" }
        assert(howItWorksPage.isStep1TitleVisible()) { "Step 1 Title should be visible" }
        assert(howItWorksPage.isStep1DescriptionVisible()) { "Step 1 Description should be visible" }

        assert(howItWorksPage.isStep1Point1TitleVisible()) { "Step 1 point 1 title should be visible" }
        assert(howItWorksPage.isStep1Point1DescriptionVisible()) { "Step 1 point 1 description should be visible" }

        assert(howItWorksPage.isStep1Point2TitleVisible()) { "Step 1 point 2 title should be visible" }
        assert(howItWorksPage.isStep1Point2DescriptionVisible()) { "Step 1 point 2 description should be visible" }


        assert(howItWorksPage.isStep1Point3TitleVisible()) { "Step 1 point 3 title should be visible" }
        assert(howItWorksPage.isStep1Point3DescriptionVisible()) { "Step 1 point 3 description should be visible" }
    }


    @Test
    fun `should display step 2 elements`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        howItWorksPage.waitForStep2Title()
        assert(howItWorksPage.isStep2Visible()) { "Step 2 should be visible" }
        assert(howItWorksPage.isStep2TitleVisible()) { "Step 2 Title should be visible" }
        assert(howItWorksPage.isStep2DescriptionVisible()) { "Step 2 Description should be visible" }

        assert(howItWorksPage.isStep2Point1TitleVisible()) { "Step 2 point 1 title should be visible" }
        assert(howItWorksPage.isStep2Point1DescriptionVisible()) { "Step 2 point 1 description should be visible" }

        assert(howItWorksPage.isStep2Point2TitleVisible()) { "Step 2 point 2 title should be visible" }
        assert(howItWorksPage.isStep2Point2DescriptionVisible()) { "Step 2 point 2 description should be visible" }


        assert(howItWorksPage.isStep2Point3TitleVisible()) { "Step 2 point 3 title should be visible" }
        assert(howItWorksPage.isStep2Point3DescriptionVisible()) { "Step 2 point 3 description should be visible" }
    }


    @Test
    fun `should display step 3 elements`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        howItWorksPage.waitForStep3Title()
        assert(howItWorksPage.isStep3Visible()) { "Step 3 should be visible" }
        assert(howItWorksPage.isStep3TitleVisible()) { "Step 3 Title should be visible" }
        assert(howItWorksPage.isStep3DescriptionVisible()) { "Step 3 Description should be visible" }

        assert(howItWorksPage.isStep3Point1TitleVisible()) { "Step 3 point 1 title should be visible" }
        assert(howItWorksPage.isStep3Point1DescriptionVisible()) { "Step 3 point 1 description should be visible" }

        assert(howItWorksPage.isStep3Point2TitleVisible()) { "Step 3 point 2 title should be visible" }
        assert(howItWorksPage.isStep3Point2DescriptionVisible()) { "Step 3 point 2 description should be visible" }


        assert(howItWorksPage.isStep3Point3TitleVisible()) { "Step 3 point 3 title should be visible" }
        assert(howItWorksPage.isStep3Point3DescriptionVisible()) { "Step 3 point 3 description should be visible" }
    }

    @Test
    fun `should display all the add on test`() {
        val landingPage = HowItWorksPage(page).navigate() as HowItWorksPage
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
    fun `should show everything you need to know your baseline`() {
        val landingPage = HowItWorksPage(page).navigate() as HowItWorksPage
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
    fun `should show stop guessing elements`() {

        val landingPage = HowItWorksPage(page).navigate() as HowItWorksPage
        landingPage.waitForPageLoad()

        assert(landingPage.stopGuessingStartWithClaritySection.stopGuessingSectionElementsVisible())
        assert(landingPage.stopGuessingStartWithClaritySection.stopGuessingBookNowButtonVisible())

        landingPage.stopGuessingStartWithClaritySection.clickStopGuessingBookNowButtonVisible()

        page.waitForURL(TestConfig.Urls.SIGNUP_VIA_WEBSITE)
        assert(page.url().contains(TestConfig.Urls.SIGNUP_VIA_WEBSITE)) { "Should navigate to app domain" }
    }

}
