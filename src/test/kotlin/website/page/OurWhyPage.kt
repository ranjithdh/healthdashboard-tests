package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Our Why Page (https://www.deepholistics.com/our-why)
 * Company mission, story, and values
 */
class OurWhyPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.MARKETING_OUR_WHY

    fun waitForPageLoad(): OurWhyPage {
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Our Why")).waitFor()
        logger.info { "Our Why page loaded" }
        return this
    }

    // ---------------------- Page Content ----------------------

    fun isPageHeadingVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Our Why")).isVisible
    }

    fun isMissionStatementVisible(): Boolean {
        return byText("mission").isVisible ||
               byText("believe").isVisible ||
               byText("health").isVisible
    }

    // ---------------------- Story Section ----------------------

    fun isStorySectionVisible(): Boolean {
        return byText("story").isVisible ||
               byText("journey").isVisible ||
               byText("founded").isVisible
    }

    // ---------------------- Team/Founder Section ----------------------

    fun isTeamSectionVisible(): Boolean {
        return byText("team").isVisible ||
               byText("founder").isVisible
    }

    // ---------------------- Values Section ----------------------

    fun isValuesSectionVisible(): Boolean {
        return byText("values").isVisible ||
               byText("principles").isVisible
    }

    // ---------------------- CTAs ----------------------

    fun isBookNowCtaVisible(): Boolean {
        return byText("Book Now").isVisible ||
               element("a[href*='login']").isVisible
    }

    fun clickBookNowCta() {
        logger.info { "Clicking Book Now CTA on Our Why page" }
        byText("Book Now").click()
    }
}
