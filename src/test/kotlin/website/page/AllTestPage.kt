package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger


class AllTestPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.ALL_TEST

    private val header = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("G e t t e s t e d f r o m t h e c o m f o r t o f y o u r h o m e .")
    )


    fun waitForPageLoad(): AllTestPage {
        header.waitFor()
        logger.info { "FAQ page loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

}
