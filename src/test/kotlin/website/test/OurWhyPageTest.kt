package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.OurWhyPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OurWhyPageTest {

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
    fun `should display page heading`() {
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()
        assert(ourWhyPage.isHederVisible()) { "Header should be visible" }
    }

    @Test
    fun `should display all the contents`() {
        val ourWhyPage = OurWhyPage(page).navigate() as OurWhyPage
        ourWhyPage.waitForPageLoad()

        ourWhyPage.waitForMostPeopleDontWantSixPackAbsText()

        assert(ourWhyPage.isMostPeopleDontWantSixPackAbsTextVisible()) { "Should display most people don't want six pack abs" }
        assert(ourWhyPage.isWeLookAroundHeadingVisible()) { "Should display we-look heading" }
        assert(ourWhyPage.isNoOneWasHelpingYouTextVisible()) { "Should display no one helping you" }
        assert(ourWhyPage.isWeBuildSomethingHeadingVisible()) { "Should display we-build something" }
        assert(ourWhyPage.isCoverImageVisible()) { "Should display cover image" }
        assert(ourWhyPage.isSystemThatConnectHeadingVisible()) { "Should display system that connect" }
        assert(ourWhyPage.isWeBuiltDeepHolisticsHeadingVisible()) { "Should display we-built-holistics" }
        assert(ourWhyPage.isToHelpPeopleLookBetterSectionVisible()) { "Should display to-help people look better" }
        assert(ourWhyPage.isThisIsNotAWellnessHeadingVisible()) { "Should display this heading" }
        assert(ourWhyPage.forEverySingleDayTextVisible()) { "Should display one day" }
        assert(ourWhyPage.weDidNotStartThisHeadingVisible()) { "Should display one heading" }
        assert(ourWhyPage.isCeoNameVisible()) { "Should display ceo name" }
        assert(ourWhyPage.isWeAreNoteHereHeadingVisible()) { "Should display heading" }
        assert(ourWhyPage.whenPeopleAroundAsTextVisible()) { "Should display people around" }
    }

}
