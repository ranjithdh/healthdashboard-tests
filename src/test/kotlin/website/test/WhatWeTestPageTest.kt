package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.WhatWeTestPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WhatWeTestPageTest {

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
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isPageHeadingVisible()) { "Page heading should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-page-heading")
    }

}
