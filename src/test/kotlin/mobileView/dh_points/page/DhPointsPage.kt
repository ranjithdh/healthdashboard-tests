package mobileView.dh_points.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig

class DhPointsPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.BASE_URL // Update this to the actual URL path for dh_points
    
    fun waitForConfirmation(): DhPointsPage {
        // Example check, replace with actual assertion if specific URL exists
        page.waitForURL { it.contains("dh-points") || it.contains("rewards") || it.contains(TestConfig.Urls.BASE_URL) }
        return this
    }
}
