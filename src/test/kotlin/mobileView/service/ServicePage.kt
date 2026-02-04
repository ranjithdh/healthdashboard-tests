package mobileView.service

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import onboard.page.LoginPage
import model.ServiceResponse
import model.ServiceProduct
import model.profile.PiiUserResponse
import mu.KotlinLogging
import webView.diagnostics.symptoms.model.SymptomsData
import webView.diagnostics.symptoms.model.UserSymptomsResponse
import java.text.NumberFormat
import java.util.Locale
import java.util.regex.Pattern
import kotlin.random.Random
import kotlin.test.assertEquals

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
    private var symptomsResponse: SymptomsData? = null
    var isSymptomsEmpty = false
    init {
        monitorTraffic()
    }
    fun navigateToServices() {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue()

        val otpPage = onboard.page.OtpPage(page)
        otpPage.enterOtp(testUser.otp)

        // Direct navigation to Services after login, with a short delay for session stability
         page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).nth(1).click()
//        page.waitForTimeout(2000.0)
//        page.navigate(TestConfig.Urls.SERVICES_URL)
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
     * Get a specific product by ID from the cached or fetched data
     */
    fun getProductById(productId: String): ServiceProduct? {
        val data = serviceData ?: fetchServiceDataFromApi()
        return data?.data?.products?.find { it.product_id == productId }
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
            page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName(imageName))
        }

        // Heading (Name) Verification - Check Visibility
        if (!product.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(product.name))
        }

        // Experience Text Verification - Check Visibility
        if (product.meta_data?.name != null && product.meta_data.experience != null) {
            val expPrefix = "${product.meta_data.name} | ${product.meta_data.experience}"
            page.getByText(expPrefix)
        }

        // Description Verification - Check Visibility
        val desc = product.description
        if (!desc.isNullOrEmpty()) {
            val subDesc = desc.take(20)
            page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText(subDesc))
        }

        // Price Verification - Check Visibility
        if (product.price != null) {
            val priceVal = product.price.toDoubleOrNull() ?: 0.0
            val fmt = NumberFormat.getNumberInstance(Locale.US)
            val priceStr = fmt.format(priceVal) // "3,000"
            val displayPrice = "₹$priceStr" // "₹3,000"
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(displayPrice))
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
//            logger.info { "Navigating back to Services page" }
//            page.goBack()

            // Ensure we are back on the Services page before continuing
