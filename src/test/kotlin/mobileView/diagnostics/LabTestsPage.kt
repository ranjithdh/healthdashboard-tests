package mobileView.diagnostics

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import login.page.LoginPage
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_FILTER
import utils.report.StepHelper.NAVIGATE_TO_DIAGNOSTICS
import utils.report.StepHelper.VIEW_TEST_DETAILS

class LabTestsPage(page: Page) : BasePage(page) {

    override val pageUrl = ""




    fun checkStaticTextsAndSegments() {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Book Lab Tests"))
        page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("With flexible testing options"))
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("All")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Blood")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Gene")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Gut")).click()
    }

    fun navigateToDiagnostics() {
        StepHelper.step(NAVIGATE_TO_DIAGNOSTICS)
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(testUser)
        
        val otpPage = login.page.OtpPage(page)
        otpPage.enterOtp(testUser.otp)
        
        // Navigate to Home first
//        page.navigate(TestConfig.Urls.BASE_URL)
        
        // Click Book Now to go to Diagnostics (this triggers the API call needed by the test)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book Now")).first().click()
    }

    fun clickViewDetails(): TestDetailPage {
        StepHelper.step(VIEW_TEST_DETAILS + "first test card")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View Details")).first().click()
        return TestDetailPage(page)
    }

    fun clickViewDetails(code: String): TestDetailPage {
        StepHelper.step(VIEW_TEST_DETAILS + code)
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

    fun verifyHowItWorksSection() {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it Works?")).click()

        // Step 01
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("01")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("At-Home Sample Collection")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("At-Home Sample Collection")).click()
        page.getByText("Schedule the blood sample").click()

        // Step 02
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("02")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Get results in 72 hrs")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Get results in 72 hrs")).click()
        page.getByText("Your sample is processed at a").click()

        // Step 03
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("03")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("-on-1 Expert Consultation")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("-on-1 Expert Consultation")).click()
        page.getByText("See how your antibody levels").click()

        // Step 04
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("04")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Track Progress Overtime")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Track Progress Overtime")).click()
        page.getByText("Monitor these markers over").click()
    }

    fun verifyCertifiedLabsSection() {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Certified Labs, Secure Data")).click()

        // Certified Labs
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("NABL and CAP Certified")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("NABL and CAP Certified")).click()
        page.getByText("Each partner lab we work with").click()

        // Privacy
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Your privacy matters")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Your privacy matters")).click()
        page.getByText("Your health data is always").click()
    }
    fun clickFilter(name: String) {
        StepHelper.step(CLICK_FILTER + name)
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName(name)).click()
    }

    fun isTestCardVisible(code: String): Boolean {
        // Check if the card is visible by looking for the image element which is unique per card
        return page.getByTestId("test-card-image-$code").isVisible
    }
}
