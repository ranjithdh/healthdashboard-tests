package website.page

import com.microsoft.playwright.Page
import config.BasePage

abstract class WebSiteBasePage(page: Page) : BasePage(page) {

    override fun navigate(): WebSiteBasePage {
        val fullUrl = pageUrl
        page.navigate(fullUrl)
        return this
    }

}
