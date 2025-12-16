package login.page

import com.microsoft.playwright.FrameLocator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * PaymentPage - Handles Razorpay checkout interactions.
 * Razorpay loads in an iframe, so we need to switch context.
 */
class PaymentPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login" // URL might remain same if it's an overlay

    // Razorpay usually loads in an iframe
    private val razorpayFrame: FrameLocator
        get() = page.frameLocator("iframe[src*='razorpay']")

    // ==================== Actions ====================

    fun waitForPaymentOptions(): PaymentPage {
        logger.info { "waitForPaymentOptions()" }
        // Wait for iframe to attach and an element inside to be visible
        razorpayFrame.getByRole(AriaRole.HEADING, FrameLocator.GetByRoleOptions().setName("Payment Options")).first().waitFor()
        // Or common header "Pay with"
        return this
    }

    /**
     * Selects Netbanking -> Test Bank for successful payment simulation in Test Mode
     */
    fun selectNetbanking(): PaymentPage {
        logger.info { "selectNetbanking()" }
        razorpayFrame.getByText("Netbanking").click()
        return this
    }

    fun selectTestBank(): PaymentPage {
        logger.info { "selectTestBank()" }
        // Often usually just a bank logo or list
        // In sandbox, look for a specific bank or 'ICICI' / 'SBI' which acts as test in sandbox if no specific 'Test Bank'
        // But usually in Razorpay Test Mode, there is a distinct flow.
        // Let's assume standard flow: Click Bank -> Pay
        
        // For generic test bank choice (often first one works in sandbox)
        razorpayFrame.getByRole(AriaRole.BUTTON, FrameLocator.GetByRoleOptions().setName("SBI")).first().click()
        return this
    }

    fun clickPayNow(): PaymentPage {
         logger.info { "clickPayNow()" }
         // "Pay ₹10,000" button footer
         razorpayFrame.getByRole(AriaRole.BUTTON, FrameLocator.GetByRoleOptions().setName("Pay")).first().click()
         return this
    }

    fun completePaymentFlow(): OrderSuccessPage {
         logger.info { "completePaymentFlow()" }
         waitForPaymentOptions()
         selectNetbanking()
         selectTestBank()
         clickPayNow()
         
         // Handle the bank window simulation if it opens in new popup or same iframe
         // In Razorpay standard test bank, it usually shows a success/failure screen in the same window/iframe
         
         val bankPage = page.waitForPopup { 
             // Click 'Success' on the test bank simulator page
             // This part is tricky as it's a new window usually
             // If it's a new window, we need to handle popup
         }
         
         if (bankPage != null) {
             bankPage.getByText("Success").click()
         }
         
         // After success, it redirects back to App
         return OrderSuccessPage(page)
    }
    
    // Simpler version if it stays in iframe (Test mode specific)
    fun simulateSuccessfulPayment(): OrderSuccessPage {
         logger.info { "simulateSuccessfulPayment()" }
         waitForPaymentOptions()
         
         // 1. Select Netbanking
         razorpayFrame.getByText("Netbanking").click()
         
         // 2. Select first available bank (e.g., SBI/ICICI)
         razorpayFrame.locator(".bank-item").first().click() // Generic class, might need specific selector
         
         // 3. Click Pay - This opens the popup
         // Use waitForPopup to capture the new window
         val popup = page.waitForPopup {
             razorpayFrame.getByRole(AriaRole.BUTTON, FrameLocator.GetByRoleOptions().setName("Pay")).click()
         }
         
         // 4. In the bank popup, click 'Success'
         popup.waitForLoadState()
         popup.getByText("Success").click()
         
         // 5. Wait for redirect back to main page
         // page.waitForURL("**/order-confirmed") // Optional: strictly wait for URL
         
         return OrderSuccessPage(page)
    }
}
