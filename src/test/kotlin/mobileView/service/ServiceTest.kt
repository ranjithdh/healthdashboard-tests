package mobileView.service

import com.microsoft.playwright.*
import config.TestConfig
import kotlinx.serialization.json.*
import org.junit.jupiter.api.*
import com.microsoft.playwright.options.AriaRole
import mobileView.service.ServicePage

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceTest {

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
        page.waitForURL(TestConfig.Urls.SERVICES_URL)
        
        // Verify static content
        servicePage.verifyStaticContent()
    }

    @Test
    fun `verify service cards using API response`() {
        val servicePage = ServicePage(page)
        
        println("Starting test: verify service cards using API response")
        
        // Navigate to Home Page first
        servicePage.navigateToServices()
//        page.waitForURL(TestConfig.Urls.SERVICES_URL)
        
        println("Capturing API response and navigating to Services page...")
        val response = page.waitForResponse({ 
            it.url().contains(TestConfig.Urls.SERVICE_SEARCH_API_URL) && it.status() == 200 
        }) {
             // Click "Book Now" -> Leads to Services Page which triggers API
             page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).nth(1).click()
        }
        
//        page.waitForURL(TestConfig.Urls.SERVICES_URL)
        
        val responseBody = response.text()
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
        val serviceResponse = json.decodeFromString<model.ServiceResponse>(responseBody)
        
        servicePage.setServiceData(serviceResponse)
        servicePage.verifyServices()
        
        println("Test completed successfully.")
    }
}
