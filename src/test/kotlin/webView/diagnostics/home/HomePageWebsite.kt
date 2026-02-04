package webView.diagnostics.home

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import utils.logger.logger


class HomePageWebsite(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.WEBSITE_HOME_PAGE_URL


    fun waitFoWebPageHomePageConfirmation(): HomePageWebsite {
        logger.info("Waiting for mobileView.home page confirmation...")
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Help")).waitFor()
        return this
    }
}