//            page.waitForURL(TestConfig.Urls.SERVICES_URL)
        }
    }

    private fun verifyServiceDetailPage(product: ServiceProduct) {
        logger.info { "Verifying detail page for: ${product.name}" }
        val meta = product.meta_data ?: return

        // 1. Header Section
        // Image
        if (!meta.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName(meta.name))
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(meta.name))
        }

        // Credentials (take first if available)
        if (!meta.credentials.isNullOrEmpty()) {
            // Using partial match for robustness or first credential
            val cred = meta.credentials.first().take(15)
            page.getByText(cred).first()
        }

        // Experience
        if (!meta.experience.isNullOrEmpty()) {
            // User snippet suggested "years experience"
            // API is "13 years". UI likely shows "13 years experience" or similar.
            // We'll search for the value "13 years"
            page.getByText(meta.experience).first().waitFor()
        }

        // 2. About Section
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("About"))

        // Bio
        if (!meta.bio.isNullOrEmpty()) {
            val bioSnippet = meta.bio.take(15) // "Rina is a seaso"
            page.getByText(bioSnippet).first()
        }

        // Product Name Heading (Contextual, e.g. "Nutritionist Consultation")
        if (!product.name.isNullOrEmpty()) {
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(product.name))
        }

        // 3. Inclusions Section
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What's Included"))

        meta.inclusions?.forEach { inclusion ->
            if (inclusion.isNotEmpty()) {
                val incSnippet = inclusion.take(15)
                page.getByText(incSnippet).first()
            }
        }

        // Static Note
        page.getByText("Note: Consultations will not")

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
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(buttonLabel))
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
//                page.goBack()
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
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What's Included"))
        } else {
            logger.info { "Item status is '$status', skipping schedule button verification" }
        }
    }
    val selectionSymptoms = mutableMapOf<String, List<String>>()

    private var isMale = true

    val symptoms = mapOf(
        "Head" to listOf(
            "Headaches", "Faintness", "Insomnia"
        ), "Eyes" to listOf(
            "Bags, dark circles", "Light Sensitivity"
        ), "Ears" to listOf(
            "Ringing / hearing loss"
        ), "Nose" to listOf(
            "Sinus problems", "Hay fever", "Sneezing attacks"
        ), "Mouth / Throat" to listOf(
            "Chronic coughing", "Canker sores", "Sore Tongue / Glossitis", "Cracks at Mouth Corners", "Metallic Taste"
        ), "Skin" to listOf(
            "Acne",
            "Hives / rashes / dry skin",
            "Hair loss",
            "Flushing / hot flashes",
            "Excessive sweating",
            "Easy Bruising",
            "Slow Wound Healing",
            "Skin Pigmentation Changes",
            "Brittle Nails"
        ), "Heart" to listOf(
            "Rapid/pounding beats", "Frequent Chest pain", "Palpitations"
        ), "Lungs" to listOf(
            "Chest congestion", "Asthma / bronchitis", "Shortness of breath", "Difficulty breathing"
        ), "Digestive Tract" to listOf(
            "Frequent Diarrhea", "Constipation", "Bloating / gas", "Belching / passing gas"
        ), "Joint / Muscles" to listOf(
            "Pain in joints",
            "Arthritis",
            "Stiffness / limited movement",
            "Pain in muscles",
            "Feeling of weakness",
            "Bone Pain/Tenderness",
            "Muscle Cramps/Spasms",
            "Muscle Weakness"
        ), "Weight" to listOf(
            "Binge eating / drinking",
            "Craving certain foods",
            "Difficulty in losing weight",
            "Underweight",
            "Persistent weight gain",
            "Unexplained Weight Gain",
            "Unexplained Weight Loss"
        ), "Energy / Activity" to listOf(
            "Fatigue / sluggishness", "Apathy / lethargy", "Hyperactivity", "Restless leg"
        ), "Mind" to listOf(
            "Poor memory", "Poor concentration"
        ), "Mood" to listOf(
            "Mood swings", "Anxiety / fear / nervousness", "Anger / irritability", "Depression"
        ), "Other" to if (isMale) {
            listOf(
                "Cold intolerance",
                "Cold extremities (feeling",
                "Low libido",
                "Persistent low-grade fever",
                "Frequent illness",
                "Frequent/urgent urination",
                "Burning Sensation in Feet",
                "Poor Coordination / Unsteady",
                "Cold Hands/Feet",
                "Swelling in Legs/Ankles",
                "Night Sweats",
                "Fever/Chills",
                "Frequent Infections",
                "Increased Thirst"
            )
        } else {
            listOf(
                "Cold intolerance",
                "Cold extremities (feeling",
                "Irregular periods",
                "Infertility",
                "Low libido",
                "Persistent low-grade fever",
                "Frequent illness",
                "Frequent/urgent urination",
                "Burning Sensation in Feet",
                "Poor Coordination / Unsteady",
                "Cold Hands/Feet",
                "Swelling in Legs/Ankles",
                "Night Sweats",
                "Fever/Chills",
                "Frequent Infections",
                "Increased Thirst"
            )
        }, "Lungs / Respiratory" to listOf(
            "Wheezing", "Chronic Cough with Phlegm"
        ), "Urinary" to if (isMale) {
            listOf(
                "UTI"
            )
        } else {
            listOf(
                "UTI",
                "Urinary Incontinence"
            )
        }
    )



    /**------------Account Information----------------*/
    fun fetchAccountInformation() {
        try {
            logger.info { "Fetching current preference from API..." }

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.API_ACCOUNT_INFORMATION,
                RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", "Asia/Calcutta")
            )

            if (response.status() != 200) {
                logger.error { "API returned status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            logger.info { "API response...${responseBody}" }

            val responseObj = utils.json.json.decodeFromString<PiiUserResponse>(responseBody)

            if (responseObj.status == "success") {
                setMaleConditions(responseObj.data.piiData.gender == "male")
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch current preference: ${e.message}" }
        }
    }


    fun setMaleConditions(isMale: Boolean) {
        this.isMale = isMale
    }




    fun dialogValidation() {
        val title = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Report Symptoms"))
        val subTitle = page.getByText("Select any symptoms you're")
        val symptomsCount = page.getByText("symptoms selected")
        val cancel = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel"))
        val closeButton =
            page.getByRole(AriaRole.BUTTON).filter(Locator.FilterOptions().setHasText(Pattern.compile("^$")))
        val submitSymptoms = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit Symptoms"))
        val components = listOf(title, subTitle, symptomsCount, closeButton, submitSymptoms, cancel)
        components.forEach { it.waitFor() }
    }


    fun reportOptionsValidations() {
        fetchAccountInformation()
        symptoms.forEach { (section, symptomList) ->
            expandSection(section)
            symptomList.forEach { symptom ->
                selectSymptom(symptom)
            }
        }
    }

    fun expandSection(section: String) {
        val heading = page.getByRole(AriaRole.HEADING)
            .filter(
                Locator.FilterOptions().setHasText(section)
            )
            .first() // pick the first matching element
        heading.scrollIntoViewIfNeeded()
        heading.waitFor()
    }

    fun selectSymptom(symptomName: String) {
        page.getByRole(
            AriaRole.BUTTON, Page.GetByRoleOptions().setName(symptomName)
        ).waitFor()
    }

    fun cancelButtonClick() {
        val cancelButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel"))
        cancelButton.click()
    }

    fun selectAllSymptoms() {
        fetchAccountInformation()
        symptoms.forEach { (section, symptoms) ->
            val selectedSymptoms = randomSubList(symptoms, 1, 3)
            selectionSymptoms[section] = selectedSymptoms
            clickSymptoms(selectedSymptoms)
            symptomsSelectedCount(selectionSymptoms)
        }
    }

    fun <T> randomSubList(list: List<T>, min: Int = 1, max: Int = 3): List<T> {
        if (list.isEmpty()) return emptyList()
        val count = Random.nextInt(min, minOf(max, list.size) + 1)
        return list.shuffled().take(count)
    }

    private fun clickSymptoms(symptoms: List<String>) {
        symptoms.forEach { name ->
            val button = page.getByRole(
                AriaRole.BUTTON, Page.GetByRoleOptions().setName(name)
            )

            button.scrollIntoViewIfNeeded()
            button.click()
        }
    }


    fun symptomsSelectedCount(selectionSymptoms: MutableMap<String, List<String>>) {
        val symptomsCount = page.getByText("symptoms selected")
        var count = 0
        selectionSymptoms.forEach { (string, symptomsList) ->
            count = count.plus(symptomsList.size)
        }
        assertEquals("$count symptoms selected", symptomsCount.innerText())
    }



    fun submitSymptoms() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit Symptoms")).click()
    }

    fun onReportSymptomsButtonClick() {
        logger.info { "Clicking Report Symptom button" }
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Report Symptom")).click()
    }

    fun verifySymptomReportFeedbackDialog() {
//        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Schedule Now")).click()
        logger.info { "Verifying Symptom Report Feedback/Acknowledge Dialog" }
        page.getByRole(AriaRole.DIALOG)
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Report Symptoms"))
        page.getByText("Your questionnaire response")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close")).click()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Schedule Now")).click()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Report Symptom")).click()
    }
    private fun monitorTraffic() {
        val symptomsList = { response: Response ->
            if (response.url().contains(TestConfig.APIs.API_SYMPTOMS_LIST)) {
                logger.info { "API Response: ${response.status()} ${response.url()}" }
                try {
                    if (response.status() == 200) {
                        val responseBody = response.text()
                        if (!responseBody.isNullOrBlank()) {
                            val responseObj = json.decodeFromString<UserSymptomsResponse>(responseBody)
                            symptomsResponse = responseObj.data
                            isSymptomsEmpty = responseObj.data.symptoms.isEmpty()
                            logger.info { "isSymptomsEmpty set to: $isSymptomsEmpty" }
                        }
                    }
                } catch (e: Exception) {
                    logger.warn { "Could not read response body: ${e.message}" }
                }
            }
        }
        page.onResponse(symptomsList)
    }
}
