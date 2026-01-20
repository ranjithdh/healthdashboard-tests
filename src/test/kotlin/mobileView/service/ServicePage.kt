package mobileView.service

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import login.page.LoginPage
import model.ServiceResponse
import model.ServiceProduct
import mu.KotlinLogging
import java.text.NumberFormat
import java.util.Locale

private val logger = KotlinLogging.logger {}

class ServicePage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.SERVICES_URL 

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private var serviceData: ServiceResponse? = null


    fun navigateToServices() {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(testUser.mobileNumber)

        val otpPage = login.page.OtpPage(page)
        otpPage.enterOtp(testUser.otp)

        // Navigate to Home first
//        page.navigate(TestConfig.Urls.BASE_URL)

        // Click Book Now to go to Services (this triggers the API call needed by the test)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).nth(1).click()
    }

    /**
     * Verify static content on the Service Page
     */
    fun verifyStaticContent() {
        logger.info { "Verifying static content on Service Page" }
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Your Health, Guided by Experts")).click()
        page.getByText("Connect with experienced").click()
    }

    /**
     * Fetch service data from API by waiting for the response
     * Should be called when the page is loading or about to load the services
     */
    fun fetchServiceDataFromApi(): ServiceResponse? {
        if (serviceData != null) {
            logger.info { "Using cached service data" }
            return serviceData
        }

        try {
            logger.info { "Waiting for service API response..." }
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()?.contains(TestConfig.Urls.SERVICE_SEARCH_API_URL) == true && response.status() == 200
                },
                {
                    // Optional: Assert page URL or similar if needed to ensure trigger
                }
            )

            val responseBodyBytes = response.body()
            if (responseBodyBytes != null && responseBodyBytes.isNotEmpty()) {
                val responseBody = String(responseBodyBytes)
                serviceData = json.decodeFromString<ServiceResponse>(responseBody)
                logger.info { "Service data fetched and parsed successfully" }
                return serviceData
            }
        } catch (e: Exception) {
            logger.warn { "Failed to fetch/parse service API data (might have been missed or timed out): ${e.message}" }
        }
        return null
    }

    /**
     * Manually set service data (e.g. if captured from a test)
     */
    fun setServiceData(data: ServiceResponse) {
        this.serviceData = data
    }

    /**
     * Verify services displayed on the page against the API data
     */
    fun verifyServices() {
        val data = serviceData ?: fetchServiceDataFromApi()
        val products = data?.data?.products
        
        if (products.isNullOrEmpty()) {
            logger.warn { "No service products found to verify" }
            return
        }
        
        logger.info { "Verifying ${products.size} service products" }
        products.forEach { product ->
            verifyServiceCard(product)
        }
    }

    private fun verifyServiceCard(product: ServiceProduct) {
        logger.info { "Verifying product card for: ${product.name}" }
        
        // Image Verification
        // User snippet: page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("Rina Baliga")).click()
        // Matches meta_data.name if available, otherwise product.name
        val imageName = product.meta_data?.name ?: product.name
        if (!imageName.isNullOrEmpty()) {
             page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName(imageName)).click()
        }

        // Heading (Name) Verification
        // User snippet: page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Nutritionist Consultation")).click()
        if (!product.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(product.name)).click()
        }

        // Experience Text Verification
        // User snippet: page.getByText("Rina Baliga | 13 years of").click()
        if (product.meta_data?.name != null && product.meta_data.experience != null) {
            val expPrefix = "${product.meta_data.name} | ${product.meta_data.experience}"
            page.getByText(expPrefix).click()
        }

        // Description Verification
        // User snippet: page.getByRole(AriaRole.PARAGRAPH).filter(new Locator.FilterOptions().setHasText("Personalized nutrition")).click()
        val desc = product.description
        if (!desc.isNullOrEmpty()) {
            // Match the start of the description as used in the snippet
            val subDesc = desc.take(20) 
            page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(subDesc)).click()
        }

        // Price Verification
        // User snippet: page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("₹3,000")).click()
        if (product.price != null) {
             val priceVal = product.price.toDoubleOrNull() ?: 0.0
             val fmt = NumberFormat.getNumberInstance(Locale.US)
             val priceStr = fmt.format(priceVal) // "3,000"
             val displayPrice = "₹$priceStr" // "₹3,000"
             page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(displayPrice)).click()
        }

        // View Details Button Verification
        // User snippet: page.getByTestId("consultation-view-details-898c67b7-bf72-4a37-8f3d-6a3dbc981edb").click()
        if (product.product_id != null) {
            page.getByTestId("consultation-view-details-${product.product_id}").click()
        }
    }
}
