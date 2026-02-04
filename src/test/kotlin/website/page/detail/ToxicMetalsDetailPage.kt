package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class ToxicMetalsDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.TOXIC_METALS_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Toxic Metals Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): ToxicMetalsDetailPage {
        header.waitFor()
        logger.info { "Toxic Metals Panel detail loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "An at-home test for detecting toxic and heavy metal exposure, safeguarding long-term health and organ function."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "This panel measures 20+ toxic and heavy metals that can accumulate in the body through food, water, air, and occupational exposure. Elevated levels of these metals may impact brain function, cardiovascular health, kidney function, and overall energy. Early detection allows you to take proactive steps to minimize exposure and safeguard your long-term health."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("No Fasting Required").isVisible &&
                page.getByText("At-Home Sample Collection").first().isVisible &&
                page.getByText("Get results in 72 hrs").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("2,499").isVisible
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

    fun isArsenicVisible(): Boolean {
        return page.getByText("Arsenic").isVisible &&
                page.getByText("Found in contaminated water and some foods; linked to skin, lung, and cardiovascular risks.").isVisible
    }

    fun isCadmiumVisible(): Boolean {
        return page.getByText("Cadmium").isVisible &&
                page.getByText("Common in cigarette smoke and industrial pollution; may harm kidneys and bones").isVisible
    }

    fun isMercuryVisible(): Boolean {
        return page.getByText("Mercury", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Present in seafood and dental fillings; affects nervous system and cognitive health.\n").isVisible
    }

    fun isLeadVisible(): Boolean {
        return page.getByText("Lead", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Found in old paint, pipes, and soil; can impair brain development and blood health.").isVisible
    }

    fun isChromiumVisible(): Boolean {
        return page.getByText("Chromium").isVisible &&
                page.getByText("Industrial exposure can damage respiratory and skin health.").isVisible
    }

    fun isBariumVisible(): Boolean {
        return page.getByText("Barium").isVisible &&
                page.getByText("Exposure from certain environments; may impact cardiovascular and muscular systems.").isVisible
    }

    fun isCobaltVisible(): Boolean {
        return page.getByText("Cobalt").isVisible &&
                page.getByText("Excess levels may affect thyroid and heart.").isVisible
    }

    fun isCaesiumVisible(): Boolean {
        return page.getByText("Caesium").isVisible &&
                page.getByText("Rare but environmental exposure may influence muscle and heart function.").isVisible
    }

    fun isThalliumVisible(): Boolean {
        return page.getByText("Thallium").isVisible &&
                page.getByText("Toxic even in small amounts; affects nerves and digestive system.").isVisible
    }

    fun isUraniumVisible(): Boolean {
        return page.getByText("Uranium").isVisible &&
                page.getByText("Environmental exposure may impair kidney function.").isVisible
    }

    fun isStrontiumVisible(): Boolean {
        return page.getByText("Strontium").isVisible &&
                page.getByText("High levels can affect bone development.").isVisible
    }

    fun isAntimonyVisible(): Boolean {
        return page.getByText("Antimony").isVisible &&
                page.getByText("Industrial exposure linked to respiratory and skin irritation.").isVisible
    }

    fun isTinVisible(): Boolean {
        return page.getByText("Tin", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Certain compounds may affect stomach and nervous system.").isVisible
    }

    fun isMolybdenumVisible(): Boolean {
        return page.getByText("Molybdenum").isVisible &&
                page.getByText("Excess intake may disrupt metabolism.").isVisible
    }

    fun isSilverVisible(): Boolean {
        return page.getByText("Silver").isVisible &&
                page.getByText("Overexposure can cause skin discoloration and organ effects.").isVisible
    }

    fun isVanadiumVisible(): Boolean {
        return page.getByText("Vanadium").isVisible &&
                page.getByText("High levels linked to lung and cardiovascular effects.").isVisible
    }

    fun isBerylliumVisible(): Boolean {
        return page.getByText("Vanadium").isVisible &&
                page.getByText("Industrial metal causing severe lung disease on exposure.").isVisible
    }

    fun isBismuthVisible(): Boolean {
        return page.getByText("Bismuth").isVisible &&
                page.getByText("High intake may lead to neurological symptoms.").isVisible
    }

    fun isSeleniumVisible(): Boolean {
        return page.getByText("Selenium").isVisible &&
                page.getByText("Essential in small amounts, but toxicity harms liver and hair.").isVisible
    }

    fun isAluminiumVisible(): Boolean {
        return page.getByText("Aluminium").isVisible &&
                page.getByText("Common exposure linked to neurological and bone effects.").isVisible
    }

    fun isNickelVisible(): Boolean {
        return page.getByText("Nickel").isVisible &&
                page.getByText("Can trigger allergic reactions and respiratory issues.").isVisible
    }

    fun isManganeseVisible(): Boolean {
        return page.getByText("Manganese").isVisible &&
                page.getByText("Necessary nutrient, but excess exposure affects brain and movement control.").isVisible
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
            "Understanding your metal exposure can help identify hidden risks to long-term health. This panel is particularly valuable if you live in high-pollution areas, work in industries with chemical exposure, or experience unexplained chronic symptoms."
        ).isVisible
    }


    fun isEnvironmentalExposureRiskVisible(): Boolean {
        return page.getByText("Environmental Exposure", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Living in areas with industrial pollution").isVisible &&
                page.getByText("Using contaminated water").isVisible &&
                page.getByText("Long-term seafood consumption (mercury risk)").isVisible

    }

    fun isOccupationalRiskVisible(): Boolean {
        return page.getByText("Occupational Risk").isVisible &&
                page.getByText("Working in construction, mining, welding, or factories").isVisible &&
                page.getByText("Handling chemicals, paints, or batteries").isVisible &&
                page.getByText("Regular exposure to smoke or fumes").isVisible

    }

    fun isUnexplainedSymptomsVisible(): Boolean {
        return page.getByText("Unexplained Symptoms").isVisible &&
                page.getByText("Persistent fatigue or weakness").isVisible &&
                page.getByText("Neurological symptoms like memory issues or tremors").isVisible &&
                page.getByText("Chronic digestive or kidney problems").isVisible

    }

    fun isFamilyAndLifestyleFactorsVisible(): Boolean {
        return page.getByText("Family & Lifestyle Factors").isVisible &&
                page.getByText("Children in older homes with lead paint/pipes").isVisible &&
                page.getByText("Family history of heavy metal toxicity").isVisible &&
                page.getByText("Concern about cumulative daily exposure").isVisible

    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun whatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs measure toxic metal levels to assess hidden environmental and occupational exposure. Results are physician-reviewed and delivered within days, helping you take proactive steps to reduce exposure and protect your long-term health."
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
                page.getByText("See how your toxic metal levels connect to symptoms and potential exposure sources.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert:").isVisible &&
                page.getByText("Get your questions answered and receive an action plan to lower risk and support detox pathways.").isVisible
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
