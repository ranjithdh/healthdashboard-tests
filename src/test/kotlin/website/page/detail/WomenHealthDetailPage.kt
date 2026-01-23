package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class WomenHealthDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.WOMEN_HEALTH_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Women’s Health Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): WomenHealthDetailPage {
        header.waitFor()
        logger.info { "Women’s Health Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "An at-home hormone panel designed to provide insights into ovulation, and menstrual cycle health for women."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "This panel measures three essential hormones that influence reproductive health, cycle regularity, and fertility. By understanding your hormone levels, you can gain clarity on ovarian reserve, ovulation, and overall cycle function. These insights help guide next steps in planning for pregnancy or addressing women’s health concerns."
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
        return page.getByText("₹").isVisible && page.getByText("999").isVisible
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

    fun isFSHFollicleStimulatingHormoneVisible(): Boolean {
        return page.getByText("FSH (Follicle Stimulating Hormone)").isVisible &&
                page.getByText("Shows ovarian reserve and stimulates egg development; elevated levels may signal reduced egg supply.").isVisible
    }

    fun isLHLuteinizingHormoneVisible(): Boolean {
        return page.getByText("LH (Luteinizing Hormone)").isVisible &&
                page.getByText("Helps assess ovulation, supports cycle regularity, and abnormal values may indicate ovulatory disorders.").isVisible
    }


    fun isEstradiolDay2Visible(): Boolean {
        return page.getByText("Estradiol (Day 2)").isVisible &&
                page.getByText("Measures estrogen levels at a critical early cycle stage; helps evaluate ovarian reserve and hormone balance.").isVisible
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
            "This panel is valuable for women seeking to better understand their reproductive health, whether for fertility planning or managing hormonal balance."
        ).isVisible
    }


    fun isCycleIrregularitiesVisible(): Boolean {
        return page.getByText("Cycle Irregularities").isVisible &&
                page.getByText("Unpredictable or absent menstrual cycles").isVisible &&
                page.getByText("Unexplained changes in flow or timing").isVisible &&
                page.getByText("Difficulty tracking ovulation").isVisible

    }


    fun isFertilityPlanningVisible(): Boolean {
        return page.getByText("Fertility Planning", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Preparing for pregnancy or IVF").isVisible &&
                page.getByText("Considering egg freezing").isVisible &&
                page.getByText("Wanting to know ovarian reserve").isVisible

    }



    fun isHormoneBalanceVisible(): Boolean {
        return page.getByText("Hormone Balance", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Suspected hormonal imbalance").isVisible &&
                page.getByText("Symptoms like hot flashes or mood swings").isVisible &&
                page.getByText("Difficulty conceiving after several months of trying").isVisible

    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs analyze your hormone levels at key points in your cycle. Results are physician-reviewed and delivered within days in a clear, actionable report to guide your next steps in women’s health and fertility planning."
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
                page.getByText("See how your hormone levels relate to cycle regularity and reproductive health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Receive personalised guidance based on your results and goals.").isVisible
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
