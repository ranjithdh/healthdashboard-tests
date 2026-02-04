package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper
import website.page.WebSiteBasePage


class AdvancedHeartHealthDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.HEART_HEALTH_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Heart Health Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): AdvancedHeartHealthDetailPage {
        StepHelper.step(StepHelper.WAIT_WEBSITE_PAGE_LOAD + "Advanced Heart Health Panel")
        header.waitFor()
        logger.info { "Advanced Heart Health Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A comprehensive blood test that measures advanced cardiac biomarkers to assess heart health, inflammation, and long-term cardiovascular risk."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Cardiovascular disease often develops silently, long before symptoms appear. This panel goes beyond basic cholesterol testing, measuring advanced lipoproteins, inflammation markers, and apolipoproteins that drive long-term heart and vascular health. With a complete view of lipid balance, arterial inflammation, and hidden cardiac risk factors, you can take preventive action to protect your heart and improve longevity."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Fasting Recommended").isVisible &&
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

    fun isLipidProfileAndRatiosVisible(): Boolean {
        return page.getByText("Lipid Profile & Ratios").isVisible &&
                page.getByText("Total Cholesterol", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("HDL Cholesterol",Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("LDL Cholesterol",Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Triglycerides (TGL)").isVisible &&
                page.getByText("Very Low-Density Lipoprotein (VLDL) Cholesterol").isVisible &&
                page.getByText("Non-HDL Cholesterol").isVisible &&
                page.getByText("Total Cholesterol to HDL Ratio (TC/HDL)").isVisible &&
                page.getByText("LDL / HDL Ratio").isVisible &&
                page.getByText("HDL / LDL Ratio").isVisible &&
                page.getByText("Triglycerides / HDL Ratio (TRIG/HDL)").isVisible
    }

    fun isAdvancedLipoproteinsVisible(): Boolean {
        return page.getByText("Advanced Lipoproteins",Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Small Dense LDL Cholesterol (sdLDL-C)").isVisible &&
                page.getByText("Lipoprotein A [Lp(a)]").isVisible
    }

    fun isApolipoproteinsVisible(): Boolean {
        return page.getByText("Apolipoproteins",Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Apolipoprotein A1 (Apo-A1)").isVisible &&
                page.getByText("Apolipoprotein B (Apo-B)").isVisible &&
                page.getByText("Apo-B / Apo-A1 Ratio").isVisible
    }

    fun isInflammationMarkersVisible(): Boolean {
        return page.getByText("Inflammation Markers",Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("High Sensitivity C-Reactive Protein (hs-CRP)").isVisible &&
                page.getByText("Interleukin-6 (IL-6)").isVisible
    }

    fun isMetabolicMarkerVisible(): Boolean {
        return page.getByText("Metabolic Marker").isVisible &&
                page.getByText("Homocysteine").isVisible
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
            "This panel is ideal for individuals seeking a deeper understanding of their heart health, especially those with family history or lifestyle risks."
        ).isVisible
    }


    fun isAtRiskIndividualsVisible(): Boolean {
        return page.getByText("At-Risk Individuals").isVisible &&
                page.getByText("Family history of heart attack or stroke").isVisible &&
                page.getByText("Existing conditions like diabetes or hypertension").isVisible &&
                page.getByText("High cholesterol despite lifestyle management").isVisible
    }

    fun isPerformanceAndLongevitySeekersVisible(): Boolean {
        return page.getByText("Performance & Longevity Seekers").isVisible &&
                page.getByText("Professionals and athletes wanting to optimise cardiovascular resilience").isVisible &&
                page.getByText("Adults interested in preventive heart health screening").isVisible &&
                page.getByText("People planning midlife health optimisation").isVisible
    }

    fun isInflammationAndRiskMonitoringVisible(): Boolean {
        return page.getByText("Inflammation & Risk Monitoring").isVisible &&
                page.getByText("Chronic low-grade inflammation or metabolic syndrome").isVisible &&
                page.getByText("History of obesity, smoking, or sedentary lifestyle").isVisible &&
                page.getByText("Wanting clarity on hidden risk factors beyond standard cholesterol").isVisible
    }




    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs analyze advanced cardiac biomarkers that reveal lipid balance, inflammation, and metabolic stress. Results are physician-reviewed and delivered in your dashboard within 72 hours, along with a consult to help you build a personalised preventive plan for stronger cardiovascular health."
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
                page.getByText("See how lipid levels, inflammation, and metabolic stress connect to heart health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Review results and create a personalised action plan to protect cardiovascular health.").isVisible
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
