package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper
import website.page.WebSiteBasePage


class BloodHealthDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.BLOOD_HEALTH_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Blood Health Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): BloodHealthDetailPage {
        StepHelper.step(StepHelper.WAIT_WEBSITE_PAGE_LOAD + "Blood Health Panel")
        header.waitFor()
        logger.info { "Blood Health Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "Comprehensive blood panel evaluating red cells, hemoglobin, and platelets for oxygen transport, clotting, and overall vitality."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Healthy blood is essential for carrying oxygen, transporting nutrients, and supporting immunity. This panel measures red blood cell indices, hemoglobin, and platelet health to detect anemia, clotting issues, and other hidden imbalances. By understanding these markers, you gain insight into energy levels, resilience, and overall circulatory health — vital for long-term wellness and early disease detection."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Fasting Optional").isVisible &&
                page.getByText("At-Home Sample Collection").first().isVisible &&
                page.getByText("Get results in 72 hrs").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("499").isVisible
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


    fun isHaemoglobinVisible(): Boolean {
        return page.getByText("Haemoglobin", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText(
                    "Key protein that carries oxygen; low levels suggest anemia, high levels may reflect other conditions."
                ).isVisible
    }

    fun isHematocritVisible(): Boolean {
        return page.getByText("Hematocrit").isVisible &&
                page.getByText(
                    "Percentage of blood volume made up of red blood cells; reflects overall oxygen-carrying capacity."
                ).isVisible
    }

    fun isTotalRBCVisible(): Boolean {
        return page.getByText("Total RBC (Red Blood Cell Count)").isVisible &&
                page.getByText(
                    "Measures total red cells; abnormalities may signal anemia, dehydration, or bone marrow issues."
                ).isVisible
    }

    fun isMCVVisible(): Boolean {
        return page.getByText("Mean Corpuscular Volume (MCV)").isVisible &&
                page.getByText(
                    "Average size of red blood cells; helps classify types of anemia."
                ).isVisible
    }

    fun isMCHVisible(): Boolean {
        return page.getByText("Mean Corpuscular Haemoglobin (MCH)").isVisible &&
                page.getByText(
                    "Amount of hemoglobin per red cell; low levels may indicate iron deficiency."
                ).isVisible
    }


    fun isMCHCVisible(): Boolean {
        return page.getByText("Mean Corpuscular Haemoglobin Concentration (MCHC)").isVisible &&
                page.getByText(
                    "Concentration of hemoglobin in red cells; abnormal values may reflect anemia or spherocytosis."
                ).isVisible
    }

    fun isRDWSDVisible(): Boolean {
        return page.getByText("Red Cell Distribution Width – Standard Deviation (RDW-SD)").isVisible &&
                page.getByText(
                    "Measures variation in red blood cell size; high levels suggest mixed or evolving anemia."
                ).isVisible
    }

    fun isRDWCVVisible(): Boolean {
        return page.getByText("Red Cell Distribution Width – Coefficient of Variation (RDW-CV)").isVisible &&
                page.getByText(
                    "Another measure of size variation in red cells; helps differentiate anemia causes."
                ).isVisible
    }

    fun isPlateletCountVisible(): Boolean {
        return page.getByText("Platelet Count").isVisible &&
                page.getByText(
                    "Number of platelets; low levels may increase bleeding risk, high levels may increase clotting risk."
                ).isVisible
    }

    fun isMPVVisible(): Boolean {
        return page.getByText("Mean Platelet Volume (MPV)").isVisible &&
                page.getByText(
                    "Average size of platelets; larger platelets may be more reactive."
                ).isVisible
    }

    fun isPDWVisible(): Boolean {
        return page.getByText("Platelet Distribution Width (PDW)").isVisible &&
                page.getByText(
                    "Reflects variation in platelet size, linked to clotting disorders."
                ).isVisible
    }

    fun isPLCRVisible(): Boolean {
        return page.getByText("Platelet To Large Cell Ratio (P-LCR)").isVisible &&
                page.getByText(
                    "Indicates proportion of larger platelets; helps assess clotting potential."
                ).isVisible
    }

    fun isPCTVisible(): Boolean {
        return page.getByText("Plateletcrit (PCT)").isVisible &&
                page.getByText(
                    "Overall volume percentage of platelets in blood; assesses platelet mass."
                ).isVisible
    }

    fun isNRBCVisible(): Boolean {
        return page.getByText("Nucleated Red Blood Cells (NRBC)").isVisible &&
                page.getByText(
                    "Immature red blood cells; presence in circulation may indicate bone marrow stress or severe disease."
                ).isVisible
    }

    fun isNRBCPercentageVisible(): Boolean {
        return page.getByText("Nucleated Red Blood Cells % (NRBC%)").isVisible &&
                page.getByText(
                    "Percentage of immature red blood cells relative to total count."
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
            "This panel is recommended for anyone monitoring anemia, clotting issues, or unexplained fatigue."
        ).isVisible
    }


    fun isEnergyAndOxygenTransportVisible(): Boolean {
        return page.getByText("Energy & Oxygen Transport").isVisible &&
                page.getByText("Chronic fatigue or weakness").isVisible &&
                page.getByText("Shortness of breath on exertion").isVisible &&
                page.getByText("Suspected anemia").isVisible
    }

    fun isClottingAndCirculationVisible(): Boolean {
        return page.getByText("Clotting & Circulation").isVisible &&
                page.getByText("Easy bruising or frequent nosebleeds").isVisible &&
                page.getByText("Family history of clotting disorders").isVisible &&
                page.getByText("Concern about excessive clotting risk").isVisible
    }

    fun isPreventiveHealthCBCVisible(): Boolean {
        return page.getByText("Preventive Health").isVisible &&
                page.getByText("Family history of blood disorders").isVisible &&
                page.getByText("Routine health monitoring").isVisible &&
                page.getByText("Wanting a complete vitality check").isVisible
    }



    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs analyze red cell and platelet health to assess oxygen delivery, clotting balance, and overall vitality. Results are physician-reviewed and delivered in your dashboard within 72 hours, along with actionable insights to strengthen long-term blood health."
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
                page.getByText("See how your blood markers connect to energy, oxygen transport, and clotting function.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Review your results and create a personalised action plan for optimal blood health.").isVisible
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
