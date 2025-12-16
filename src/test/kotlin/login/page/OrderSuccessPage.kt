package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage

/**
 * OrderSuccessPage - Landing page after successful payment
 */
class OrderSuccessPage(page: Page) : BasePage(page) {
    
    override val pageUrl = "/order-confirmed" // Update with actual URL
    override val pageLoadedSelector = byText("Order Placed Successfully").toString() // Update with actual selector

    fun isOrderPlacedSuccessfully(): Boolean {
        // Look for common success indicators
        return byText("Order Placed").isVisible || 
               byText("Payment Successful").isVisible ||
               byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Thank you")).isVisible
    }
}
