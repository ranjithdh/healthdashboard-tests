package mobileView.flipboard

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import mobileView.baseline.BaselineScoreDetailPage
import onboard.page.LoginPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import utils.report.Modules
import kotlin.collections.emptyList

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_HOME)
@Tag("mobile")
class FlipboardPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    private lateinit var flipboardPage: FlipboardPage

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())

        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
        flipboardPage = navigateToHomePage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    fun navigateToHomePage(): FlipboardPage {
        val tesUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage

        val response = loginPage
            .enterMobileAndContinue(tesUser)
            .enterOtpAndContinueToMobileHomePage(tesUser)
            .clickFlipBoardMenu()

        val flipboardPage = FlipboardPage(page)
        flipboardPage.setResponse(response)
        flipboardPage.waitForPageLoad()

        return flipboardPage
    }


    @Test
    @Order(1)
    fun `verify articles list headings and tags`() {
        assertTrue(flipboardPage.isForYouTabVisible())
        val articles = flipboardPage.getResponse()?.articles?.flipboards ?: emptyList()

        articles.forEach { article ->
            val title = article.title ?: ""
            val tag = article.tag ?: ""

            if (title.isNotEmpty()) {
                assertTrue(
                    flipboardPage.isArticleHeadingVisible(title),
                    "Article heading not visible: $title"
                )
            }
            if (tag.isNotEmpty()) {
                assertTrue(
                    flipboardPage.isArticleTagVisible(tag),
                    "Article tag not visible: $tag for article: $title"
                )
            }
        }
    }

    @Test
    @Order(2)
    fun `verify article detail view`() {
        val article = flipboardPage.getResponse()?.articles?.flipboards?.firstOrNull {
            it.title?.isNotEmpty() == true && it.content?.isNotEmpty() == true
        }

        assertTrue(article != null, "No article with content found in API response")

        val title = article?.title!!
        val content = article.content!!

        flipboardPage.clickArticle(title)

        assertTrue(
            flipboardPage.isArticleDetailHeadingVisible(title),
            "Article detail heading not visible: $title"
        )

        val contentLines = content.split("\n").filter { it.trim().isNotEmpty() }
        val allowedCitations = article.citations?.mapNotNull { it.tag } ?: emptyList()

        contentLines.forEach { line ->
            val currentLine = line.trim()

            // If the line contains any allowed citations, split and verify parts
            val citationsInLine = allowedCitations.filter { currentLine.contains("($it)") }

            if (citationsInLine.isEmpty()) {
                assertTrue(
                    flipboardPage.isArticleDetailContentVisible(currentLine),
                    "Article detail content line not visible: $currentLine"
                )
            } else {
                // Split the line by all citations it contains
                // We escape the parentheses for the regex
                val splitRegex = citationsInLine.joinToString("|") { "\\($it\\)" }.toRegex()
                val segments = currentLine.split(splitRegex)
                    .filter { it.trim().length > 3 } // Filter out very short segments like "." or " ("

                segments.forEach { segment ->
                    assertTrue(
                        flipboardPage.isArticleDetailContentVisible(segment.trim(), exact = false),
                        "Article detail content segment not visible: ${segment.trim()}"
                    )
                }
            }
        }

        allowedCitations.forEach { tag ->
            assertTrue(
                flipboardPage.isArticleDetailLinkVisible(tag),
                "Article detail link not visible for citation: $tag"
            )
        }


        assertTrue(flipboardPage.isSourcesHeadingVisible(), "Sources heading not visible")

        val sources = article.sources ?: emptyList()
        sources.forEach { source ->
            val domain = source.domain ?: ""
            val url = source.url ?: ""
            val name = "$domain $url"

            assertTrue(
                flipboardPage.isSourceLinkVisible(name),
                "Source link not visible: $name"
            )
        }

        flipboardPage.closeArticleDetail()

    }

    @Order(3)
    @Test
    fun `verify the unread count`() {
        assertTrue(
            flipboardPage.isUnreadCountVisible(flipboardPage.getResponse()?.unreadCount?.unread_count ?: 0),
            "Count not visible"
        )

        val filterUnreadArticle = flipboardPage.getResponse()?.articles?.flipboards?.firstOrNull {
            it.is_shown == false
        }

        if (filterUnreadArticle != null) {
            val title = filterUnreadArticle.title!!
            flipboardPage.clickArticle(title)
            flipboardPage.closeArticleDetail()

            val count = flipboardPage.getResponse()?.unreadCount?.unread_count ?: 0

            assertTrue(
                flipboardPage.isUnreadCountVisible(count - 1),
                "Unread count is not decreased after reading an article"
            )
        }
    }


    @Test
    @Order(4)
    fun `verify tag menu list and related articles`(){
        assertTrue(
            flipboardPage.isTagTabVisible(),
            "Tag tab not visible"
        )

        flipboardPage.clickTagTab()

        val tagList = flipboardPage.getResponse()?.tags?.tags ?: emptyList()
        tagList.forEach { name ->
            assertTrue(
                flipboardPage.isTagMenuItemVisible(name),
                "Tag $name is not visible"
            )
        }


        flipboardPage.clickTagMenuItem(tagList.first())

        if (flipboardPage.getTopics()?.flipboards?.isNotEmpty() == true){
            val articles = flipboardPage.getTopics()?.flipboards ?: emptyList()

            articles.forEach { article ->
                val title = article.title ?: ""
                val tag = article.tag ?: ""

                if (title.isNotEmpty()) {
                    assertTrue(
                        flipboardPage.isArticleHeadingVisible(title),
                        "Article heading not visible: $title"
                    )
                }
                if (tag.isNotEmpty()) {
                    assertTrue(
                        flipboardPage.isArticleTagVisible(tag),
                        "Article tag not visible: $tag for article: $title"
                    )
                }
            }
        }else{
            assertTrue(flipboardPage.isNoFlipBoardEmptyStateVisible(tagList.first()),"No flipboards found")
        }
    }

}

