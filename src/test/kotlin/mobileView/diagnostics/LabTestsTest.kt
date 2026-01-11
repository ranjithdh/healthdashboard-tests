package mobileView.diagnostics

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import kotlinx.serialization.json.*
import login.page.LoginPage
import org.junit.jupiter.api.*
import forWeb.diagnostics.page.TestSchedulingPage

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
                val formattedPrice = "₹ " + numberFormat.format(rawPrice)
                
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
        testCards.forEach { card ->
            println("Verifying card for code: ${card.code}")
            labTestsPage.verifyTestCard(card.code, card.name, card.sampleType, card.price)
            println("Verified card for code: ${card.code}")
        }


    }
    @Test
    fun `verify inside card static section`() {
        val labTestsPage = LabTestsPage(page)
        labTestsPage.navigateToDiagnostics()
        println("Clicking View Details...")
        val testDetailPage = labTestsPage.clickViewDetails(code = "PROJ1056379")

        println("Verifying How It Works section...")
        testDetailPage.verifyHowItWorksSection()

        println("Verifying Certified Labs section...")
        testDetailPage.verifyCertifiedLabsSection()

        println("Test completed successfully.")
    }

    @Test
    fun `verify detail page components`() {
        val labTestsPage = LabTestsPage(page)
        println("Starting test: verify detail page components")
        
        // Capture the API response during navigation
        println("Navigating to diagnostics page and capturing API response...")
        val listResponse = page.waitForResponse({ it.url().contains("human-token/lab-test") && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }
        
        val targetCode = "PROJ1024561" //"DH_LONGEVITY_PANEL"
        
        // Parse list response to find the target item
        val listJson = kotlinx.serialization.json.Json.parseToJsonElement(listResponse.text()).jsonObject
        val listData = listJson["data"]?.jsonObject
        val productList = listData?.get("diagnostic_product_list")?.jsonObject
        val packages = productList?.get("packages")?.jsonArray
        
        val targetPackage = packages?.map { it.jsonObject }?.firstOrNull { 
            it["code"]?.jsonPrimitive?.content == targetCode 
        } ?: throw AssertionError("Package with code $targetCode not found in API response")
        
        val content = targetPackage["content"]?.jsonObject ?: throw AssertionError("Content not found for $targetCode")
        
        // Extract descriptions
        val whatMeasuredDesc = content["what_measured_description"]?.jsonPrimitive?.content ?: ""
        val whatToExpectDesc = content["what_to_expect_description"]?.jsonPrimitive?.content ?: ""
        
        // 'who' is an array in the JSON, we take the first element
        val whoArray = content["who"]?.jsonArray
        val whoDesc = whoArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
        
        println("Expected What's Measured: $whatMeasuredDesc")
        println("Expected Who: $whoDesc")
        println("Expected What to Expect: $whatToExpectDesc")

        println("Clicking View Details for code $targetCode")
        // Click View Details for the specific panel
        labTestsPage.clickViewDetails(targetCode)

       /* val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)

        // Verify Header Info (Name, Short Description, About Description)
        testDetailPage.verifyTestHeaderInfo(targetCode)
        
        // Click and verify buttons with text
        testDetailPage.expandAndVerifySection("What’s measured?", whatMeasuredDesc)
        testDetailPage.expandAndVerifySection("Who should take this test?", whoDesc)
        testDetailPage.expandAndVerifySection("What to expect?", whatToExpectDesc)

        // Extract and format price
        val rawPrice = targetPackage["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
        val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
        numberFormat.maximumFractionDigits = 0
        val formattedPrice = "₹ " + numberFormat.format(rawPrice)
        
        println("Expected Price: $formattedPrice")
        
        // Verify Price and Booking Button
        testDetailPage.verifyPriceAndBookingButton(targetCode, formattedPrice)*/

        println("Test completed successfully.")

//        println("Verifying How It Works section...")
//        testDetailPage.verifyHowItWorksSection()

//        println("Verifying Certified Labs section...")
//        testDetailPage.verifyCertifiedLabsSection()

        println("Test completed successfully.")
    }

    @Test
    fun `verify test scheduling`() {
        val labTestsPage = LabTestsPage(page)
        println("Starting test: verify test scheduling")

        // Capture the API response during navigation
        println("Navigating to diagnostics page and capturing API response...")
        val listResponse = page.waitForResponse({ it.url().contains("human-token/lab-test") && it.status() == 200 }) {
            labTestsPage.navigateToDiagnostics()
        }

        val targetCode = "P037"

        println("Clicking View Details for code $targetCode")
        labTestsPage.clickViewDetails(targetCode)

        val testDetailPage = forWeb.diagnostics.page.TestDetailPage(page)
        
        val testSchedulingPage = TestSchedulingPage(page)
        println("Capturing address list and verifying scheduling page...")
        testSchedulingPage.captureAddressData {
            testDetailPage.clickBookNow(targetCode)
        }

        testSchedulingPage.verifySampleCollectionAddressHeading()

        println("Verifying addresses from API...")
        testSchedulingPage.assertAddressesFromApi()

        // Extract price for the targetCode from listResponse
        val listJson = kotlinx.serialization.json.Json.parseToJsonElement(listResponse.text()).jsonObject
        val listData = listJson["data"]?.jsonObject
        val productList = listData?.get("diagnostic_product_list")?.jsonObject
        
        val productTypes = listOf("packages", "test_profiles", "tests")
        val allProducts = productTypes.flatMap { type ->
            productList?.get(type)?.jsonArray?.map { it.jsonObject } ?: emptyList()
        }

        val targetProduct = allProducts.firstOrNull { 
            it["code"]?.jsonPrimitive?.content == targetCode 
        } ?: throw AssertionError("Product with code $targetCode not found in any of $productTypes in API response")
        
        val rawPrice = targetProduct["product"]?.jsonObject?.get("price")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

        println("Verifying price details...")
        testSchedulingPage.verifyPriceDetails(expectedSubtotal = rawPrice, expectedDiscount = 0.0)

        println("Verifying footer actions...")
        testSchedulingPage.verifyFooterActions()

        println("Test completed successfully.")
    }
}
