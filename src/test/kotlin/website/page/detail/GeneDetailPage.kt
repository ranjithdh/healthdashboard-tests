package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class GeneDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.GENE_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Genetic Analysis"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): GeneDetailPage {
        header.waitFor()
        logger.info { "GutMicrobiomeDetailPage loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A cutting-edge cheek swab DNA test that reveals genetic predispositions across nutrition, fitness, mental health, disease risk and more."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Your genes influence everything from metabolism and fitness to mental health and long-term disease risks. This comprehensive analysis screens hundreds of genetic markers to provide insights into nutrition, exercise performance, sleep, immunity, skin, and chronic disease predispositions. With these insights, you can take proactive steps to personalize lifestyle, nutrition, and medical decisions for optimal health and longevity."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Cheek Swab Sample").isVisible &&
                page.getByText("At-Home Collection Kit").isVisible &&
                page.getByText("Results Within 3–4 Weeks").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("14,999").isVisible
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

    fun isNutritionAndMetabolismVisible(): Boolean {
        return page.getByText("Nutrition and Metabolism").isVisible &&
                page.getByText("Vitamin absorption and metabolism: Vitamin D, B12, B6, B9, Vitamin C").isVisible &&
                page.getByText("PUFA response, antioxidant capacity").isVisible &&
                page.getByText("Obesity and BMI risk").isVisible &&
                page.getByText("Response to dietary fat, lactose intolerance").isVisible &&
                page.getByText("Caffeine and alcohol response").isVisible
    }

    fun isFitnessAndPerformanceVisible(): Boolean {
        return page.getByText("Fitness and Performance").isVisible &&
                page.getByText("Muscle performance and recovery").isVisible &&
                page.getByText("Exercise motivation").isVisible &&
                page.getByText("Bone mineral density").isVisible &&
                page.getByText("Fast vs slow-twitch muscle balance").isVisible
    }

    fun isSkinHairAndAppearanceVisible(): Boolean {
        return page.getByText("Skin, Hair, and Appearance").isVisible &&
                page.getByText("Hair texture, colour, and eyebrow thickness").isVisible &&
                page.getByText("Sunburn risk, tanning tendency, freckling").isVisible &&
                page.getByText("Male pattern baldness").isVisible &&
                page.getByText("Risk of vitiligo, psoriasis, age-related macular degeneration").isVisible
    }

    fun isSleepAndLifestyleVisible(): Boolean {
        return page.getByText("Sleep and Lifestyle").isVisible &&
                page.getByText("Sleep cycle preference, duration, and depth").isVisible &&
                page.getByText("High-altitude adaptation").isVisible &&
                page.getByText("Nicotine dependence").isVisible
    }

    fun isMentalHealthAndPersonalityVisible(): Boolean {
        return page.getByText("Mental Health and Personality").isVisible &&
                page.getByText("Genetic predispositions to depression, anxiety, ADHD, bipolar disorder").isVisible &&
                page.getByText("Cognitive traits: focus, planning, learning capacity, memory").isVisible &&
                page.getByText("Behavioural and emotional traits: empathy, resilience, social connectivity").isVisible
    }

    fun isCardioAndMetabolicHealthVisible(): Boolean {
        return page.getByText("Cardio-metabolic Health").isVisible &&
                page.getByText("Lipids: LDL, HDL, triglycerides").isVisible &&
                page.getByText("Blood markers: homocysteine, blood pressure, hypertension").isVisible &&
                page.getByText("Diabetes type 1 & 2, insulin resistance").isVisible &&
                page.getByText("Cardiovascular conditions: atrial fibrillation, coronary heart disease, stroke, long QT, sudden cardiac arrest").isVisible
    }

    fun isImmuneAndInflammatoryHealthVisible(): Boolean {
        return page.getByText("Immune & Inflammatory Health").isVisible &&
                page.getByText("Asthma, lupus, ankylosing spondylitis, rheumatoid arthritis").isVisible
    }

    fun isDigestiveHealthVisible(): Boolean {
        return page.getByText("Digestive Health").isVisible &&
                page.getByText("Celiac disease, Crohn’s disease, ulcerative colitis").isVisible &&
                page.getByText("Chronic kidney disease, liver cirrhosis").isVisible
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
            "A genetic analysis provides lifelong insights that can inform nutrition, disease prevention, and personal health strategies. This panel is ideal for those wanting to understand their unique biology and take control of their long-term health."
        ).isVisible
    }


    fun isNutritionAndLifestyleGoalsVisible(): Boolean {
        return page.getByText("Nutrition & Lifestyle Goals").isVisible &&
                page.getByText("Want to personalise diet and supplements").isVisible &&
                page.getByText("Struggling with weight or food intolerances").isVisible &&
                page.getByText("Looking to optimise exercise and recovery").isVisible

    }

    fun isPreventiveHealthAndLongevity(): Boolean {
        return page.getByText("Preventive Health & Longevity").isVisible &&
                page.getByText("Family history of chronic disease").isVisible &&
                page.getByText("Concern about cancer, heart disease, or autoimmune risk").isVisible &&
                page.getByText("Want proactive strategies for prevention").isVisible

    }

    fun isMentalHealthAndPersonalityWhoShouldTakeVisible(): Boolean {
        return page.getByText("Mental Health & Personality").isVisible &&
                page.getByText("Interested in how genes shape focus, memory, or resilience").isVisible &&
                page.getByText("Family history of psychiatric or neurological conditions").isVisible &&
                page.getByText("Want tailored support for mental health").isVisible

    }

    fun isFamilyPlanningAndRiskAwarenessVisible(): Boolean {
        return page.getByText("Family Planning & Risk Awareness").isVisible &&
                page.getByText("Curious about carrier status for inherited conditions").isVisible &&
                page.getByText("Planning pregnancy or screening for genetic disorders").isVisible &&
                page.getByText("Concerned about hereditary risks").isVisible

    }


    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple cheek swab collected at home, our certified labs analyse your DNA across hundreds of markers. Results are reviewed by genetic experts and delivered in an easy-to-navigate dashboard within 3–4 weeks, along with a consult to help you interpret findings and create a personalised action plan."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection:").isVisible &&
                page.getByText(
                    "Collect your cheek swab at home using the kit provided."
                ).isVisible
    }

    fun isSendForProcessingVisible(): Boolean {
        return page.getByText("Send for Processing:").isVisible &&
                page.getByText(
                    "Return your sample with the prepaid shipping label."
                ).isVisible
    }

    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Access your personalised genetic report within 3–4 weeks.").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText(" Explore how your genes impact nutrition, performance, disease risks, and more.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Receive guidance on lifestyle, diet, and preventive strategies tailored to your genetic profile.").isVisible
    }


    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it works")).isVisible
    }

    fun isStep1ContentVisible(): Boolean {
        return page.getByText("01").nth(1).isVisible &&
                page.getByText("Gene Kit Delivered").isVisible &&
                page.getByText("Your DNA kit arrives at your doorstep with simple cheek swab instructions.").isVisible
    }

    fun isStep2ContentVisible(): Boolean {
        return page.getByText("02").nth(1).isVisible &&
                page.getByText("At-Home Sample Collection").nth(1).isVisible &&
                page.getByText("Schedule a quick home visit — our technician collects your sample in minutes.").isVisible
    }

    fun isStep3ContentVisible(): Boolean {
        return page.getByText("03").nth(1).isVisible &&
                page.getByText("Results Within 3–4 Weeks").nth(1).isVisible &&
                page.getByText("Your sample is analysed in a certified lab, and results are shared on your dashboard.").isVisible
    }

    fun isStep4ContentVisible(): Boolean {
        return page.getByText("04").nth(1).isVisible &&
                page.getByText("1-on-1 Expert Consultation").nth(1).isVisible &&
                page.getByText("Discuss your gut health report with our experts and get personalised guidance.").isVisible
    }

}
