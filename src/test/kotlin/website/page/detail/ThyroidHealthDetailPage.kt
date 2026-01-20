package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class ThyroidHealthDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.THYROID_HEALTH_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Thyroid Health Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): ThyroidHealthDetailPage {
        header.waitFor()
        logger.info { "Thyroid Health Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A targeted blood test that measures thyroid hormones and ratios to assess metabolism, energy, and overall thyroid health."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Your thyroid controls metabolism, energy, and hormone balance. This panel measures thyroid-stimulating hormone (TSH), total and free thyroid hormones (T3 and T4), and key ratios. Together, these biomarkers provide a complete view of thyroid function, helping identify hypothyroidism, hyperthyroidism, or subtle imbalances that affect mood, weight, and energy."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Fasting Optional").isVisible &&
                page.getByText("At-Home Sample Collection").first().isVisible &&
                page.getByText("Get results in 72 hrs").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").first().isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("749").isVisible
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

    fun isThyroidStimulatingHormoneTSHVisible(): Boolean {
        return page.getByText("Thyroid Stimulating Hormone (TSH)").isVisible &&
                page.getByText("Master regulator of thyroid activity; high or low levels can signal hypo- or hyperthyroidism.").isVisible
    }

    fun isTotalThyroxineT4Visible(): Boolean {
        return page.getByText("Total Thyroxine (T4)").isVisible &&
                page.getByText("Measures the main thyroid hormone in circulation; reflects overall thyroid output.").isVisible
    }

    fun isFreeThyroxine_fT4Visible(): Boolean {
        return page.getByText("Free Thyroxine (fT4)").isVisible &&
                page.getByText("Active form of T4 that is available for tissues to use.").isVisible
    }

    fun isTotalTriiodothyronineT3Visible(): Boolean {
        return page.getByText("Total Triiodothyronine (T3)").isVisible &&
                page.getByText("Indicates total circulating T3, the active thyroid hormone that drives metabolism.").isVisible
    }

    fun isFreeTriiodothyronine_fT3Visible(): Boolean {
        return page.getByText("Free Triiodothyronine (fT3)").isVisible &&
                page.getByText("Measures unbound T3 available for immediate use in cells.").isVisible
    }

    fun isT3_T4RatioVisible(): Boolean {
        return page.getByText("T3 / T4 Ratio").isVisible &&
                page.getByText("Helps assess thyroid hormone conversion efficiency.").isVisible
    }

    fun isFreeT3_Free_T4_RatioVisible(): Boolean {
        return page.getByText("Free T3 / Free T4 Ratio").isVisible &&
                page.getByText("Provides deeper insight into balance between free hormones and metabolic activity.").isVisible
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
            "This panel is especially useful for those with symptoms or risk factors of thyroid dysfunction."
        ).isVisible
    }


    fun isEnergyAndMetabolismVisible(): Boolean {
        return page.getByText("Energy & Metabolism").isVisible &&
                page.getByText("Unexplained fatigue or weight changes").isVisible &&
                page.getByText("Slow or rapid metabolism").isVisible &&
                page.getByText("Hair loss or skin dryness").isVisible

    }


    fun isMoodAndHormoneBalanceVisible(): Boolean {
        return page.getByText("Mood & Hormone Balance").isVisible &&
                page.getByText("Mood swings, anxiety, or brain fog").isVisible &&
                page.getByText("Irregular menstrual cycles").isVisible &&
                page.getByText("Hair loss or skin dryness").isVisible

    }

    fun isFamilyAndRiskFactorsVisible(): Boolean {
        return page.getByText("Family & Risk Factors").isVisible &&
                page.getByText("Family history of thyroid disorders").isVisible &&
                page.getByText("Previous abnormal thyroid results").isVisible &&
                page.getByText("Autoimmune risk such as Hashimoto’s or Graves’ disease").isVisible

    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs measure thyroid hormone levels and ratios to assess overall thyroid function. Results are physician-reviewed and delivered in your dashboard within 72 hours, along with a consult to help you understand your results and create an action plan."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection:").isVisible &&
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
                page.getByText("See how your thyroid levels connect to symptoms and metabolism.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Review results and receive personalised guidance for thyroid health.").isVisible
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
