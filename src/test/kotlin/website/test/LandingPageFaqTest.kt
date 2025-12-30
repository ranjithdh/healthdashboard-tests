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

        assert(landingPage.isGeneralTabVisible()) { "Should display general tab" }
        assert(landingPage.isAppointmentAndProcessVisible()) { "Should display appointment and process tab" }
        assert(landingPage.isTestCoverageTabVisible()) { "Should display test coverage" }
        assert(landingPage.isPrivacyAndDateSecurityTabVisible()) { "Should display privacy and date" }
        assert(landingPage.isConsultTabVisible()) { "Should display consult" }
        assert(landingPage.isActionPlanTabVisible()) { "Should display action plan" }
        assert(landingPage.isPointsTabVisible()) { "Should display points" }

    }


    @Test
    fun `should display all the general tab questions`() {
        val landingPage = navigateToLandingPage()

        assert(landingPage.isWhoHelpsMeUnderStandMyResultQuestionVisible()) { "Should display who helps me" }
        assert(landingPage.isDoYouTestEverythingOrOnlWhatMattersVisible()) { "Should display do you think" }
        assert(landingPage.isWhyMostHealthTestsFeelConfusingVisible()) { "Should display why most health tests" }
        assert(landingPage.isWhatMakesBaselineDifferentFromOtherBloodTestsVisible()) { "Should display what makes blood" }
        assert(landingPage.isWhatExactlyIsBaselineTestsVisible()) { "Should display what exactly is baseline tests" }
        assert(landingPage.isWhoIsBaselineForVisible()) { "Should display who is baseline for" }
        assert(landingPage.isCanBaselineHelpMeUnderstandWhyIFeelLowEnergyOrUnwellVisible()) { "Should display who helps me" }
        assert(landingPage.isWhoShouldTakeThisTestVisible()) { "Should display who helps me" }
        assert(landingPage.isIveDoneARecentHealthCheckUpShouldIStillGoForBaselineVisible()) { "Should display who helps me" }
        assert(landingPage.isWhenShouldICheckMyBaselineVisible()) { "Should display who helps me" }
    }

    @Test
    fun `should display appointment and process tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.clickProcessAndAppointmentTab()

        assert(landingPage.isWhoToContactForHelpVisible()) {
            "Should display 'Who to contact for help'"
        }

        assert(landingPage.isWhatHappensAfterResultsArriveVisible()) {
            "Should display 'What happens after results arrive'"
        }

        assert(landingPage.isWhyTwoBloodDrawsVisible()) {
            "Should display 'Why are there two blood draws'"
        }
        assert(landingPage.isHowLongDoesBloodDrawTakeVisible()) {
            "Should display 'How long does the blood draw take'"
        }
    }

    @Test
    fun `should display test coverage tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.clickTestCoverageTab()

        assert(landingPage.isHowAddOnsImproveClarityVisible()) {
            "Should display 'How add ons improve clarity'"
        }
        assert(landingPage.isWhetherAddOnsMandatoryVisible()) {
            "Should display 'Whether add ons are mandatory'"
        }
        assert(landingPage.isWhatAddOnTestsAvailableVisible()) {
            "Should display 'What add on tests are available'"
        }
        assert(landingPage.isCanIAddTestsLaterVisible()) {
            "Should display 'Can I add tests later'"
        }

        assert(landingPage.isBaselineMedicallyReliableVisible()) {
            "Should display 'Is Baseline medically reliable'"
        }
    }

    @Test
    fun `should display test privacy and data security tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.clickPrivacyAndDateSecurityTab()

        assert(landingPage.isWhoCanAccessMyDataVisible()) {
            "Should display 'Who can access my data'"
        }
        assert(landingPage.isHowPrivacyProtectedVisible()) {
            "Should display 'How privacy is protected'"
        }
        assert(landingPage.isCanIDeleteMyDataVisible()) {
            "Should display 'Can I delete my data'"
        }
        assert(landingPage.isWhetherMyDataSharedVisible()) {
            "Should display 'Whether my data is shared'"
        }
        assert(landingPage.isHowHealthDataStoredVisible()) {
            "Should display 'How my health data is stored'"
        }
    }

    @Test
    fun `should display consult tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.clickConsultTab()

        assert(landingPage.isConsultationIncludedVisible()) {
            "Should display 'Is consultation included'"
        }
        assert(landingPage.isConsultationDurationVisible()) {
            "Should display 'How long the consultation takes'"
        }
        assert(landingPage.isWhatHappensDuringConsultationVisible()) {
            "Should display 'What happens during the consultation'"
        }
        assert(landingPage.isHowPersonalisedGuidanceVisible()) {
            "Should display 'How personalised the guidance is'"
        }
        assert(landingPage.isWhoConductsConsultationVisible()) {
            "Should display 'Who conducts the consultation'"
        }
    }

    @Test
    fun `should display action plan tab questions`() {
        val landingPage = navigateToLandingPage()
        landingPage.clickActionPlanTab()

        assert(landingPage.isWhenToRetestVisible()) {
            "Should display 'When to retest'"
        }
        assert(landingPage.isWhenAddOnsRecommendedVisible()) {
            "Should display 'When add ons are recommended'"
        }
        assert(landingPage.isHowPlanEvolvesVisible()) {
            "Should display 'How the plan evolves over time'"
        }
        assert(landingPage.isHowFollowUpTestsSuggestedVisible()) {
            "Should display 'How follow up tests are suggested'"
        }
        assert(landingPage.isHowPrioritiesDecidedVisible()) {
            "Should display 'How priorities are decided'"
        }
    }

    @Test
    fun `should display points tab questions`() {
        val landingPage = navigateToLandingPage()

        landingPage.clickPointsTab()

        assert(landingPage.isWhereICanSeeMyPointsVisible()) {
            "Should display 'Where I can see my points'"
        }
        assert(landingPage.isWhetherPointsExpireVisible()) {
            "Should display 'Whether points expire'"
        }
        assert(landingPage.isHowGiftingPointsWorksVisible()) {
            "Should display 'How gifting points works'"
        }
        assert(landingPage.isWhatPointsUsedForVisible()) {
            "Should display 'What DH Points can be used for'"
        }
        assert(landingPage.isHowReferralsWorkVisible()) {
            "Should display 'How referrals work'"
        }
    }

}
