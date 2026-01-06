package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger


class FaqPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.FAQ

    private val header = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("F r e q u e n t l y A s k e d Q u e s t i o n s")
    )

    val faqSection = FaqSection(page)

    fun waitForPageLoad(): FaqPage {
        header.waitFor()
        logger.info { "FAQ page loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

}
