package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.HowItWorksPage

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HowItWorksPageTest {

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
    fun `should display page heading and description`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isPageHeadingVisible()) { "Page heading should be visible" }
        assert(howItWorksPage.isFromTestToClarifyDescriptionVisible()) { "Page Description should be visible" }
    }


    @Test
    fun `should display header navigation elements`() {
        val howItWorksPage = HowItWorksPage(page).navigate() as HowItWorksPage
        howItWorksPage.waitForPageLoad()

        assert(howItWorksPage.isLogoVisible()) { "Logo should be visible" }
        assert(howItWorksPage.isHowItWorksLinkVisible()) { "How It Works link should be visible" }
        assert(howItWorksPage.isWhatWeTestLinkVisible()) { "What We Test link should be visible" }
        assert(howItWorksPage.isOurWhyLinkVisible()) { "Our Why link should be visible" }
        assert(howItWorksPage.isFaqLinkVisible()) { "FAQ link should be visible" }
        assert(howItWorksPage.isLoginLinkVisible()) { "Login link should be visible" }
        assert(howItWorksPage.isHeaderBookNowVisible()) { "Header Book Now button should be visible" }

        howItWorksPage.takeScreenshot("how-it-works-header-elements")
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
        assert(howItWorksPage.isStep1Point2DescriptionVisible()){"Step 1 point 2 description should be visible"}


        assert(howItWorksPage.isStep1Point3TitleVisible()) { "Step 1 point 3 title should be visible"}
        assert(howItWorksPage.isStep1Point3DescriptionVisible()) { "Step 1 point 3 description should be visible"}
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
        assert(howItWorksPage.isStep2Point2DescriptionVisible()){"Step 2 point 2 description should be visible"}


        assert(howItWorksPage.isStep2Point3TitleVisible()) { "Step 2 point 3 title should be visible"}
        assert(howItWorksPage.isStep2Point3DescriptionVisible()) { "Step 2 point 3 description should be visible"}
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
        assert(howItWorksPage.isStep3Point2DescriptionVisible()){"Step 3 point 2 description should be visible"}


        assert(howItWorksPage.isStep3Point3TitleVisible()) { "Step 3 point 3 title should be visible"}
        assert(howItWorksPage.isStep3Point3DescriptionVisible()) { "Step 3 point 3 description should be visible"}
    }


}
