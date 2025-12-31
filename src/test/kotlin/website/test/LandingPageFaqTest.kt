package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.LandingPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LandingPageFaqTest {

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

    fun navigateToLandingPage(): LandingPage {
        val landingPage = LandingPage(page).navigate() as LandingPage
        landingPage.waitForPageLoad()
        return landingPage
    }

    @Test
    fun `should display faq heading`() {
        val landingPage = navigateToLandingPage()
        assert(landingPage.isFaqHeadingVisible()) { "Should display faq heading" }
    }

    @Test
    fun `should display faq tabs`() {
        val landingPage = navigateToLandingPage()

        assert(landingPage.faqSection.isGeneralTabVisible()) { "Should display general tab" }
        assert(landingPage.faqSection.isAppointmentAndProcessVisible()) { "Should display appointment and process tab" }
        assert(landingPage.faqSection.isTestCoverageTabVisible()) { "Should display test coverage" }
        assert(landingPage.faqSection.isPrivacyAndDataSecurityTabVisible()) { "Should display privacy and date" }
        assert(landingPage.faqSection.isConsultTabVisible()) { "Should display consult" }
        assert(landingPage.faqSection.isActionPlanTabVisible()) { "Should display action plan" }
        assert(landingPage.faqSection.isPointsTabVisible()) { "Should display points" }

    }


    @Test
    fun `should display all the general tab questions`() {
        val landingPage = navigateToLandingPage()

        assert(landingPage.faqSection.isWhoHelpsMeUnderstandMyResultQuestionVisible()) { "Should display who helps me" }
        assert(landingPage.faqSection.isDoYouTestEverythingOrOnlyWhatMattersVisible()) { "Should display do you think" }
        assert(landingPage.faqSection.isWhyMostHealthTestsFeelConfusingVisible()) { "Should display why most health tests" }
        assert(landingPage.faqSection.isWhatMakesBaselineDifferentFromOtherBloodTestsVisible()) { "Should display what makes blood" }
        assert(landingPage.faqSection.isWhatExactlyIsBaselineVisible()) { "Should display what exactly is baseline tests" }
        assert(landingPage.faqSection.isWhoIsBaselineForVisible()) { "Should display who is baseline for" }
        assert(landingPage.faqSection.isCanBaselineHelpMeUnderstandWhyIFeelLowEnergyOrUnwellVisible()) { "Should display can baseline help me" }
        assert(landingPage.faqSection.isWhoShouldTakeThisTestVisible()) { "Should display who should take this test" }
        assert(landingPage.faqSection.isIveDoneARecentHealthCheckUpShouldIStillGoForBaselineVisible()) { "Should display who helps me" }
        assert(landingPage.faqSection.isWhenShouldICheckMyBaselineVisible()){"Should display when should i check" }
    }

    @Test
    fun `should display appointment and process tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.faqSection.clickAppointmentAndProcessTab()

        assert(landingPage.faqSection.isWhoToContactForHelpVisible()) {
            "Should display 'Who to contact for help'"
        }

        assert(landingPage.faqSection.isWhatHappensAfterResultsArriveVisible()) {
            "Should display 'What happens after results arrive'"
        }

        assert(landingPage.faqSection.isWhyTwoBloodDrawsVisible()) {
            "Should display 'Why are there two blood draws'"
        }
        assert(landingPage.faqSection.isHowLongDoesBloodDrawTakeVisible()) {
            "Should display 'How long does the blood draw take'"
        }
    }

    @Test
    fun `should display test coverage tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.faqSection.clickTestCoverageTab()

        assert(landingPage.faqSection.isHowAddOnsImproveClarityVisible()) {
            "Should display 'How add ons improve clarity'"
        }
        assert(landingPage.faqSection.isWhetherAddOnsMandatoryVisible()) {
            "Should display 'Whether add ons are mandatory'"
        }
        assert(landingPage.faqSection.isWhatAddOnTestsAvailableVisible()) {
            "Should display 'What add on tests are available'"
        }
        assert(landingPage.faqSection.isCanIAddTestsLaterVisible()) {
            "Should display 'Can I add tests later'"
        }

        assert(landingPage.faqSection.isBaselineMedicallyReliableVisible()) {
            "Should display 'Is Baseline medically reliable'"
        }
    }

    @Test
    fun `should display test privacy and data security tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.faqSection.clickPrivacyAndDataSecurityTab()

        assert(landingPage.faqSection.isWhoCanAccessMyDataVisible()) {
            "Should display 'Who can access my data'"
        }
        assert(landingPage.faqSection.isHowPrivacyProtectedVisible()) {
            "Should display 'How privacy is protected'"
        }
        assert(landingPage.faqSection.isCanIDeleteMyDataVisible()) {
            "Should display 'Can I delete my data'"
        }
        assert(landingPage.faqSection.isWhetherMyDataSharedVisible()) {
            "Should display 'Whether my data is shared'"
        }
        assert(landingPage.faqSection.isHowHealthDataStoredVisible()) {
            "Should display 'How my health data is stored'"
        }
    }

    @Test
    fun `should display consult tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.faqSection.clickConsultTab()

        assert(landingPage.faqSection.isConsultationIncludedVisible()) {
            "Should display 'Is consultation included'"
        }
        assert(landingPage.faqSection.isConsultationDurationVisible()) {
            "Should display 'How long the consultation takes'"
        }
        assert(landingPage.faqSection.isWhatHappensDuringConsultationVisible()) {
            "Should display 'What happens during the consultation'"
        }
        assert(landingPage.faqSection.isHowPersonalisedGuidanceVisible()) {
            "Should display 'How personalised the guidance is'"
        }
        assert(landingPage.faqSection.isWhoConductsConsultationVisible()) {
            "Should display 'Who conducts the consultation'"
        }
    }

    @Test
    fun `should display action plan tab questions`() {
        val landingPage = navigateToLandingPage()
        landingPage.faqSection.clickActionPlanTab()

        assert(landingPage.faqSection.isWhenToRetestVisible()) {
            "Should display 'When to retest'"
        }
        assert(landingPage.faqSection.isWhenAddOnsRecommendedVisible()) {
            "Should display 'When add ons are recommended'"
        }
        assert(landingPage.faqSection.isHowPlanEvolvesVisible()) {
            "Should display 'How the plan evolves over time'"
        }
        assert(landingPage.faqSection.isHowFollowUpTestsSuggestedVisible()) {
            "Should display 'How follow up tests are suggested'"
        }
        assert(landingPage.faqSection.isHowPrioritiesDecidedVisible()) {
            "Should display 'How priorities are decided'"
        }
    }

    @Test
    fun `should display points tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.faqSection.clickPointsTab()

        assert(landingPage.faqSection.isWhereICanSeeMyPointsVisible()) {
            "Should display 'Where I can see my points'"
        }
        assert(landingPage.faqSection.isWhetherPointsExpireVisible()) {
            "Should display 'Whether points expire'"
        }
        assert(landingPage.faqSection.isHowGiftingPointsWorksVisible()) {
            "Should display 'How gifting points works'"
        }
        assert(landingPage.faqSection.isWhatPointsUsedForVisible()) {
            "Should display 'What DH Points can be used for'"
        }
        assert(landingPage.faqSection.isHowReferralsWorkVisible()) {
            "Should display 'How referrals work'"
        }
    }

}
