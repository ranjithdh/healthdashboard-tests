package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import org.junit.jupiter.api.*
import utils.report.Modules
import website.page.FaqPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_WEBSITE)
class FaqPageTest : BaseTest() {

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

    private fun navigateToFaqPage(): FaqPage {
        val faqPage = FaqPage(page).navigate() as FaqPage
        faqPage.waitForPageLoad()
        return faqPage
    }


    @Test
    fun `should display FAQ page heading`() {
      val faqPage = navigateToFaqPage()

        assert(faqPage.isPageHeadingVisible()) { "FAQ page heading should be visible" }

        faqPage.takeScreenshot("faq-page-heading")
    }


    @Test
    fun `should display faq tabs`() {
        val faqPage = navigateToFaqPage()

        assert(faqPage.faqSection.isGeneralTabVisible()) { "Should display general tab" }
        assert(faqPage.faqSection.isAppointmentAndProcessVisible()) { "Should display appointment and process tab" }
        assert(faqPage.faqSection.isTestCoverageTabVisible()) { "Should display test coverage" }
        assert(faqPage.faqSection.isPrivacyAndDataSecurityTabVisible()) { "Should display privacy and date" }
        assert(faqPage.faqSection.isConsultTabVisible()) { "Should display consult" }
        assert(faqPage.faqSection.isActionPlanTabVisible()) { "Should display action plan" }
        assert(faqPage.faqSection.isPointsTabVisible()) { "Should display points" }
    }

    @Test
    fun `should display all the general tab questions`() {
        val landingPage = navigateToFaqPage()

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

        assert(landingPage.faqSection.isBaselineAReplacementForADoctorVisible()) { "Should display baseline replacement" }
        assert(landingPage.faqSection.isIsBaselineForDiagnosingDisease()){"Should display baseline for diagnosing"}
        assert(landingPage.faqSection.shouldIUseBaselineDuringAcuteIllness()) { "Should display baseline during acute" }
        assert(landingPage.faqSection.areThereAnyHiddenCosts()) { "Should display areThereAnyHiddenCosts" }
        assert(landingPage.faqSection.whatIsNotIncluded()) { "Should display what isNotIncluded" }
        assert(landingPage.faqSection.whatIsIncludedInTheBaselinePrice()){"Should display what isIncludedInTheBaselinePrice" }
        assert(landingPage.faqSection.whichLabsAreUsed()) { "Should display which labs are" }
        assert(landingPage.faqSection.whatNormalOptimalAndOutOfRangeMean()) { "Should display which normal optimal and out of mean" }
        assert(landingPage.faqSection.howResultsAreOrganisedAndExplained()) { "Should display which how results explained" }
        assert(landingPage.faqSection.howBiomarkersAreSelected()){"Should display how biomarkers are selected" }
        assert(landingPage.faqSection.whatKindOfBiomarkersAreIncluded()) { "Should display which kind of biomarkers are included" }
        assert(landingPage.faqSection.whatProblemBaselineIsDesignedToSolve()){"Should display which biomarkers are designed" }
        assert(landingPage.faqSection.whyClarityAndPrioritisationMatterMoreThanMoreTests()){"Should display which which makes baseline" }
        assert(landingPage.faqSection.howBaselineFitsIntoAModernHealthJourney()) { "Should display which baseline fits in a modern health" }
        assert(landingPage.faqSection.howBaselineHelpsYouUnderstandWhereYouStand()) { "Should display which baseline helps you" }
        assert(landingPage.faqSection.isBaselineOneTimeOrOngoing()) { "Should display which baseline one time" }
        assert(landingPage.faqSection.howOftenShouldIGetTested()) { "Should display which baseline one time" }
        assert(landingPage.faqSection.canBaselineAssessMyHeartHealth()) { "Should display which baseline assess my heart" }
        assert(landingPage.faqSection.whyDontRegularHealthCheckupsIncludeTheseBiomarkers()) { "Should display which biomarkers are different" }
        assert(landingPage.faqSection.doYouDoYourOwnTesting()) { "Should display which biomarkers are different" }
        assert(landingPage.faqSection.whatValueDoYouAddWithThirdPartyTesting()) { "Should display which biomarkers are different" }
        assert(landingPage.faqSection.doIHaveToGoToAPhysicalCentre()) { "Should display which biomarkers are different" }
        assert(landingPage.faqSection.howAccurateAreYourResults()) { "Should display which biomarkers are different" }
        assert(landingPage.faqSection.whatDoIDoWithTheData()) { "Should display which biomarkers are different" }
    }


