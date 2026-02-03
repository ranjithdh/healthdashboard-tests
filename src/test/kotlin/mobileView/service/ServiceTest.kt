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
        
        // Navigate to Home Page
//        page.navigate(TestConfig.Urls.HOME_PAGE_URL)

        // Click "Book Now" -> Leads to Services Page
        // page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Book Now")).nth(1).click();
//        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).nth(1).click()
        
        // Wait for URL to be services
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
            it.url().contains(TestConfig.APIs.SERVICE_SEARCH_API_URL) && (it.status() == 200 || it.status() == 304)
        }) {
             // navigateToServices() includes login and the "Book Now" click which triggers the API
             servicePage.navigateToServices()
        }
        
        page.waitForURL(TestConfig.Urls.SERVICES_URL)
        
        val responseBody = response.text()
        if (responseBody.isNotEmpty()) {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
            val serviceResponse = json.decodeFromString<model.ServiceResponse>(responseBody)
            servicePage.setServiceData(serviceResponse)
        }
        
        // Verify specific consultant flow (e.g. Nutritionist Consultation)
        val targetProductId =  "898c67b7-bf72-4a37-8f3d-6a3dbc981edb"//"72055641-39fc-423b-9a57-b07cda66727f" //"898c67b7-bf72-4a37-8f3d-6a3dbc981edb"
        servicePage.verifyServices(targetProductId)
        
        println("Test completed successfully.")
    }
}
