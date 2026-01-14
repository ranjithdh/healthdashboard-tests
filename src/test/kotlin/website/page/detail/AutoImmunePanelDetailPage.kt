package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class AutoImmunePanelDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.AUTO_IMMUNE_PANEL_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Autoimmune Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): AutoImmunePanelDetailPage {
        header.waitFor()
        logger.info { "Autoimmune Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "An at-home test that detects autoimmune activity by measuring antibodies associated with lupus and rheumatoid arthritis."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "This panel measures critical antibodies involved in autoimmune responses. These markers can help identify early warning signs of conditions such as lupus, rheumatoid arthritis, and other autoimmune disorders. With timely insights, you can seek medical advice sooner and manage your long-term health more effectively."
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

    fun isANAMarkerVisible(): Boolean {
        return page.getByText("ANA (Antinuclear Antibodies)").isVisible &&
                page.getByText(
                    "Detects antibodies that target the nucleus of cells, commonly elevated in autoimmune conditions such as lupus or mixed connective tissue disease."
                ).isVisible
    }


    fun isDsDNAMarkerVisible(): Boolean {
        return page.getByText("dsDNA (Double-Stranded DNA Antibodies)").isVisible &&
                page.getByText(
                    "Highly specific marker for lupus; elevated levels may indicate disease activity and help guide early medical attention."
                ).isVisible
    }

    fun isRAFactorMarkerVisible(): Boolean {
        return page.getByText("RA Factor (Rheumatoid Factor)").isVisible &&
                page.getByText(
                    "Measures antibodies associated with rheumatoid arthritis. Elevated levels can point toward joint inflammation or other autoimmune conditions."
                ).isVisible
    }

    fun isAntiCCPMarkerVisible(): Boolean {
        return page.getByText("Anti-CCP (Anti-Cyclic Citrullinated Peptide)").isVisible &&
                page.getByText(
                    "A sensitive marker for rheumatoid arthritis. Positive results may indicate increased risk of developing more severe or progressive joint disease."
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
            "Understanding your immune activity can provide valuable insights into unexplained symptoms and long-term risks. This panel is especially helpful if you have a family history of autoimmune conditions or are experiencing persistent, unexplained health issues."
        ).isVisible
    }

    fun isJointHealthVisible(): Boolean {
        return page.getByText("Joint Health").isVisible &&
                page.getByText("Persistent joint pain or stiffness").isVisible &&
                page.getByText("Swelling in small or large joints").isVisible &&
                page.getByText("Morning stiffness lasting more than 30 minutes").isVisible
    }

    fun isChronicSymptomsVisible(): Boolean {
        return page.getByText("Chronic Symptoms").isVisible &&
                page.getByText("Unexplained fatigue or muscle weakness").isVisible &&
                page.getByText("Frequent low-grade fevers").isVisible &&
                page.getByText("Ongoing skin rashes or sensitivity to sunlight").isVisible
    }

    fun isFamilyHistoryVisible(): Boolean {
        return page.getByText("Family History", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Close relatives with autoimmune disorders").isVisible &&
                page.getByText("Higher genetic risk of conditions like lupus or rheumatoid arthritis").isVisible &&
                page.getByText("Concern about developing autoimmune disease in the future").isVisible
    }

    fun isUnexplainedInflammationVisible(): Boolean {
        return page.getByText("Unexplained Inflammation").isVisible &&
                page.getByText("Recurrent swelling without clear cause").isVisible &&
                page.getByText("Inflammatory markers elevated in previous tests").isVisible &&
                page.getByText("Symptoms that don’t resolve with routine care").isVisible
    }



    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs analyze key antibody markers linked to autoimmune activity. Results are physician-reviewed and delivered in a clear, easy-to-understand report within days, along with guidance on what next steps may be appropriate."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("At-Home Sample Collection").nth(1).isVisible &&
                page.getByText(
                    "Schedule the blood sample collection from the comfort of your home."
                ).isVisible
    }


    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Your sample is processed at a certified lab and your report is ready online within 72 hours.").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText("See how antibody activity relates to your symptoms and potential autoimmune conditions.").isVisible
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
