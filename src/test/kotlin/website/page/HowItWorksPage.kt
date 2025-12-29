package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * How It Works Page (https://www.deepholistics.com/how-it-works)
 * Explains the step-by-step process of using Deep Holistics
 */
class HowItWorksPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.MARKETING_HOW_IT_WORKS

    fun waitForPageLoad(): HowItWorksPage {
        page.getByText("F", Page.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("r", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.getByText("o", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.locator("h1").getByText("m").waitFor()
        page.getByText("T", Page.GetByTextOptions().setExact(true)).waitFor()
        page.locator("h1").getByText("e", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.locator("h1").getByText("s", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("t", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.getByText("o", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.getByText("C", Page.GetByTextOptions().setExact(true)).waitFor()
        page.locator("h1").getByText("l", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.locator("h1").getByText("a", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("r", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.locator("h1").getByText("i", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(2).waitFor()
        page.locator("h1").getByText("y").waitFor()
        page.locator("h1").getByText(".").waitFor()
        logger.info { "How It Works page loaded" }
        return this
    }

    // ---------------------- Page Content ----------------------

    fun isPageHeadingVisible(): Boolean {
        return  page.getByText("F", Page.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("r", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.getByText("o", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.locator("h1").getByText("m").isVisible &&
        page.getByText("T", Page.GetByTextOptions().setExact(true)).isVisible &&
        page.locator("h1").getByText("e", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.locator("h1").getByText("s", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("t", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.getByText("o", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.getByText("C", Page.GetByTextOptions().setExact(true)).isVisible &&
        page.locator("h1").getByText("l", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.locator("h1").getByText("a", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("r", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.locator("h1").getByText("i", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(2).isVisible &&
        page.locator("h1").getByText("y").isVisible &&
        page.locator("h1").getByText(".").isVisible
    }

    fun isStep1Visible(): Boolean {
        return byText("Book").isVisible || byText("Step 1").isVisible
    }

    fun isStep2Visible(): Boolean {
        return byText("Test").isVisible || byText("Step 2").isVisible
    }

    fun isStep3Visible(): Boolean {
        return byText("Results").isVisible || byText("Step 3").isVisible
    }

    fun isStep4Visible(): Boolean {
        return byText("Track").isVisible || byText("Step 4").isVisible
    }

    // ---------------------- CTAs ----------------------

    fun isBookNowCtaVisible(): Boolean {
        return element("a[href*='login']").isVisible ||
               byText("Book Now").isVisible
    }

    fun clickBookNowCta() {
        logger.info { "Clicking Book Now CTA on How It Works page" }
        byText("Book Now").click()
    }

    // ---------------------- Benefits Section ----------------------

    fun isBenefitsSectionVisible(): Boolean {
        return byText("Why choose").isVisible ||
               byText("Benefits").isVisible
    }
}
