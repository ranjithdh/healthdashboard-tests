package mobileView.baseline

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import onboard.page.LoginPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utils.report.Modules

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_HOME)
class BaselineScoreDetailPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    private lateinit var baselineScoreDetailPage: BaselineScoreDetailPage

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())

        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
        baselineScoreDetailPage = navigateToHomePage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    fun navigateToHomePage(): BaselineScoreDetailPage {
        val tesUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage

        val baselineScoreDetailResponse = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)
            .clickBaseLineScoreCard()

        val baselineScoreDetailPage = BaselineScoreDetailPage(page)
        baselineScoreDetailPage.waitForPageLoad()

        baselineScoreDetailPage.saveBaseLineScoreDetails(baselineScoreDetailResponse)

        return baselineScoreDetailPage
    }

    @Test
    fun `verify baseline score title and last updated time`() {
        assertTrue(baselineScoreDetailPage.isBaselineScoreTitleVisible())
        assertTrue(baselineScoreDetailPage.isBetaTagVisible())
        assertTrue(baselineScoreDetailPage.isLastUpdatedTimeVisible())
    }

    @Test
    fun `verify baseline score and status`() {
        assertTrue(baselineScoreDetailPage.isBaseLineScoreIsMatching())
        assertTrue(baselineScoreDetailPage.isBaseLineScoreStatusMatching())
        assertTrue(baselineScoreDetailPage.isScoreRangeElementsVisible())
        assertTrue(baselineScoreDetailPage.isBaselineScoreDescriptionMatching())
    }


    @Test
    fun `verify what needs attention section`() {
        assertTrue(baselineScoreDetailPage.isWhatNeedsAttentionTitleAndDescriptionVisible())
    }

    @Test
    fun `verify negative contributors details`() {
        baselineScoreDetailPage.clickViewAllNegativeContributors()
        val negativeContributors =
            baselineScoreDetailPage.getBaselineScoreDetails()?.data?.contributors?.negative ?: emptyList()

        negativeContributors.forEach { contributor ->
            assertTrue(
                baselineScoreDetailPage.isContributorDetailsVisible(
                    contributor.display_name ?: "",
                    contributor.current_value.toString(),
                    contributor.unit ?: "",
                    contributor.inference ?: ""
                ),
                "Failed to verify negative contributor: ${contributor.display_name}"
            )
        }

        baselineScoreDetailPage.clickViewLessNegativeContributors()
    }

    @Test
    fun `verify positive contributors details`() {
        baselineScoreDetailPage.clickViewAllPositiveContributors()
        val positiveContributors =
            baselineScoreDetailPage.getBaselineScoreDetails()?.data?.contributors?.positive ?: emptyList()

        positiveContributors.forEach { contributor ->
            assertTrue(
                baselineScoreDetailPage.isContributorDetailsVisible(
                    contributor.display_name ?: "",
                    contributor.current_value.toString(),
                    contributor.unit ?: "",
                    contributor.inference ?: ""
                ),
                "Failed to verify positive contributor: ${contributor.display_name}"
            )
        }

        baselineScoreDetailPage.clickViewLessPositiveContributors()
    }


    @Test
    fun `verify what is baseline score section`(){
        assertTrue(baselineScoreDetailPage.verifyWhatIsBaselineScoreSection())
    }

    @Test
    fun `verify biological age section`(){
        assertTrue(baselineScoreDetailPage.verifyBiologicalAgeSection())
    }

    @Test
    fun `verify disclaimer text visible`(){
        assertTrue {
            baselineScoreDetailPage.isDisclaimerVisible()
        }
    }
}

