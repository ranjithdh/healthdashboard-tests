package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * FAQ Page (https://www.deepholistics.com/faq)
 * Frequently asked questions with accordion functionality
 */
class FaqPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.MARKETING_FAQ

    fun waitForPageLoad(): FaqPage {
        element("div.faq-toggle-small").first().waitFor()
        logger.info { "FAQ page loaded" }
        return this
    }

    // ---------------------- Page Content ----------------------

    fun isPageHeadingVisible(): Boolean {
        return byText("Frequently Asked Questions").isVisible ||
               byText("FAQs").isVisible ||
               byText("FAQ").isVisible
    }

    // ---------------------- Category Tabs ----------------------

    fun isCategoryTabsVisible(): Boolean {
        return byText("General").isVisible
    }

    fun isGeneralTabVisible(): Boolean {
        return byText("General").isVisible
    }

    fun isAppointmentTabVisible(): Boolean {
        return byText("Appointment").isVisible ||
               byText("Process").isVisible
    }

    fun clickGeneralTab(): FaqPage {
        logger.info { "Clicking General tab" }
        byText("General").click()
        return this
    }

    fun clickAppointmentTab(): FaqPage {
        logger.info { "Clicking Appointment tab" }
        byText("Appointment").click()
        return this
    }

    // ---------------------- FAQ Accordion ----------------------

    fun getFaqAccordionItems(): Locator {
        return element("div.faq-toggle-small")
    }

    fun getFaqItemsCount(): Int {
        return getFaqAccordionItems().count()
    }

    fun clickFaqItem(index: Int): FaqPage {
        logger.info { "Clicking FAQ item at index: $index" }
        getFaqAccordionItems().nth(index).click()
        return this
    }

    fun isFaqItemExpanded(index: Int): Boolean {
        val item = getFaqAccordionItems().nth(index)
        // Check if the answer content is visible (expanded state)
        // This depends on the actual implementation - typically a sibling or child element
        return item.locator("+ div, .faq-answer, [class*='answer']").isVisible
    }

    fun getFaqQuestionText(index: Int): String {
        return getFaqAccordionItems().nth(index).textContent() ?: ""
    }

    // ---------------------- Specific FAQ Questions ----------------------

    fun isWhatIsDeepHolisticsQuestionVisible(): Boolean {
        return byText("What is Deep Holistics").isVisible
    }

    fun isHowDoesItWorkQuestionVisible(): Boolean {
        return byText("How does it work").isVisible ||
               byText("How do I").isVisible
    }

    fun isPricingQuestionVisible(): Boolean {
        return byText("price").isVisible ||
               byText("cost").isVisible ||
               byText("How much").isVisible
    }

    // ---------------------- Contact Section ----------------------

    fun isContactSectionVisible(): Boolean {
        return byText("Still have questions").isVisible ||
               byText("Contact").isVisible
    }

    fun isContactEmailVisible(): Boolean {
        return byText("@deepholistics.com").isVisible
    }
}
