package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.AddOnTestPage
import website.page.LandingPage
import website.page.OurWhyPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddOnPageTest : BaseTest() {

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
        val ourWhyPage = AddOnTestPage(page).navigate() as AddOnTestPage
        ourWhyPage.waitForPageLoad()
        assert(ourWhyPage.isPageHeadingVisible()) { "Header should be visible" }
        assert(ourWhyPage.isDescriptionVisible()) { "Description should be visible" }
    }


    @Test
    fun `should display all the add on test`() {
        val landingPage = AddOnTestPage(page).navigate() as AddOnTestPage
        landingPage.waitForPageLoad()

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

    }
}
