package website.page

import com.microsoft.playwright.Page
import config.BasePage
import utils.report.StepHelper

abstract class WebSiteBasePage(page: Page) : BasePage(page) {

    override fun navigate(): WebSiteBasePage {
        StepHelper.step("Navigate to $pageUrl")
        val fullUrl = pageUrl
        page.navigate(fullUrl)
        return this
    }

}
