package mobileView.service

import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import kotlinx.serialization.json.*
import org.junit.jupiter.api.*
import com.microsoft.playwright.options.AriaRole
import mobileView.service.ServicePage

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceTest : BaseTest() {

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
        val viewport = TestConfig.Viewports.MOBILE_PORTRAIT
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)
//            .setExtraHTTPHeaders(mapOf("Cache-Control" to "no-cache", "Pragma" to "no-cache"))

        context = browser.newContext(contextOptions)
        page = context.newPage()
        
        // TODO: Login or Navigate to Home if required
        // login() 
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    @Test
    fun `verify service page static texts`() {
        val servicePage = ServicePage(page)
        
        // Navigate to Services page using the robust helper
        servicePage.navigateToServices()
        // Verify static content
        servicePage.verifyStaticContent()
    }

    @Test
    fun `verify service cards using API response`() {
        val servicePage = ServicePage(page)
        
        println("Starting test: verify service cards using API response")
        
        println("Capturing API response and navigating to Services page...")
        val response = page.waitForResponse({ 
            it.url().contains(TestConfig.Urls.SERVICE_SEARCH_API_URL) && it.status() == 200
        }) {
             // navigateToServices() includes login and the "Book Now" click which triggers the API
             servicePage.navigateToServices()
        }
        
        println("Response Status: ${response.status()}")
        if (response.status() == 304) {
             throw AssertionError("API returned 304 Not Modified. Playwright cannot read body of 304 responses. Header 'Cache-Control: no-cache' should have prevented this.")
        }
        
        val responseBody = response.text()
        if (responseBody.isNotEmpty()) {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true;  }
            val serviceResponse = json.decodeFromString<model.ServiceResponse>(responseBody)
            servicePage.setServiceData(serviceResponse)
        }
        
        // Verify specific consultant flow (e.g. Nutritionist Consultation)
        val targetProductId = "72055641-39fc-423b-9a57-b07cda66727f"
        servicePage.verifyServices(targetProductId)
        val product = servicePage.getProductById(targetProductId)
        val status = product?.item_purchase_status
        if (!status.equals("paid", ignoreCase = true)) {
            servicePage.verifySymptomReportFeedbackDialog()

            servicePage.dialogValidation()
            servicePage.reportOptionsValidations()
            servicePage.cancelButtonClick()
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Schedule Now")).click()
            servicePage.onReportSymptomsButtonClick()
            servicePage.selectAllSymptoms()
            servicePage.submitSymptoms()
        }
        // Final verification for the feedback/acknowledgement dialog
        println("Test completed successfully.")
    }
}
