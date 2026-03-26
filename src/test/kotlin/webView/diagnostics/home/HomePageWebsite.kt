package webView.diagnostics.home

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper


class HomePageWebsite(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.WEBSITE_HOME_PAGE_URL


    fun waitFoWebPageHomePageConfirmation(): HomePageWebsite {
        StepHelper.step("${StepHelper.HOME_PAGE_WAIT_START} | URL: ${page.url()}")
        logger.info("[HomePageWebsite] Waiting for home page confirmation. Current URL: ${page.url()}")
        val helpLink = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Help"))
        val isVisible = helpLink.isVisible
        StepHelper.step("'Help' link already visible: $isVisible")
        logger.info("[HomePageWebsite] 'Help' link visible before wait: $isVisible")
        helpLink.waitFor()
        StepHelper.step("${StepHelper.HOME_PAGE_WAIT_DONE} | URL: ${page.url()}")
        logger.info("[HomePageWebsite] Home page confirmed. URL: ${page.url()}")
        return this
    }
}