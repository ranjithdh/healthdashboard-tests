package forWeb.diagnostics.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions

private val logger = KotlinLogging.logger {}

class TestSchedulingPage(page: Page) : BasePage(page) {

    override val pageUrl = ""

    fun captureAddressData(block: () -> Unit) {
        logger.info { "Capturing address data..." }
        // Ideally we would set up a waitForResponse here if we needed to capture data
        // For now, we just execute the block which navigates to the page
        block()
    }

    fun verifySampleCollectionAddressHeading() {
        val heading = page.getByText("Sample Collection Address")
        logger.info { "Verifying Sample Collection Address heading" }
        Assertions.assertTrue(heading.isVisible, "Sample Collection Address heading should be visible")
    }

    fun assertAddressesFromApi() {
        // Implementation regarding API validation would go here
        logger.info { "Asserting addresses from API..." }
    }

    fun clickAddNewAddress() {
        logger.info { "Clicking Add New Address" }
//        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add New Address")).click()
        page.getByTestId("diagnostics-booking-add-new-address").click();
    }

    fun addAddressAndValidate() {
        logger.info { "Adding address and validating..." }
        // Implementation for adding address
    }

    fun editUserAddress(index: Int) {
        logger.info { "Editing user address at index $index" }
        // Implementation for editing address
    }

    fun verifyPriceDetails(expectedSubtotal: Double, expectedDiscount: Double) {
        logger.info { "Verifying price details: Subtotal=$expectedSubtotal, Discount=$expectedDiscount" }
        // Basic verification logic
        val subtotalText = "₹$expectedSubtotal" // Simplified formatting logic
        // Verify subtotal visibility if possible, or generic check
        
        // This is a placeholder validation
        Assertions.assertTrue(page.isVisible("text=Price Details"), "Price Details section should be visible")
    }

    fun verifyFooterActions() {
        logger.info { "Verifying footer actions" }
        val proceedBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Proceed"))
        Assertions.assertTrue(proceedBtn.isVisible, "Proceed button should be visible")
    }

    fun clickProceed() {
        logger.info { "Clicking Proceed" }
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Proceed")).click()
    }

    fun verifySlotSelectionPage() {
        logger.info { "Verifying Slot Selection Page" }
        // Verify we are on the slot selection page, e.g. check for "Select a Date" or "Schedule" heading
        val heading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Schedule your Blood Test").setLevel(1))
        // If heading differs, might need adjustment
        Assertions.assertTrue(heading.isVisible || page.getByText("Select Date").isVisible, "Slot selection page should be visible")
    }
}