    @Test
    fun `should display appointment and process tab questions`() {
        val landingPage = navigateToFaqPage()

        landingPage.faqSection.clickAppointmentAndProcessTab()

        assert(landingPage.faqSection.isWhoToContactForHelpVisible()) { "Should display 'Who to contact for help'" }
        assert(landingPage.faqSection.isWhatHappensAfterResultsArriveVisible()) { "Should display 'What happens after results arrive'" }
        assert(landingPage.faqSection.isWhyTwoBloodDrawsVisible()) { "Should display 'Why are there two blood draws'" }
        assert(landingPage.faqSection.isHowLongDoesBloodDrawTakeVisible()) { "Should display 'How long does the blood draw take'" }

        assert(landingPage.faqSection.whatToDoBeforeTheTest()) { "Should display 'What do' before the'" }
        assert(landingPage.faqSection.howLongItTakesToGetResults()) { "Should display 'How long it takes to get'" }
        assert(landingPage.faqSection.howToBookBaseline()){"Should display 'How to book baseline'"}
        assert(landingPage.faqSection.whatToDoIfINeedMedicalCare()){"Should display 'What do if information about 'in'"}
        assert(landingPage.faqSection.howSamplesAreHandled()) { "Should display 'How samples are'" }
        assert(landingPage.faqSection.whatIfINeedToReschedule()) { "Should display 'What if information about 'reschedule'"}
        assert(landingPage.faqSection.howHomeSampleCollectionWorks()) { "Should display 'How home sample collection works'"}
        assert(landingPage.faqSection.howHygieneAndSafetyAreEnsured()) { "Should display 'How hygieneAndSafety'"}
        assert(landingPage.faqSection.howLongTheProcessTakes()) { "Should display 'How long the process takes'" }
        assert(landingPage.faqSection.whoCollectsTheSample()) { "Should display 'Who samples are'" }
        assert(landingPage.faqSection.canBaselineDiagnoseMe()){"Should display 'What do if information about 'diagnose'" }
        assert(landingPage.faqSection.doINeedToStopMedicationsBeforeMyTest()) { "Should display do i need to stop medications before mytest" }
        assert(landingPage.faqSection.doINeedToFastOrPrepareBeforeMyTest()) { "Should display do i need to fast or prepare before mytest" }
        assert(landingPage.faqSection.canIGetTestedIfImPregnantOrNursing()) { "Should display can i get tested if im pregnant or nursing" }
    }


    @Test
    fun `should display test coverage tab questions`() {
        val landingPage = navigateToFaqPage()

        landingPage.faqSection.clickTestCoverageTab()

        assert(landingPage.faqSection.isHowAddOnsImproveClarityVisible()) { "Should display 'How add ons improve clarity'" }
        assert(landingPage.faqSection.isWhetherAddOnsMandatoryVisible()) { "Should display 'Whether add ons are mandatory'" }
        assert(landingPage.faqSection.isWhatAddOnTestsAvailableVisible()) { "Should display 'What add on tests are available'" }
        assert(landingPage.faqSection.isCanIAddTestsLaterVisible()) { "Should display 'Can I add tests later'" }
        assert(landingPage.faqSection.isBaselineMedicallyReliableVisible()) { "Should display 'Is Baseline medically reliable'" }


        assert(landingPage.faqSection.howAccurateAreTheTests()) { "Should display 'How accurate are'" }
        assert(landingPage.faqSection.willIUnderstandAllTheResults()) { "Should display 'How understand all results'" }
        assert(landingPage.faqSection.howSoonWillIGetMyResults()) { "Should display 'How soon will get MyResults'" }
        assert(landingPage.faqSection.whatMoreDoesBaselineConsultOfferComparedToMyFamilyDoctor()){"Should display 'What more does baseline'"}
        assert(landingPage.faqSection.canBaselineTellMeAboutMyFertilityHealth()){"Should display 'What more does baseline'"}
        assert(landingPage.faqSection.canDeepHolisticsTellMeIfImMetabolicallyFit()){"Should display 'What more does baseline'"}
        assert(landingPage.faqSection.whatTestsAreIncludedInTheBaseline()){"Should display 'What more included in the baseline'"}
    }

