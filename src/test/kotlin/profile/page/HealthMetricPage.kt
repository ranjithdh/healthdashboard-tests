package profile.page

import com.microsoft.playwright.Page
import config.BasePage

class HealthMetricPage(page: Page): BasePage(page) {
    override val pageUrl = "/profile"

}