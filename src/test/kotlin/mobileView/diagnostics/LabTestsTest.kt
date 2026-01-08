package mobileView.diagnostics

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import kotlinx.serialization.json.*
import login.page.LoginPage
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabTestsTest {

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
        val viewport = TestConfig.Viewports.MOBILE_PORTRAIT
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    @Test
    fun `verify lab tests page static texts and segments`() {
        val labTestsPage = LabTestsPage(page)
        labTestsPage.navigateToDiagnostics()
        labTestsPage.checkStaticTextsAndSegments()
    }


    @Test
    fun `verify lab tests cards using API response`() {

        println("Starting test: verify lab tests page static texts and segments")
        val labTestsPage = LabTestsPage(page)

        // Capture the API response during navigation
        println("Navigating to diagnostics page and capturing API response...")
        val response = page.waitForResponse({ it.url().contains("human-token/lab-test") && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }

        println("Waiting for URL: **/diagnostics")
        page.waitForURL("**/diagnostics")

        println("Checking static texts and segments...")

        // Parse response and verify cards
        println("Parsing API response...")
        val json = kotlinx.serialization.json.Json.parseToJsonElement(response.text()).jsonObject
        val data = json["data"]?.jsonObject
        val productList = data?.get("diagnostic_product_list")?.jsonObject

        val codes = mutableListOf<String>()

        // Extract codes from packages
        productList?.get("packages")?.jsonArray?.forEach {
            it.jsonObject["code"]?.jsonPrimitive?.content?.let { code -> codes.add(code) }
        }

        // Extract codes from test_profiles
        productList?.get("test_profiles")?.jsonArray?.forEach {
            it.jsonObject["code"]?.jsonPrimitive?.content?.let { code -> codes.add(code) }
        }

        // Extract codes from tests
        productList?.get("tests")?.jsonArray?.forEach {
            it.jsonObject["code"]?.jsonPrimitive?.content?.let { code -> codes.add(code) }
        }

        println("Found ${codes.size} codes to verify: $codes")

        // Verify each card
        codes.forEach { code ->
            println("Verifying card for code: $code")
            labTestsPage.verifyTestCard(code)
            println("Verified card for code: $code")
        }


    }
    @Test
    fun `verify inside card static section`() {
        val labTestsPage = LabTestsPage(page)
        labTestsPage.navigateToDiagnostics()
        println("Clicking View Details...")
        val testDetailPage = labTestsPage.clickViewDetails()
        
        println("Verifying How It Works section...")
        testDetailPage.verifyHowItWorksSection()
        
        println("Verifying Certified Labs section...")
        testDetailPage.verifyCertifiedLabsSection()
        
        println("Test completed successfully.")
    }
}
