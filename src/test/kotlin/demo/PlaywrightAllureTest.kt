package demo

import com.microsoft.playwright.*
import io.qameta.allure.Allure
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaywrightAllureTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var page: Page

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
        page = browser.newPage()
    }

    @AfterAll
    fun teardown() {
        page.close()
        browser.close()
        playwright.close()
    }

    @Test
    fun testLogin() {
        try {
            page.navigate("https://app.stg.deepholistics.com/login?utm_source=direct")
            page.fill("#username", "testuser")
            page.fill("#password", "password123")
            page.click("#loginButton")

            // Assertion
            Assertions.assertTrue(page.locator("text=Welcome").isVisible())

        } catch (e: Exception) {
            attachScreenshot()
            throw e
        }
    }

    private fun attachScreenshot() {
        val buildDir = System.getProperty("buildDir")
        val screenshotPath = "$buildDir/allure-results/screenshot${UUID.randomUUID()}.png"
        page.screenshot(Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)))
        Allure.addAttachment(
            "Screenshot",
            FileInputStream(File(screenshotPath))
        )
    }
}