    @Test
    fun `should display consult tab questions`() {
        val landingPage = navigateToFaqPage()

        landingPage.faqSection.clickConsultTab()

        assert(landingPage.faqSection.isConsultationIncludedVisible()) { "Should display 'Is consultation included'" }
        assert(landingPage.faqSection.isConsultationDurationVisible()) { "Should display 'How long the consultation takes'" }
        assert(landingPage.faqSection.isWhatHappensDuringConsultationVisible()) { "Should display 'What happens during the consultation'" }
        assert(landingPage.faqSection.isHowPersonalisedGuidanceVisible()) { "Should display 'How personalised the guidance is'" }
        assert(landingPage.faqSection.isWhoConductsConsultationVisible()) { "Should display 'Who conducts the consultation'" }

        assert(landingPage.faqSection.isHowSymptomsAreConnectedToResultsVisible()) { "Should display 'How symptoms are connected to results'" }
    }

    @Test
    fun `should display action plan tab questions`() {
        val landingPage = navigateToFaqPage()
        landingPage.faqSection.clickActionPlanTab()

        assert(landingPage.faqSection.isWhenToRetestVisible()) { "Should display 'When to retest'" }
        assert(landingPage.faqSection.isWhenAddOnsRecommendedVisible()) { "Should display 'When add ons are recommended'" }
        assert(landingPage.faqSection.isHowPlanEvolvesVisible()) { "Should display 'How the plan evolves over time'" }
        assert(landingPage.faqSection.isHowFollowUpTestsSuggestedVisible()) { "Should display 'How follow up tests are suggested'" }
        assert(landingPage.faqSection.isHowPrioritiesDecidedVisible()) { "Should display 'How priorities are decided'" }


        assert(landingPage.faqSection.whatTheActionPlanIncludes()){"Should display 'What the_action_plan includes'" }
        assert(landingPage.faqSection.canIAskFollowUpQuestions()) { "Should display 'What should ask follow up questions'" }
        assert(landingPage.faqSection.whatIfSomethingLooksConcerning()) { "Should display 'What should ask follow up questions'" }
    }

    @Test
    fun `should display test privacy and data security tab questions`() {
        val landingPage = navigateToFaqPage()

        landingPage.faqSection.clickPrivacyAndDataSecurityTab()

        assert(landingPage.faqSection.isWhoCanAccessMyDataVisible()) { "Should display 'Who can access my data'" }
        assert(landingPage.faqSection.isHowPrivacyProtectedVisible()) { "Should display 'How privacy is protected'" }
        assert(landingPage.faqSection.isCanIDeleteMyDataVisible()) { "Should display 'Can I delete my data'" }
        assert(landingPage.faqSection.isWhetherMyDataSharedVisible()) { "Should display 'Whether my data is shared'" }
        assert(landingPage.faqSection.isHowHealthDataStoredVisible()) { "Should display 'How my health data is stored'" }

        assert(landingPage.faqSection.howDataQualityIsEnsured()) { "Should display 'How data quality is ensured'" }
        assert(landingPage.faqSection.canIUpdateMyPersonalInformation()){"Should display 'What should ask follow up personal information'" }
        assert(landingPage.faqSection.whoCanSeeMyTestResults()) { "Should display 'Who can see my_test results'" }
        assert(landingPage.faqSection.isMyDataSafeWithDeepHolistics()){"Should display 'What should ask follow up personal information'" }
    }

    @Test
    fun `should display points tab questions`() {
        val landingPage = navigateToFaqPage()

        landingPage.faqSection.clickPointsTab()

        assert(landingPage.faqSection.isWhereICanSeeMyPointsVisible()) { "Should display 'Where I can see my points'" }
        assert(landingPage.faqSection.isWhetherPointsExpireVisible()) { "Should display 'Whether points expire'" }
        assert(landingPage.faqSection.isHowGiftingPointsWorksVisible()) { "Should display 'How gifting points works'" }
        assert(landingPage.faqSection.isWhatPointsUsedForVisible()) { "Should display 'What DH Points can be used for'" }
        assert(landingPage.faqSection.isHowReferralsWorkVisible()) { "Should display 'How referrals work'" }

        assert(landingPage.faqSection.isCanIGiftBaselineToMyFamilyOrFriendsVisible()) { "Should display 'Can IGift baseline or friends'" }

    }

}
