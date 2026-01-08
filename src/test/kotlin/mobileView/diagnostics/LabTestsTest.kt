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

        // Map API sample types to displayed text
        val sampleTypeMap = mapOf(
            "blood" to "Blood test",
            "saliva" to "Cheek swab test",
            "stool" to "Stool test",
            "dried_blood_spot" to "At-Home Test Kit",
            "saliva_stress" to "At-Home Test Kit"
        )

        data class TestCardData(val code: String, val name: String, val sampleType: String, val price: String)
        val testCards = mutableListOf<TestCardData>()
        
        fun extractData(jsonArray: JsonArray?) {
            jsonArray?.forEach { element ->
                val obj = element.jsonObject
                val code = obj["code"]?.jsonPrimitive?.content ?: return@forEach
                val name = obj["name"]?.jsonPrimitive?.content ?: ""
                val rawSampleType = obj["sample_type"]?.jsonPrimitive?.content ?: ""
                val sampleType = sampleTypeMap[rawSampleType] ?: rawSampleType
                
                // Price extraction and formatting
                val rawPrice = obj["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                numberFormat.maximumFractionDigits = 0
                val formattedPrice = "₹" + numberFormat.format(rawPrice)
                
                testCards.add(TestCardData(code, name, sampleType, formattedPrice))
            }
        }
        
        // Extract data from packages
        extractData(productList?.get("packages")?.jsonArray)
        
        // Extract data from test_profiles
        extractData(productList?.get("test_profiles")?.jsonArray)
        
        // Extract data from tests
        extractData(productList?.get("tests")?.jsonArray)

        println("Found ${testCards.size} cards to verify: ${testCards.map { it.code }}")

        // Verify each card
        testCards.forEach { data ->
            println("Verifying card for code: ${data.code}")
            labTestsPage.verifyTestCard(data.code, data.name, data.sampleType, data.price)
            println("Verified card for code: ${data.code}")
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
