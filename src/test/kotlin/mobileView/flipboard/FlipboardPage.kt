package mobileView.flipboard

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.home.FlipBoardResponse
import model.flipboard.FlipBoardArticles
import utils.logger.logger


class FlipboardPage(page: Page) : BasePage(page) {

    override val pageUrl: String = "https://human-token-visualizer-gowthaman-stg--developers48.replit.app/insights"

    private var flipBoardResponse: FlipBoardResponse? = null
    private val tagTab = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Tags"))
    private var topics: FlipBoardArticles? = null

    fun setResponse(response: FlipBoardResponse?) {
        this.flipBoardResponse = response
    }

    fun getResponse() = flipBoardResponse
    fun getTopics() = topics

    fun waitForPageLoad(): FlipboardPage {
        page.waitForURL("**/flipboard")
        return this
    }

    fun isForYouTabVisible(): Boolean {
        return page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("For You")).isVisible
    }

    fun isArticleHeadingVisible(title: String): Boolean {
        val locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(title))
        locator.waitFor()
        return locator.isVisible
    }

    fun isArticleTagVisible(tag: String): Boolean {
        val locator = page.getByText(tag).first()
        locator.waitFor()
        return locator.isVisible
    }

    fun clickArticle(title: String) {
        val locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(title))
        locator.waitFor()
        locator.click()
    }

    fun isArticleDetailHeadingVisible(title: String): Boolean {
        val locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(title))
        locator.waitFor()
        return locator.isVisible
    }

    fun isArticleDetailContentVisible(text: String, exact: Boolean = false): Boolean {
        val dialogLocator = page.getByRole(AriaRole.DIALOG).getByText(text, Locator.GetByTextOptions().setExact(exact))
        val paragraphLocator = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText(text))

        val locator = dialogLocator.or(paragraphLocator)

        locator.first().waitFor()
        return locator.first().isVisible
    }

    fun isArticleDetailLinkVisible(linkText: String): Boolean {
        val locator = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName(linkText).setExact(true))
        locator.waitFor()
        return locator.isVisible
    }

    fun isSourcesHeadingVisible(): Boolean {
        val locator = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Sources"))
        locator.waitFor()
        return locator.isVisible
    }

    fun isSourceLinkVisible(linkText: String): Boolean {
        val locator = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName(linkText))
        locator.waitFor()
        return locator.isVisible
    }

    fun isUnreadCountVisible(count: Int): Boolean {
        val locator = page.getByText("${count}Unread")
        return locator.isVisible
    }

    fun closeArticleDetail() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close")).click()
    }

    fun isTagTabVisible() = tagTab.isVisible

    fun clickTagTab(){
        tagTab.click()
    }

    fun isTagMenuItemVisible(tag: String): Boolean {
        return page.getByRole(AriaRole.MENUITEM, Page.GetByRoleOptions().setName(tag).setExact(true)).isVisible
    }

    fun clickTagMenuItem(tag: String) {
        page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains(TestConfig.APIs.FLIP_BOARD_TOPICS) == true && response.status() == 200
            },
            {
                page.getByRole(AriaRole.MENUITEM, Page.GetByRoleOptions().setName(tag)).click()
            }
        ).let { response ->
            val topicResponseBody = response.text()
            if (topicResponseBody.isNullOrBlank()) {
                logger.info { "UnreadCount API response body is empty" }
            }
            try {
               topics= utils.json.json.decodeFromString<FlipBoardArticles>(topicResponseBody)
            } catch (e: Exception) {
                logger.error { "Failed to parse unreadCount API response..${e.message}" }
            }
        }
    }

    fun isNoFlipBoardEmptyStateVisible(tag: String): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("No flipboards found")).isVisible
                && page.getByText("There are no flipboards available for the tag \"$tag\" at the moment.").isVisible

    }

}