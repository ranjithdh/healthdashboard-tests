package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper
import website.page.WebSiteBasePage


class GutMicrobiomeDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.GUT_DETAIL

    private val header =
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Gut Microbiome Analysis"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): GutMicrobiomeDetailPage {
        StepHelper.step(StepHelper.WAIT_WEBSITE_PAGE_LOAD + "Advanced Gut Microbiome Analysis")
        header.waitFor()
        logger.info { "GutMicrobiomeDetailPage loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "An advanced at-home stool test that analyses your gut bacteria, digestion, immunity and overall microbiome health for personalized insights."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Your gut microbiome plays a central role in digestion, metabolism, immunity, and overall health. This test measures the balance and diversity of gut bacteria, identifies harmful microbes, and evaluates digestion efficiency, inflammation markers, and disease risk indicators. With this information, you can take targeted actions to restore balance and improve long-term well-being."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Stool Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("At-home collection kit").isVisible &&
                page.getByText("Get results in 7–10 days").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("8,999").isVisible
    }

    private val whatIsMeasured = page.getByText("What’s measured?")
    private val whoShouldTakeThisTest = page.getByText("Who should take this test?")
    private val whatToExpect = page.getByText("What to expect?")

    fun isWhatIsMeasuredSectionVisible(): Boolean {
        return whatIsMeasured.isVisible
    }

    fun clickWhatIsMeasuredButton() {
        whatIsMeasured.click()
    }

    fun isGutBacteriaDiversityAndBalanceVisible(): Boolean {
        return page.getByText("Gut Bacteria Diversity and Balance").isVisible &&
                page.getByText("Overall microbial diversity").isVisible &&
                page.getByText("Ratio of beneficial to harmful species").isVisible &&
                page.getByText("Abundance of key good bacteria (e.g., Lactobacillus, Bifidobacterium)").isVisible &&
                page.getByText("Pathogenic or harmful bacteria presence (e.g., Clostridium, Escherichia)").isVisible
    }

    fun isDigestionAndMetabolismVisible(): Boolean {
        return page.getByText("Digestion and Metabolism").isVisible &&
                page.getByText("Short-chain fatty acid producers").isVisible &&
                page.getByText("Fiber-degrading bacteria").isVisible &&
                page.getByText("Markers for vitamin synthesis (B vitamins, K2)").isVisible &&
                page.getByText("Protein and fat fermentation profiles").isVisible
    }

    fun isImmunityAndInflammationVisible(): Boolean {
        return page.getByText("Immunity and Inflammation").isVisible &&
                page.getByText("Presence of bacteria linked to inflammation or immune disorders").isVisible &&
                page.getByText("Potential for leaky gut and gut barrier status").isVisible

    }

    fun isDiseaseRiskIndicatorsVisible(): Boolean {
        return page.getByText("Disease Risk Indicators", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Microbes associated with metabolic, heart, and autoimmune conditions").isVisible &&
                page.getByText("Overall risk scoring compared to healthy populations").isVisible

    }


    fun isWhoShouldTakeThisTestVisible(): Boolean {
        return whoShouldTakeThisTest.isVisible
    }

    fun clickWhoShouldTakeThisTest() {
        whoShouldTakeThisTest.scrollIntoViewIfNeeded()
        return whoShouldTakeThisTest.click()
    }

    fun isWhoShouldTakeThisDescriptionVisible(): Boolean {
        return page.getByText(
            "A gut microbiome analysis can uncover hidden imbalances that impact digestion, energy, mood, and disease risk. This test is ideal for anyone seeking to improve digestive health, strengthen immunity, or personalise nutrition."
        ).isVisible
    }


    fun isDigestiveIssuesVisible(): Boolean {
        return page.getByText("Digestive Issues").isVisible &&
                page.getByText("Bloating, gas, or abdominal discomfort").isVisible &&
                page.getByText("Constipation or diarrhea").isVisible &&
                page.getByText("Food intolerances or sensitivities").isVisible

    }

    fun isImmuneAndInflammationConcernsVisible(): Boolean {
        return page.getByText("Immune & Inflammation Concerns").isVisible &&
                page.getByText("Frequent infections or low immunity").isVisible &&
                page.getByText("Chronic inflammation or autoimmune suspicion").isVisible &&
                page.getByText("Symptoms of leaky gut").isVisible

    }

    fun isEnergyAndMetabolismVisible(): Boolean {
        return page.getByText("Energy & Metabolism").isVisible &&
                page.getByText("Unexplained fatigue or low energy").isVisible &&
                page.getByText("Difficulty managing weight").isVisible &&
                page.getByText("Blood sugar or cholesterol fluctuations").isVisible

    }

    fun isPreventiveHealthAndLongevityVisible(): Boolean {
        return page.getByText("Preventive Health & Longevity").isVisible &&
                page.getByText("Interested in personalised nutrition").isVisible &&
                page.getByText("Want to reduce long-term disease risk").isVisible &&
                page.getByText("Looking to optimise gut health for overall well-being").isVisible

    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple stool sample collected at home, our certified labs analyse your gut microbiome across key domains of digestion, immunity, and disease risk. Results are physician-reviewed and delivered in an easy-to-read dashboard within 7–10 days, along with actionable recommendations to restore balance and optimise gut health."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection:", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText(
                    "Collect your stool sample at home using the kit provided.Send for Processing: Use the prepaid return label to mail your sample to our certified lab."
                ).isVisible
    }

    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Access your personalised microbiome report online within 7–10 days.").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText("See how your gut bacteria diversity, metabolism, and immune markers relate to your health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Discuss your results and receive tailored recommendations to improve gut health.").isVisible
    }



    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it works")).isVisible
    }

    fun isStep1ContentVisible(): Boolean {
        return page.getByText("01").nth(1).isVisible &&
                page.getByText("Gut Test Kit is delivered").isVisible &&
                page.getByText("Your gut test kit arrives at your doorstep with easy sample collection instructions.").isVisible
    }

    fun isStep2ContentVisible(): Boolean {
        return page.getByText("02").nth(1).isVisible &&
                page.getByText("At-Home Self-Test Kit").isVisible &&
                page.getByText("Collect your stool sample and schedule a quick pickup from home.").isVisible
    }

    fun isStep3ContentVisible(): Boolean {
        return page.getByText("03").nth(1).isVisible &&
                page.getByText("Get results in 7–10 days").first().isVisible &&
                page.getByText("Your sample is analysed in a certified lab, and results are shared on your dashboard.").isVisible
    }

    fun isStep4ContentVisible(): Boolean {
        return page.getByText("04").nth(1).isVisible &&
                page.getByText("-on-1 Expert Consultation").nth(1).isVisible &&
                page.getByText("Discuss your gut health report with our experts and get personalised guidance.").isVisible
    }

}
