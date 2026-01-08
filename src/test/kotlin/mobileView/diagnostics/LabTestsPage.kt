package mobileView.diagnostics

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import forWeb.diagnostics.page.LabTestsPage
import login.page.LoginPage

class LabTestsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/diagnostics"




    fun checkStaticTextsAndSegments() {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Book Lab Tests"))
        page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("With flexible testing options"))
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("All")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Blood")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Gene")).click()
        page.getByRole(AriaRole.SWITCH, Page.GetByRoleOptions().setName("Gut")).click()
    }

    fun navigateToDiagnostics() {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(testUser.mobileNumber)
        
        val otpPage = login.page.OtpPage(page)
        otpPage.enterOtp(testUser.otp)
        
        // Direct navigation to diagnostics
        page.navigate(TestConfig.Urls.DIAGNOSTICS_URL)
    }

    fun clickViewDetails(): TestDetailPage {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View Details")).first().click()
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
        // Price might contain currency symbol, so check if it contains the expected price value
        if (!priceElement.innerText().contains(price)) throw AssertionError("Price mismatch for code: $code. Expected to contain: '$price', Found: '${priceElement.innerText()}'")

        if (!page.getByTestId("test-card-view-details-$code").isVisible) throw AssertionError("View details not visible for code: $code")
    }
}
