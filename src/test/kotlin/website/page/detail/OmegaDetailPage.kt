package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper
import website.page.WebSiteBasePage


class OmegaDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.OMEGA_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Omega Profile Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): OmegaDetailPage {
        StepHelper.step(StepHelper.WAIT_WEBSITE_PAGE_LOAD + "Omega Profile Panel")
        header.waitFor()
        logger.info { "Omega detail loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "An advanced fatty acid blood test that measures Omega-3, Omega-6, and lipid balance to evaluate inflammation, metabolism, and heart health."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Your fatty acid profile plays a vital role in cardiovascular, brain, and metabolic health. This panel analyzes Omega-3, Omega-6, saturated fats, and trans fats to assess dietary balance, inflammation risk, and long-term disease markers. With insights into Omega-3 deficiency, Omega-6/Omega-3 imbalance, and lipid-related inflammation, you can take proactive steps through diet and supplementation to improve resilience, energy, and longevity."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Dry Blood Spot (DBS) Sample").isVisible &&
                page.getByText("Fasting Optional").isVisible &&
                page.getByText("Get results in 7–10 days").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("3,999").isVisible
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

    fun isOmega3IndexVisible(): Boolean {
        return page.getByText("Levels of EPA and DHA; deficiency linked to inflammation, cardiovascular risk, and poor insulin sensitivity.").isVisible
    }

    fun isOmega6AndOmega3RatioVisible(): Boolean {
        return page.getByText("High ratios signal chronic disease risk; ideal balance is under 3:1.").isVisible
    }

    fun isPalmiticAcidIndexVisible(): Boolean {
        return page.getByText("Reflects sugar and carbohydrate overload; associated with diabetes, obesity, and metabolic syndrome.").isVisible
    }

    fun isAAEPARatioVisible(): Boolean {
        return page.getByText("Inflammation marker balancing pro-inflammatory arachidonic acid (AA) with anti-inflammatory EPA.").isVisible
    }

    fun isTransFatIndexTFIVisible(): Boolean {
        return page.getByText("Measures harmful industrial trans-fat levels; linked to diabetes, cardiovascular disease, and inflammation.").isVisible
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
            "This test is ideal for those interested in cardiovascular prevention, anti-inflammatory nutrition, and longevity-focused health planning."
        ).isVisible
    }


    fun isCardiovascularAndMetabolicRiskVisible(): Boolean {
        return page.getByText("❤\uFE0F Cardiovascular & Metabolic Risk").isVisible &&
                page.getByText("Family history of heart disease, diabetes, or obesity").isVisible &&
                page.getByText("Concern about cholesterol and triglyceride management").isVisible &&
                page.getByText("Wanting to reduce long-term cardiovascular risks").isVisible

    }

    fun isBrainAndInflammationHealthVisible(): Boolean {
        return page.getByText("❤\uFE0F Cardiovascular & Metabolic Risk").isVisible &&
                page.getByText("Chronic fatigue or mood issues").isVisible &&
                page.getByText("Suspected systemic inflammation").isVisible &&
                page.getByText("Concern about cognitive decline or memory").isVisible

    }

    fun isLifestyleAndNutritionVisible(): Boolean {
        return page.getByText(" \uD83C\uDF7D\uFE0F Lifestyle & Nutrition").isVisible &&
                page.getByText("Low fish intake or vegetarian diet").isVisible &&
                page.getByText("High reliance on processed foods and vegetable oils").isVisible &&
                page.getByText("Looking to optimise diet with supplementation").isVisible

    }




    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection:").isVisible &&
                page.getByText(
                    "Use the dry blood spot kit to collect your sample by carefully following the provided instructions. Once your sample collection is complete, please inform us so we can arrange a sample pickup from your location."
                ).isVisible
    }



    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Your fatty acid profile is processed at a certified lab and your report is ready online within 7–10 days.").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText("See how Omega-3 and Omega-6 balance impacts inflammation, metabolism, and cardiovascular health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Create a personalised nutrition and supplementation plan based on your results.").isVisible
    }


    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it works")).isVisible
    }

    fun isStep1ContentVisible(): Boolean {
        return page.getByText("01").nth(1).isVisible &&
                page.getByText("Omega Test Kit is Delivered").isVisible &&
                page.getByText("Your Omega test kit arrives at your doorstep with an easy DBS tool.").isVisible
    }

    fun isStep2ContentVisible(): Boolean {
        return page.getByText("02").nth(1).isVisible &&
                page.getByText("At-Home Self-Test Kit").isVisible &&
                page.getByText("Do easy DBS test by yourself and schedule a quick pickup from home.").isVisible
    }

    fun isStep3ContentVisible(): Boolean {
        return page.getByText("03").nth(1).isVisible &&
                page.getByText("Get results in 7–10 days").nth(1).isVisible &&
                page.getByText("Your sample is analysed in a certified lab, and results are shared on your dashboard.").isVisible
    }

    fun isStep4ContentVisible(): Boolean {
        return page.getByText("04").nth(1).isVisible &&
                page.getByText("1-on-1 Expert Consultation").nth(1).isVisible &&
                page.getByText("Discuss your gut health report with our experts and get personalised guidance.").isVisible
    }

}
