package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper
import website.page.WebSiteBasePage


class WomenFertilityDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.WOMEN_FERTILITY_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Women's Fertility Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): WomenFertilityDetailPage {
        StepHelper.step(StepHelper.WAIT_WEBSITE_PAGE_LOAD + "Women's Fertility Panel")
        header.waitFor()
        logger.info { "Women's Fertility Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A complete at-home hormone and metabolic panel to assess fertility readiness and reproductive health."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "his panel measures key reproductive and metabolic hormones that influence ovarian reserve, ovulation, and overall fertility. With insights from six critical biomarkers, you can better understand your reproductive window and identify potential barriers to conception."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Day 2 of your period").isVisible &&
                page.getByText("No fasting required").isVisible &&
                page.getByText("At-home sample collection", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Get results in 72 hrs").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
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

    fun isWhtMeasuredDescriptionVisible(): Boolean {
        return page.getByText("Here’s what the panel includes, with plain explanations:").isVisible
    }


    fun isAMHMarkerVisible(): Boolean {
        return page.getByText("AMH (Anti-Müllerian Hormone)").isVisible &&
                page.getByText(
                    "Indicates ovarian reserve by estimating how many eggs remain. Higher AMH generally reflects a greater number of available eggs."
                ).isVisible
    }

    fun isProlactinMarkerVisible(): Boolean {
        return page.getByText("Prolactin").isVisible &&
                page.getByText(
                    "A hormone involved in milk production. Elevated prolactin can interfere with ovulation and menstrual regularity."
                ).isVisible
    }

    fun isFSHMarkerVisible(): Boolean {
        return page.getByText("FSH (Follicle Stimulating Hormone)").isVisible &&
                page.getByText(
                    "Helps regulate the menstrual cycle and stimulates egg growth. High levels may indicate reduced ovarian reserve."
                ).isVisible
    }

    fun isLHMarkerVisible(): Boolean {
        return page.getByText("LH (Luteinizing Hormone)").isVisible &&
                page.getByText(
                    "Triggers ovulation and helps regulate the menstrual cycle. Abnormal levels may signal PCOS or other ovulatory disorders."
                ).isVisible
    }

    fun isEstradiolDay2MarkerVisible(): Boolean {
        return page.getByText("Estradiol (Day 2)").isVisible &&
                page.getByText(
                    "The primary form of estrogen. Measuring estradiol early in the cycle helps assess ovarian reserve and hormone balance."
                ).isVisible
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
            "Understanding your reproductive hormones can give valuable insights into your fertility health. This panel is especially helpful if you are planning a pregnancy, managing cycle irregularities, or exploring fertility treatment options."
        ).isVisible
    }

    fun isCycleHealthVisible(): Boolean {
        return page.getByText("Cycle Health").isVisible &&
                page.getByText("Irregular or absent menstrual cycles").isVisible &&
                page.getByText("Very short or long cycle lengths").isVisible &&
                page.getByText("Unexplained changes in flow or timing").isVisible
    }

    fun isOvarianReserveVisible(): Boolean {
        return page.getByText("Ovarian Reserve",Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Considering egg freezing or IVF").isVisible &&
                page.getByText("Planning pregnancy later in life").isVisible &&
                page.getByText("Wanting to know egg reserve status").isVisible
    }

    fun isOvulationAndHormoneBalanceVisible(): Boolean {
        return page.getByText("Ovulation and Hormone Balance").isVisible &&
                page.getByText("Suspected PCOS or hormonal imbalance").isVisible &&
                page.getByText("Difficulty conceiving after 6–12 months of trying").isVisible &&
                page.getByText("Symptoms of estrogen or progesterone imbalance").isVisible
    }

    fun isMetabolicHealthVisible(): Boolean {
        return page.getByText("Metabolic Health").isVisible &&
                page.getByText("Insulin resistance or high blood sugar").isVisible &&
                page.getByText("Unexplained weight gain").isVisible &&
                page.getByText("Family history of diabetes affecting fertility").isVisible
    }




    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple blood sample taken on specific days of your cycle, our certified labs analyze key fertility hormones and insulin levels. Results are physician-reviewed and delivered in a clear, easy-to-understand report within days, along with guidance on next steps for your fertility journey."
        ).isVisible
    }


    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection", Page.GetByTextOptions().setExact(true)).isVisible &&
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
                page.getByText("See how your hormone levels connect to your fertility health and menstrual cycle.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Get your questions answered and receive personalized guidance and action plan.").isVisible
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
