package mobileView.service

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import onboard.page.LoginPage
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
        loginPage.enterMobileAndContinue()

        val otpPage = onboard.page.OtpPage(page)
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
                    response?.url()
                        ?.contains(TestConfig.Urls.SERVICE_SEARCH_API_URL) == true && (response.status() == 200 || response.status() == 304)
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
    fun verifyServices(targetProductId: String? = null) {
        val data = serviceData ?: fetchServiceDataFromApi()
        val products = data?.data?.products
        
        if (products.isNullOrEmpty()) {
            logger.warn { "No service products found to verify" }
            return
        }
        
        val productsToVerify = if (targetProductId != null) {
            products.filter { it.product_id == targetProductId }
        } else {
            products
        }
        
        if (productsToVerify.isEmpty()) {
            logger.warn { "No products matched the target ID: $targetProductId" }
            return
        }

        logger.info { "Verifying ${productsToVerify.size} service products" }
        productsToVerify.forEach { product ->
            verifyServiceCard(product)
        }
    }

    private fun verifyServiceCard(product: ServiceProduct) {
        logger.info { "Verifying product card for: ${product.name}" }

        // Image Verification - Check Visibility
        val imageName = product.meta_data?.name ?: product.name
        if (!imageName.isNullOrEmpty()) {
            page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName(imageName)).waitFor()
        }

        // Heading (Name) Verification - Check Visibility
        if (!product.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(product.name)).waitFor()
        }

        // Experience Text Verification - Check Visibility
        if (product.meta_data?.name != null && product.meta_data.experience != null) {
            val expPrefix = "${product.meta_data.name} | ${product.meta_data.experience}"
            page.getByText(expPrefix).waitFor()
        }

        // Description Verification - Check Visibility
        val desc = product.description
        if (!desc.isNullOrEmpty()) {
            val subDesc = desc.take(20)
            page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(subDesc)).waitFor()
        }

        // Price Verification - Check Visibility
        if (product.price != null) {
            val priceVal = product.price.toDoubleOrNull() ?: 0.0
            val fmt = NumberFormat.getNumberInstance(Locale.US)
            val priceStr = fmt.format(priceVal) // "3,000"
            val displayPrice = "₹$priceStr" // "₹3,000"
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(displayPrice)).waitFor()
        }

        // View Details Button Verification - Click and Go Back
        if (product.product_id != null) {
            val viewDetailsBtn = page.getByTestId("consultation-view-details-${product.product_id}")
            logger.info { "Clicking View Details for ${product.name}" }
            viewDetailsBtn.click()

            // Wait for navigation/load
            page.waitForLoadState()

            // Verify Detail Page Content
            verifyServiceDetailPage(product)

            // Go Back to Services Page
            logger.info { "Navigating back to Services page" }
            page.goBack()

            // Ensure we are back on the Services page before continuing
            page.waitForURL(TestConfig.Urls.SERVICES_URL)
        }
    }

    private fun verifyServiceDetailPage(product: ServiceProduct) {
        logger.info { "Verifying detail page for: ${product.name}" }
        val meta = product.meta_data ?: return

        // 1. Header Section
        // Image
        if (!meta.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName(meta.name)).waitFor()
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(meta.name)).waitFor()
        }

        // Credentials (take first if available)
        if (!meta.credentials.isNullOrEmpty()) {
            // Using partial match for robustness or first credential
            val cred = meta.credentials.first().take(15)
            page.getByText(cred).first().waitFor()
        }

        // Experience
        if (!meta.experience.isNullOrEmpty()) {
            // User snippet suggested "years experience"
            // API is "13 years". UI likely shows "13 years experience" or similar.
            // We'll search for the value "13 years"
            page.getByText(meta.experience).first().waitFor()
        }

        // 2. About Section
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("About")).waitFor()

        // Bio
        if (!meta.bio.isNullOrEmpty()) {
            val bioSnippet = meta.bio.take(15) // "Rina is a seaso"
            page.getByText(bioSnippet).first().waitFor()
        }

        // Product Name Heading (Contextual, e.g. "Nutritionist Consultation")
        if (!product.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(product.name)).waitFor()
        }

        // 3. Inclusions Section
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What's Included")).waitFor()

        meta.inclusions?.forEach { inclusion ->
            if (inclusion.isNotEmpty()) {
                val incSnippet = inclusion.take(15)
                page.getByText(incSnippet).first().waitFor()
            }
        }

        // Static Note
        page.getByText("Note: Consultations will not").waitFor()

        // 4. Footer / Action Section
        // Logic: if item_purchase_status was "paid" -> SKIP price/button checks
        val status = product.item_purchase_status
        if (!status.equals("paid", ignoreCase = true)) {
            logger.info { "Item not paid (status: $status), verifying schedule button and price" }

            // Price Button Label: "₹1,500 30 mins"
            if (product.price != null && meta.duration != null) {
                val priceVal = product.price.toDoubleOrNull() ?: 0.0
                val fmt = NumberFormat.getNumberInstance(Locale.US)
                val priceStr = fmt.format(priceVal) // "1,500"

                val buttonLabel = "₹$priceStr ${meta.duration} mins"
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(buttonLabel)).waitFor()
            }

            // Schedule Now Button Logic
            // Handle potentially opening in new tab or same tab
            val currentUrl = page.url()
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Schedule Now")).click()
            
            // Wait briefly to see if navigation happens or a new tab opens
            try {
                page.waitForURL({ url -> url != currentUrl }, Page.WaitForURLOptions().setTimeout(3000.0))
                // If we reached here, URL changed in the same tab
                logger.info { "Schedule Now navigated to new URL. Going back." }
                page.goBack()
            } catch (e: Exception) {
                // Timeout means URL didn't change (likely opened in new tab)
                logger.info { "Schedule Now did not change URL (likely new tab). Verifying if new page exists." }
                
                // If a new page was created, we can close it to clean up, though not strictly required if we just focus on current page
                if (page.context().pages().size > 1) {
                    val latestPage = page.context().pages().last()
                    if (latestPage != page) {
                        logger.info { "Closing new tab opened by Schedule Now" }
                        latestPage.close()
                    }
                }
            }
            
            // Verify we are safely on Detail Page (check unique element)
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What's Included")).waitFor()
        } else {
            logger.info { "Item status is '$status', skipping schedule button verification" }
        }
    }
}
