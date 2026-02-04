package mobileView.diagnostics

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import model.LabTestResponse
import mu.KotlinLogging
import onboard.page.LoginPage
import utils.json.json

private val logger = KotlinLogging.logger {}

class LabTestsPage(page: Page) : BasePage(page) {

    override val pageUrl = ""
     var labTestData: LabTestResponse? = null




    fun checkStaticTextsAndSegments() {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Book Lab Tests"))
        page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("With flexible testing options"))
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("All")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Blood")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Gene")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Gut")).click()
    }

    fun login() {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(testUser)
        
        val otpPage = onboard.page.OtpPage(page)
        otpPage.enterOtp(testUser.otp)
    }

    fun goToDiagnosticsUrl() {
          page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).first().click()
//        page.waitForTimeout(2000.0)
//        page.navigate(TestConfig.Urls.DIAGNOSTICS_URL)
    }

    fun navigateToDiagnostics() {
        login()
        goToDiagnosticsUrl()
    }


    init {
        getLabTestsResponse()
    }

    fun getLabTestsResponse() {
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains(TestConfig.Urls.LAB_TEST_API_URL) == true && response.status() == 200
            },
            {
                navigateToDiagnostics()
            }
        )

        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "getLabTestsResponse API response body is empty" }
//            return null
        }

        try {
            val responseObj = json.decodeFromString<LabTestResponse>(responseBody)

            if (responseObj.data != null) {
                labTestData = responseObj
//                return labTestData
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response..${e.message}" }
//            return null
        }

//        return null
    }

    fun clickViewDetails(): TestDetailPage {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View Details")).first().click()
        return TestDetailPage(page)
    }

    fun clickViewDetails(code: String): TestDetailPage {
        val button = page.getByTestId("test-card-view-details-$code")
        button.scrollIntoViewIfNeeded()
        button.click()
        return TestDetailPage(page)
    }

    fun verifyTestCard(code: String) {
        val image = page.getByTestId("test-card-image-$code")
        image.scrollIntoViewIfNeeded()
        if (!image.isVisible) throw AssertionError("Image not visible for code: $code")

        if (!page.getByTestId("test-card-name-$code").isVisible) throw AssertionError("Name not visible for code: $code")
        if (!page.getByTestId("test-card-sample-type-$code").isVisible) throw AssertionError("Sample type not visible for code: $code")
        if (!page.getByTestId("test-card-description-$code").isVisible) throw AssertionError("Description not visible for code: $code")
        if (!page.getByTestId("test-card-price-$code").isVisible) throw AssertionError("Price not visible for code: $code")
        if (!page.getByTestId("test-card-view-details-$code").isVisible) throw AssertionError("View details not visible for code: $code")
    }

    fun verifyTestCard(code: String, name: String, sampleType: String, price: String) {
        val image = page.getByTestId("test-card-image-$code")
        image.scrollIntoViewIfNeeded()
        if (!image.isVisible) throw AssertionError("Image not visible for code: $code")

        val nameElement = page.getByTestId("test-card-name-$code")
        if (!nameElement.isVisible) throw AssertionError("Name not visible for code: $code")
        if (nameElement.innerText() != name) throw AssertionError("Name mismatch for code: $code. Expected: '$name', Found: '${nameElement.innerText()}'")

        val sampleTypeElement = page.getByTestId("test-card-sample-type-$code")
        if (!sampleTypeElement.isVisible) throw AssertionError("Sample type not visible for code: $code")
        if (sampleTypeElement.innerText() != sampleType) throw AssertionError("Sample type mismatch for code: $code. Expected: '$sampleType', Found: '${sampleTypeElement.innerText()}'")

        if (!page.getByTestId("test-card-description-$code").isVisible) throw AssertionError("Description not visible for code: $code")

        val priceElement = page.getByTestId("test-card-price-$code")
        if (!priceElement.isVisible) throw AssertionError("Price not visible for code: $code")
        // Price might contain currency symbol, normalize for comparison
        val actualPrice = priceElement.innerText().replace("₹", "₹ ").replace("  ", " ").trim()
        if (!actualPrice.contains(price)) throw AssertionError("Price mismatch for code: $code. Expected to contain: '$price', Found: '$actualPrice'")

        if (!page.getByTestId("test-card-view-details-$code").isVisible) throw AssertionError("View details not visible for code: $code")
    }

    fun verifyHowItWorksSection(sampleType: String, code: String, reportGenerationHr: String? = null, firstHighlight: String? = null) {
        val type = sampleType.lowercase()
        println("Verifying 'How it Works' section for sample type: $type")

        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it Works?")).waitFor()

        // Logic based on React implementation provided by the user:
        // If not blood, there is a "Kit Delivered" step first.
        val steps = mutableListOf<Map<String, String>>()

        // Step: Kit Delivered (Non-blood only)
        when (type) {
            "saliva" -> steps.add(mapOf("title" to "Get Gene Kit Delivered", "desc" to "Your DNA kit arrives at your doorstep with simple cheek swab instructions."))
            "stool" -> steps.add(mapOf("title" to "Get Gut Kit Delivered", "desc" to "Your gut test kit arrives at your doorstep with easy sample collection instructions."))
            "dried_blood_spot" -> steps.add(mapOf("title" to "Get Omega Test Kit Delivered", "desc" to "Your Omega test kit arrives at your doorstep with an easy DBS tool."))
            "saliva_stress" -> steps.add(mapOf("title" to "Get Cortisol Test Kit Delivered", "desc" to "Your cortisol test kit arrives at your doorstep with an easy saliva collection tube."))
        }

        // Step: Sample Collection
        val collectionTitle = if (type == "saliva" || type == "blood") "At-Home Sample Collection" else "At-Home Self-Test Kit"
        val collectionDesc = when (type) {
            "saliva" -> "Schedule a quick home visit — our technician collects your sample in minutes."
            "stool" -> "Collect your stool sample and schedule a quick pickup from home."
            "blood" -> "Schedule the blood sample collection from the comfort of your home."
            "dried_blood_spot" -> "Do easy DBS test by yourself and schedule a quick pickup from home."
            "saliva_stress" -> "Collect your saliva sample as per the instructions and schedule a quick pickup from home."
            else -> "Schedule the ${firstHighlight ?: "blood sample"} collection from the comfort of your home."
        }
        steps.add(mapOf("title" to collectionTitle, "desc" to collectionDesc))

        // Step: Results
        val resultsTitle = if (type == "blood") "Get results in 72 hrs" else (if (type == "saliva") "Get results in 3–4 weeks" else if (type == "stool") "Get results in 7–10 days" else "Get results in 72 hrs")
        val resultsDesc = when (type) {
            "blood" -> "Your sample is processed at a certified lab, and your report is ready online in ${reportGenerationHr ?: "72 hours"}."
            "saliva" -> "Your sample is analysed in a certified lab, and your report goes live on your dashboard."
            "stool" -> "Your sample is analysed in a certified lab, and results are shared on your dashboard."
            "dried_blood_spot" -> "Your sample is analysed in a certified lab, and results are shared on your dashboard."
            "saliva_stress" -> "Your sample is analysed in a certified lab, and results are shared on your dashboard."
            else -> "Your sample is processed at a certified lab, and your report is ready online in 72 hours."
        }
        steps.add(mapOf("title" to resultsTitle, "desc" to resultsDesc))

        // Step: Consultation
        val consultDesc = when (type) {
            "saliva" -> "Chat with our experts to understand your results and get personalised guidance."
            "stool" -> "Discuss your gut health report with our experts and get personalised guidance."
            "blood" -> "See how your antibody levels connect with your symptoms by talking to our experts."
            "dried_blood_spot" -> "Discuss your Omega panel report with our experts and get personalised guidance."
            "saliva_stress" -> "Discuss your stress and cortisol report with our experts and get personalised guidance."
            else -> "See how your antibody levels connect with your symptoms by talking to our experts."
        }
        steps.add(mapOf("title" to "1-on-1 Expert Consultation", "desc" to consultDesc))

        // Step: Track Progress (Blood only)
        if (type == "blood") {
            steps.add(mapOf("title" to "Track Progress Overtime", "desc" to "Monitor these markers over time to understand changes and treatment response."))
        }

        // Now verify each step in UI
        steps.forEachIndexed { index, step ->
            val stepNum = String.format("%02d", index + 1)
            println("Verifying step $stepNum: ${step["title"]}")
            
            // Verify Number
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(stepNum)).scrollIntoViewIfNeeded()
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(stepNum)).waitFor()
            
            // Verify Title
            page.getByText(step["title"]!!).first().waitFor()
            
            // Verify Description
            val desc = step["desc"]!!
            // Description might be long or slightly truncated in getByText if not exact, 
            // but we'll try exact first or a significant prefix
            page.getByText(desc.take(40)).waitFor()
        }
    }

    fun verifyCertifiedLabsSection() {
        println("Verifying 'Certified Labs, Secure Data' section")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Certified Labs, Secure Data")).scrollIntoViewIfNeeded()
        
        // Lab Step
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("NABL and CAP Certified Laboratories")).waitFor()
        page.getByText("Each partner lab we work with is CAP-accredited and NABL-certified.").waitFor()
        
        // Privacy Step
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Your privacy matters")).waitFor()
        page.getByText("Your health data is always protected with strict privacy safeguards.").waitFor()
    }
    fun clickFilter(name: String) {
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName(name)).click()
    }

    fun isTestCardVisible(code: String): Boolean {
        // Check if the card is visible by looking for the image element which is unique per card
        return page.getByTestId("test-card-image-$code").isVisible
    }
}
