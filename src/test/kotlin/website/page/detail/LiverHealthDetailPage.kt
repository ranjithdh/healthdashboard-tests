package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class LiverHealthDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.LIVER_HEALTH_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Liver Health Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): LiverHealthDetailPage {
        header.waitFor()
        logger.info { "Liver Health Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A focused blood test that evaluates liver enzymes, proteins, and bilirubin levels to assess liver function, detox capacity, and health."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Your liver plays a central role in detoxification, digestion, and metabolism. This panel measures key enzymes, proteins, and ratios that reflect liver health and efficiency. By checking for enzyme elevations, bilirubin imbalances, and protein ratios, it helps detect fatty liver, hepatitis, or early dysfunction. With these insights, you can take proactive steps to protect your liver and overall well-being."
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

    fun isSGOT_SGPT_RatioVisible(): Boolean {
        return page.getByText("SGOT / SGPT Ratio").isVisible &&
                page.getByText(
                    "Used to evaluate liver cell damage and differentiate between liver and non-liver causes of elevated enzymes."
                ).isVisible
    }

    fun isFattyLiverIndexFLIVisible(): Boolean {
        return page.getByText("Fatty Liver Index (FLI)").isVisible &&
                page.getByText(
                    "A calculated score that estimates the risk of fatty liver disease."
                ).isVisible
    }

    fun isAlbuminGlobulinAGRatioVisible(): Boolean {
        return page.getByText("Albumin / Globulin (A/G) Ratio").isVisible &&
                page.getByText(
                    "Indicates balance between major blood proteins; abnormalities may reflect liver or kidney dysfunction."
                ).isVisible
    }

    fun isGammaGlutamylTransferaseGGTVisible(): Boolean {
        return page.getByText("Gamma-Glutamyl Transferase (GGT)").isVisible &&
                page.getByText(
                    "Sensitive marker of bile duct issues, alcohol use, or liver cell stress."
                ).isVisible
    }

    fun isAlkalinePhosphataseALPVisible(): Boolean {
        return page.getByText("Alkaline Phosphatase (ALP)").isVisible &&
                page.getByText(
                    "Elevated levels may signal bile duct obstruction or liver disease."
                ).isVisible
    }

    fun isAlanineTransaminaseSGPTVisible(): Boolean {
        return page.getByText("Alanine Transaminase (SGPT)").isVisible &&
                page.getByText(
                    "Key enzyme reflecting liver inflammation or injury."
                ).isVisible
    }

    fun isAspartateAminotransferaseSGOTVisible(): Boolean {
        return page.getByText("Aspartate Aminotransferase (SGOT)").isVisible &&
                page.getByText(
                    "Enzyme elevated in liver, muscle, or cardiac stress; often paired with SGPT for accuracy."
                ).isVisible
    }

    fun isSerumGlobulinVisible(): Boolean {
        return page.getByText("Serum Globulin").isVisible &&
                page.getByText(
                    "Reflects immune proteins; abnormal levels may indicate chronic liver disease."
                ).isVisible
    }

    fun isSerumAlbuminVisible(): Boolean {
        return page.getByText("Serum Albumin").isVisible &&
                page.getByText(
                    "Major liver protein; low levels may point to chronic liver dysfunction."
                ).isVisible
    }

    fun isTotalBilirubinVisible(): Boolean {
        return page.getByText("Total Bilirubin").isVisible &&
                page.getByText(
                    "Indicates overall bilirubin levels; high values may reflect liver dysfunction or bile flow issues."
                ).isVisible
    }

    fun isBilirubinDirectVisible(): Boolean {
        return page.getByText("Bilirubin – Direct").isVisible &&
                page.getByText(
                    "Measures conjugated bilirubin; elevated in bile duct blockages or liver disease."
                ).isVisible
    }

    fun isBilirubinIndirectVisible(): Boolean {
        return page.getByText("Bilirubin – Indirect").isVisible &&
                page.getByText(
                    "Measures unconjugated bilirubin; elevated in hemolysis or impaired bilirubin processing."
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
            "This panel is recommended for anyone monitoring liver health due to lifestyle, family history, or symptoms."
        ).isVisible
    }


    fun isLifestyleRisksVisible(): Boolean {
        return page.getByText("Lifestyle Risks").isVisible &&
                page.getByText("Regular alcohol consumption").isVisible &&
                page.getByText("High-fat or processed diet").isVisible &&
                page.getByText("Long-term medication use").isVisible
    }

    fun isPreventiveHealthVisible(): Boolean {
        return page.getByText("Preventive Health").isVisible &&
                page.getByText("Family history of liver disease").isVisible &&
                page.getByText("Concern about fatty liver or hepatitis").isVisible &&
                page.getByText("Routine health and wellness screening").isVisible
    }

    fun isSymptomsAndWarningSignsVisible(): Boolean {
        return page.getByText("Symptoms & Warning Signs").isVisible &&
                page.getByText("Persistent fatigue or weakness").isVisible &&
                page.getByText("Jaundice (yellowing of skin or eyes)").isVisible &&
                page.getByText("Abdominal discomfort or swelling").isVisible
    }



    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs measure liver enzymes, proteins, and bilirubin levels. Results are physician-reviewed and delivered in your dashboard within 72 hours, along with a personalized consult to help you take action for liver health."
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
                page.getByText("See how enzyme, protein, and bilirubin levels reflect your liver health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Receive tailored guidance on lifestyle, nutrition, and preventive care.").isVisible
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
