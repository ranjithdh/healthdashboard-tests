package website.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.AddOnTestPage
import website.page.OurWhyPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddOnPageTest {

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
        val ourWhyPage = AddOnTestPage(page).navigate() as AddOnTestPage
        ourWhyPage.waitForPageLoad()
        assert(ourWhyPage.isPageHeadingVisible()) { "Header should be visible" }
        assert(ourWhyPage.isDescriptionVisible()) { "Description should be visible" }
    }


}
