package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger


class AddOnTestPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.ALL_TEST
    val addOnTestCards = AddOnTestCards(page, AddOnTestPageType.ALL_TESTS)

    private val header = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("G e t t e s t e d f r o m t h e c o m f o r t o f y o u r h o m e .")
    )

    private val bookNowButton = page.locator("#join-all-test-btn-hero")


    fun waitForPageLoad(): AddOnTestPage {
        header.waitFor()
        logger.info { "FAQ page loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescriptionVisible(): Boolean {
        return page.getByText(
            "With the largest range of functional and preventive tests, flexible testing options, clear results you can act on, and a dedicated 1-on-1 guidance call, you move from guessing to decoding."
        ).isVisible
    }

    fun isBookNowButtonVisible() = bookNowButton.isVisible

    fun clickBookNowButton() {
        bookNowButton.click()
    }



}
