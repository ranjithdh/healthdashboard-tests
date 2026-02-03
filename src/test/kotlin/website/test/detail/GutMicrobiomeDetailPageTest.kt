package website.test.detail

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import org.junit.jupiter.api.*
import utils.report.Modules
import website.page.detail.GutMicrobiomeDetailPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_WEBSITE)
class GutMicrobiomeDetailPageTest : BaseTest() {

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
        val detail = GutMicrobiomeDetailPage(page).navigate() as GutMicrobiomeDetailPage
        detail.waitForPageLoad()
        assert(detail.isPageHeadingVisible()) { "Header should be visible" }
        assert(detail.isDescription1Visible()) { "Description should be visible" }
        assert(detail.isDescription2Visible()) { "Description should be visible" }
        assert(detail.testingStepsVisible()) { "Test steps should be visible" }
        assert(detail.testAmountVisible()) { "Test amount should be visible" }
    }

    @Test
    fun `should display what is measured section content`() {
        val detail = GutMicrobiomeDetailPage(page).navigate() as GutMicrobiomeDetailPage
        detail.waitForPageLoad()
        assert(detail.isPageHeadingVisible()) { "Header should be visible" }

        assert(detail.isWhatIsMeasuredSectionVisible()) { "Section content should be visible" }

        detail.clickWhatIsMeasuredButton()

        assert(detail.isGutBacteriaDiversityAndBalanceVisible()) { "should display bacteria diversity" }
        assert(detail.isDigestionAndMetabolismVisible()) { "should display digestion and metabolism" }
        assert(detail.isImmunityAndInflammationVisible()) { "should display immunity and inflammation" }
        assert(detail.isDiseaseRiskIndicatorsVisible()){"should display disease and risk indicators" }
    }

    @Test
    fun `should display who should take this test content`() {
        val detail = GutMicrobiomeDetailPage(page).navigate() as GutMicrobiomeDetailPage
        detail.waitForPageLoad()
        assert(detail.isPageHeadingVisible()) { "Header should be visible" }

        assert(detail.isWhoShouldTakeThisTestVisible()) { "who should take this test content" }

        detail.clickWhoShouldTakeThisTest()

        assert(detail.isWhoShouldTakeThisDescriptionVisible()) { "who should take this test description" }
        assert(detail.isDigestiveIssuesVisible()) { "food and dietConcern should be visible" }
        assert(detail.isImmuneAndInflammationConcernsVisible()) { "environment and seasonalAllergens should be visible" }
        assert(detail.isEnergyAndMetabolismVisible()) { "drug and contactSensitivities should be visible" }
        assert(detail.isPreventiveHealthAndLongevityVisible()) { "preventive health should be visible" }

    }

    @Test
    fun `should display what to expect content`() {
        val detail = GutMicrobiomeDetailPage(page).navigate() as GutMicrobiomeDetailPage
        detail.waitForPageLoad()
        assert(detail.isPageHeadingVisible()) { "Header should be visible" }

        assert(detail.isWhatToExpectVisible()) { "who should take this test content" }

        detail.clickWhatToExpect()

        assert(detail.isWhatToExpectDescriptionVisible()) { "what to expect description" }
        assert(detail.isSampleCollectionVisible()) { "sample collection should be visible" }
        assert(detail.isYourResultsAreUpdatedInYourDashboardVisible()) { "Your results should be updated in your dashboard" }
        assert(detail.isCorrelateDataOnYourDashboardVisible()) { "CorrelateData should be visible" }
        assert(detail.isGeta1on1ConsultWithOurLongevityExpertVisible()) { "Get1 consultWithOurLongevityExpertshould be visible" }

    }

    @Test
    fun `should display how it works content`() {
        val detail = GutMicrobiomeDetailPage(page).navigate() as GutMicrobiomeDetailPage
        detail.waitForPageLoad()
        assert(detail.isPageHeadingVisible()) { "Header should be visible" }

        assert(detail.isHowItWorksHeadingVisible()) { "How it works should be visible" }
        assert(detail.isStep1ContentVisible()) { "Step should be visible" }
        assert(detail.isStep2ContentVisible()) { "Step should be visible" }
        assert(detail.isStep3ContentVisible()) { "Step should be visible" }
        assert(detail.isStep4ContentVisible()) { "Step should be visible" }

    }

    @Test
    fun `should display certified lab content`() {

        val detail = GutMicrobiomeDetailPage(page).navigate() as GutMicrobiomeDetailPage
        detail.waitForPageLoad()
        assert(detail.isPageHeadingVisible()) { "Header should be visible" }

        assert(detail.certifiedLabsSection.isHeadingVisible()) { "Header should be visible" }
        assert(detail.certifiedLabsSection.isNABLAndCAPCertifiedLaboratoriesVisible()) { "NABL and CAP certificates should be visible" }
        assert(detail.certifiedLabsSection.isNABLAndCAPCertifiedLaboratoriesDescriptionVisible()) { "NABL and CAP certificates should be visible" }
        assert(detail.certifiedLabsSection.isYourPrivacyMattersVisible()) { "Your privacy matters should be visible" }
        assert(detail.certifiedLabsSection.isYourPrivacyMattersDescriptionVisible()) { "Your privacy matters should not be visible" }

    }


}
