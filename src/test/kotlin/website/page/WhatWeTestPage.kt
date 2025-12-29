package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * What We Test Page (https://www.deepholistics.com/what-we-test)
 * Details about biomarkers and tests offered
 */
class WhatWeTestPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.MARKETING_WHAT_WE_TEST

    fun waitForPageLoad(): WhatWeTestPage {
        page.getByText("H", Page.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("e", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.getByText("r", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.getByText("i", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.getByText("s", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(2).waitFor()
        page.locator("h1").getByText("v").waitFor()
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(3).waitFor()
        page.getByText("r", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.locator("h1").getByText("y").waitFor()
        page.getByText("t", Page.GetByTextOptions().setExact(true)).first().waitFor()
        page.locator("h1").getByText("h", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("i", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.locator("h1").getByText("n", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.locator("h1").getByText("g", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.locator("h1").getByText("w", Locator.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(4).waitFor()
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(5).waitFor()
        page.getByText("s", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(2).waitFor()

        logger.info { "What We Test page loaded" }
        return this
    }

    // ---------------------- Page Content ----------------------

    fun isPageHeadingVisible(): Boolean {
        return page.getByText("H", Page.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("e", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.getByText("r", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.getByText("i", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.getByText("s", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(2).isVisible &&
        page.locator("h1").getByText("v").isVisible &&
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(3).isVisible &&
        page.getByText("r", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.locator("h1").getByText("y").isVisible &&
        page.getByText("t", Page.GetByTextOptions().setExact(true)).first().isVisible &&
        page.locator("h1").getByText("h", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("i", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.locator("h1").getByText("n", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.locator("h1").getByText("g", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.locator("h1").getByText("w", Locator.GetByTextOptions().setExact(true)).isVisible &&
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(4).isVisible &&
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.getByText("e", Page.GetByTextOptions().setExact(true)).nth(5).isVisible &&
        page.getByText("s", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
        page.getByText("t", Page.GetByTextOptions().setExact(true)).nth(2).isVisible
    }

    fun isBiomarkersDescriptionVisible(): Boolean {
        return byText("biomarkers").isVisible ||
               byText("100+").isVisible
    }

    // ---------------------- Test Categories ----------------------

    fun isBloodTestCategoryVisible(): Boolean {
        return byText("Blood").isVisible
    }

    fun isGeneticTestCategoryVisible(): Boolean {
        return byText("Gene").isVisible || byText("Genetic").isVisible
    }

    fun isGutTestCategoryVisible(): Boolean {
        return byText("Gut").isVisible
    }

    // ---------------------- Biomarker Sections ----------------------

    fun isHeartHealthSectionVisible(): Boolean {
        return byText("Heart").isVisible || byText("Cardiovascular").isVisible
    }

    fun isThyroidSectionVisible(): Boolean {
        return byText("Thyroid").isVisible
    }

    fun isLiverSectionVisible(): Boolean {
        return byText("Liver").isVisible
    }

    fun isKidneySectionVisible(): Boolean {
        return byText("Kidney").isVisible
    }

    fun isNutrientsSectionVisible(): Boolean {
        return byText("Nutrients").isVisible || byText("Vitamins").isVisible
    }

    // ---------------------- CTAs ----------------------

    fun isBookNowCtaVisible(): Boolean {
        return byText("Book Now").isVisible ||
               element("a[href*='login']").isVisible
    }

    fun clickBookNowCta() {
        logger.info { "Clicking Book Now CTA on What We Test page" }
        byText("Book Now").click()
    }
}
