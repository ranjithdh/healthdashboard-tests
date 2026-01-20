package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class AdvancedThyroidDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.ADVANCED_THYROID_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Thyroid Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): AdvancedThyroidDetailPage {
        header.waitFor()
        logger.info { "Advanced Thyroid Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A comprehensive at-home test for detecting autoimmune thyroid conditions, aiding in early diagnosis of thyroid disorders."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "his panel measures thyroid-specific antibodies that play a critical role in autoimmune thyroid diseases. These markers can help identify Hashimoto’s thyroiditis, Graves’ disease, and other thyroid dysfunctions. With clear insights into antibody activity, you can take proactive steps with your doctor to manage thyroid health more effectively."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("No Fasting Required").isVisible &&
                page.getByText("At-Home Sample Collection").first().isVisible &&
                page.getByText("Get results in 72 hrs").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").first().isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("1,499").isVisible
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

    fun isThyroglobulinAntibodiesVisible(): Boolean {
        return page.getByText("Thyroglobulin Antibodies").isVisible &&
                page.getByText("Detects antibodies against thyroglobulin, a protein involved in thyroid hormone production. Elevated levels are commonly seen in Hashimoto’s thyroiditis.").isVisible
    }

    fun isThyroidPeroxidaseTPOAntibodiesVisible(): Boolean {
        return page.getByText("Thyroid Peroxidase (TPO) Antibodies").isVisible &&
                page.getByText("Measures antibodies targeting thyroid peroxidase, an enzyme crucial for thyroid hormone synthesis. High levels often indicate autoimmune thyroid disease.").isVisible
    }

    fun isTSHReceptorAntibodiesVisible(): Boolean {
        return page.getByText("TSH Receptor Antibodies").isVisible &&
                page.getByText("Identifies antibodies that bind to thyroid-stimulating hormone receptors. Elevated levels are linked to Graves’ disease and hyperthyroidism.").isVisible
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
            "Tracking thyroid antibodies can provide early detection of autoimmune thyroid disease and help explain unexplained symptoms. This panel is especially useful for those with family history or signs of thyroid dysfunction."
        ).isVisible
    }


    fun isThyroidSymptomsVisible(): Boolean {
        return page.getByText("Thyroid Symptoms").isVisible &&
                page.getByText("Unexplained weight gain or loss").isVisible &&
                page.getByText("Fatigue, sluggishness, or low energy").isVisible &&
                page.getByText("Sensitivity to cold or heat").isVisible

    }


    fun isFamilyHistoryVisible(): Boolean {
        return page.getByText("Family History", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Family history of thyroid disease").isVisible &&
                page.getByText("Genetic risk for Hashimoto’s or Graves’ disease").isVisible &&
                page.getByText("Concern about developing thyroid autoimmunity").isVisible

    }


    fun isHormoneImbalancesVisible(): Boolean {
        return page.getByText("Hormone Imbalances").isVisible &&
                page.getByText("Irregular menstrual cycles or fertility challenges").isVisible &&
                page.getByText("Mood swings, anxiety, or depression").isVisible &&
                page.getByText("Hair loss, brittle nails, or dry skin").isVisible
    }

    fun isPersistentHealthIssuesVisible(): Boolean {
        return page.getByText("Persistent Health Issues").isVisible &&
                page.getByText("Goiter or swelling at the base of the neck").isVisible &&
                page.getByText("Difficulty regulating body temperature").isVisible &&
                page.getByText("Symptoms that don’t improve with standard care").isVisible
    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs measure thyroid antibodies to assess autoimmune thyroid activity. Results are physician-reviewed and delivered in a clear, easy-to-understand report within days, along with next-step guidance to support your thyroid health."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("At-Home Sample Collection").nth(1).isVisible &&
                page.getByText(
                    "Book a convenient at-home blood draw with our certified phlebotomist."
                ).isVisible
    }


    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Your sample is processed at a certified lab and your report is ready online within 72 hours.").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText("See how thyroid antibodies relate to your symptoms and risk for autoimmune thyroid disease.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Get your questions answered and receive a personalised guidance plan.").isVisible
    }


    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it works")).isVisible
    }

    fun isStep1ContentVisible(): Boolean {
        return page.getByText("01").first().isVisible &&
                page.getByText("At-Home Sample Collection").nth(1).isVisible &&
                page.getByText("Schedule the blood sample collection from the comfort of your home.").isVisible
    }

    fun isStep2ContentVisible(): Boolean {
        return page.getByText("02").first().isVisible &&
                page.getByText("Get Results in 72 Hours").isVisible &&
                page.getByText("Your sample is processed at a certified lab, and your report is ready online in 72 hours.").isVisible
    }

    fun isStep3ContentVisible(): Boolean {
        return page.getByText("03").first().isVisible &&
                page.getByText("1-on-1 Expert Consultation").first().isVisible &&
                page.getByText("See how your antibody levels connect with your symptoms by talking to our experts.").isVisible
    }

    fun isStep4ContentVisible(): Boolean {
        return page.getByText("04").first().isVisible &&
                page.getByText("Track Progress Overtime").isVisible &&
                page.getByText("Monitor these markers over time to understand changes and treatment response.").isVisible
    }

}
