package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class EssentialAndNutrientsDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.ESSENTIAL_AND_NUTRIENTS_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Essential Nutrients Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): EssentialAndNutrientsDetailPage {
        header.waitFor()
        logger.info { "Essential Nutrients Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A focused blood test that measures key vitamins and minerals essential for energy, metabolism, and overall long-term health."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Nutrition is the foundation of health and longevity. This panel screens for the most essential nutrients and markers of deficiency or imbalance — including vitamin B12, vitamin D, iron, ferritin, and homocysteine. These biomarkers provide a clear picture of your body’s nutritional status, helping you identify deficiencies early and take corrective action through diet or supplementation."
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
        return page.getByText("₹").isVisible && page.getByText("1,299").isVisible
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

    fun isVitaminB12Visible(): Boolean {
        return page.getByText("Vitamin B12", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Essential for nerve function, red blood cell formation, and energy metabolism.").isVisible
    }

    fun isVitaminDVisible(): Boolean {
        return page.getByText("Vitamin D", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Supports bone strength, immunity, and muscle health; deficiencies are common due to limited sun exposure.").isVisible
    }


    fun isIronVisible(): Boolean {
        return page.getByText("Iron", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Vital for oxygen transport; low levels can lead to fatigue and anaemia.").isVisible
    }

    fun isFerritinVisible(): Boolean {
        return page.getByText("Ferritin", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Indicates stored iron levels and helps detect iron deficiency or overload.").isVisible
    }

    fun isHomocysteineVisible(): Boolean {
        return page.getByText("Homocysteine", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("An amino acid linked to vitamin B status; elevated levels may increase cardiovascular risk.").isVisible
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
            "This panel is recommended for anyone wanting to optimize energy, immunity, and long-term health through nutrition."
        ).isVisible
    }


    fun isEnergyAndVitalityVisible(): Boolean {
        return page.getByText("Energy & Vitality").isVisible &&
                page.getByText("Persistent fatigue or weakness").isVisible &&
                page.getByText("Difficulty concentrating or low productivity").isVisible &&
                page.getByText("Suspected vitamin deficiency").isVisible

    }


    fun isPreventiveHealthVisible(): Boolean {
        return page.getByText("Preventive Health", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Adults with limited sun exposure").isVisible &&
                page.getByText("Vegetarians, vegans, or restricted diets").isVisible &&
                page.getByText("Family history of anaemia or cardiovascular conditions").isVisible

    }


    fun isSpecificHealthConcernsVisible(): Boolean {
        return page.getByText("\uD83D\uDC69\u200D⚕\uFE0F Specific Health Concerns", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Hair loss, brittle nails, or pale skin").isVisible &&
                page.getByText("Bone or muscle weakness").isVisible &&
                page.getByText("Concern about heart and vascular health").isVisible
    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs measure key vitamins and minerals that shape energy, metabolism, and resilience. Results are physician-reviewed and delivered in your dashboard within 72 hours, along with practical guidance to improve nutrition through diet and supplementation."
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
                page.getByText("See how nutrient levels impact your energy, immunity, and long-term health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Discuss results and create a personalised nutrition plan.").isVisible
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
