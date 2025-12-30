package website.page

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

    private val header = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("H e r e i s e v e r y t h i n g w e t e s t")
    )

    private val bookNow = page.locator("#join-btn-test")

    fun waitForPageLoad(): WhatWeTestPage {
        header.waitFor()
        logger.info { "What We Test page loaded" }
        return this
    }


    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isWhatIsBiomarkerTestingTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What Is Biomarker Testing?")).isVisible
    }

    fun isWhatIsBiomarkingTextVisible(): Boolean {
        return page.getByText(
            "A biomarker test measures specific molecules in your body that reflect how your organs and systems are functioning. At Deep Holistics, biomarker testing forms the backbone of our approach to personalised wellness. It’s not about guesswork, generic plans, or one-size-fits-all solutions. Our testing uncovers precise, objective data about your body’s unique biology. This enables a deeply personalised, preventive, and data-driven approach to health, far beyond surface-level insights or symptom-based care."
        ).isVisible
    }

    fun isWhyItMattersTitleVisible(): Boolean {
        return page.getByText("Why It Matters").isVisible
    }


    fun isWhyItMattersSection1Visible(): Boolean {
        return page.getByText(
            "These markers act like signals from inside your body, helping identify early signs of imbalance, disease risk, or progress toward better health. Many health issues develop silently long before symptoms appear, which is why tracking the right biomarkers gives you the power to act early and precisely. \u200D By monitoring these indicators over time, you can:"
        ).isVisible
    }

    fun isWhyItMattersSection2Visible(): Boolean {
        return page.getByText(
            "Detect early shifts linked to conditions like diabetes, heart disease, and inflammation, long before they escalate."
        ).isVisible &&
                page.getByText(
                    "Establish your personal health baseline to understand what “normal” looks like for you and track how it evolves."
                ).isVisible &&
                page.getByText(
                    "Measure progress objectively as you adjust your nutrition, exercise, or supplement routines."
                ).isVisible &&
                page.getByText(
                    "Make data-backed decisions that reflect your unique biology, not population averages or guesswork."
                ).isVisible
    }


    fun whyItMattersSection3Visible(): Boolean {
        return page.getByText(
            "This preventive approach transforms health management from reactive care into continuous optimisation, helping you understand and influence your well-being at every stage."
        ).isVisible
    }


    fun isBaseLineBloodPanelVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Baseline Blood Panel")).isVisible
    }

    fun isBaseLineBloodPanelDescriptionVisible(): Boolean {
        return page.getByText(
            "The following 100+ biomarkers are included with your first blood test."
        ).isVisible
    }

    fun isBookNowVisible() = bookNow.isVisible


    fun isHearHealthSectionTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Heart Health")).isVisible
    }

    fun isMetabolicHeathSectionTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Metabolic Health")).isVisible
    }

    fun isInflammationTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Inflammation")).isVisible
    }

    fun isMitochondrialHealthTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Mitochondrial Health")).isVisible
    }

    fun isHormoneHealthTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Hormone Health")).isVisible
    }

    fun isImmuneHealthTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Immune Health")).isVisible
    }






    fun Apo_B_Apo_A1_Ratio_Visible(): Boolean {
        val biomarkerName = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Apo-B / Apo-A1 Ratio"))
        val description = page.getByText(
            "Compares harmful cholesterol particles to protective ones, giving a more accurate prediction of cardiovascular risk than cholesterol totals."
        )

        biomarkerName.click()
        description.waitFor()

        return biomarkerName.isVisible && description.isVisible
    }


}
