package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import utils.report.StepHelper
import website.page.WebSiteBasePage


class StressAndCortisolDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.STRESS_CORTISOL_DETAIL

    private val header =
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Stress and Cortisol Rhythm Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): StressAndCortisolDetailPage {
        StepHelper.step(StepHelper.WAIT_WEBSITE_PAGE_LOAD + "Stress and Cortisol Rhythm Panel")
        header.waitFor()
        logger.info { "Stress and Cortisol Rhythm Panel loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText(
            "A non-invasive saliva test that maps your daily cortisol rhythm, revealing stress reactivity, recovery, and potential HPA axis imbalance."
        ).isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "Cortisol is your body’s main stress hormone, released by the adrenal glands in response to daily challenges. This test measures cortisol at five points across the day to assess your Cortisol Awakening Response (CAR) and overall stress rhythm. Patterns such as exaggerated morning peaks, sustained midday elevation, or evening rebounds may indicate chronic stress, poor sleep recovery, or circadian disruption. With this knowledge, you can take targeted steps to manage stress, improve sleep, and protect long-term metabolic and mental health."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Saliva Sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("At-Home Collection Kit").isVisible &&
                page.getByText("Results in 3–5 Days").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included").isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("7,499").isVisible
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

    fun isCortisolAwakeningResponse_CAR_Visible(): Boolean {
        return page.getByText("Cortisol Awakening Response (CAR): Rise in cortisol within two hours of waking, reflecting stress reactivity.").isVisible
    }

    fun isMidMorningDeclineVisible(): Boolean {
        return page.getByText("Mid-Morning Decline: Whether cortisol falls as expected after the morning peak.").isVisible
    }

    fun isAfternoonRecoveryVisible(): Boolean {
        return page.getByText("Afternoon Recovery: Checks how quickly cortisol stabilizes later in the day.").isVisible
    }

    fun isEveningRhythmVisible(): Boolean {
        return page.getByText("Evening Rhythm: Identifies abnormal rebounds or high cortisol at night linked to sleep issues.").isVisible
    }

    fun isOverallCurveVisible(): Boolean {
        return page.getByText("Overall Curve: Classifies common patterns such as hyper-reactive, rebound, chronic stress, or burnout.").isVisible
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
            "This panel is especially helpful if you:"
        ).isVisible
    }


    fun isStressAndEnergyIssuesVisible(): Boolean {
        return page.getByText("⚡ Stress & Energy Issues").isVisible &&
                page.getByText("Feel “tired but wired” at night").isVisible &&
                page.getByText("Struggle with sleep onset or poor-quality rest").isVisible &&
                page.getByText("Experience constant fatigue despite adequate sleep").isVisible

    }

    fun isMoodAndMentalHealthVisible(): Boolean {
        return page.getByText("\uD83E\uDDE0 Mood & Mental Health").isVisible &&
                page.getByText("Have anxiety, irritability, or low resilience under stress").isVisible &&
                page.getByText("Experience food cravings, especially for sugar or carbs").isVisible &&
                page.getByText("Notice mood swings or brain fog linked to stress").isVisible

    }

    fun isPreventiveHealthVisible(): Boolean {
        return page.getByText("\uD83D\uDEE1\uFE0F Preventive Health").isVisible &&
                page.getByText("Work in high-stress environments").isVisible &&
                page.getByText("Suspect chronic stress affecting immunity, metabolism, or hormones").isVisible &&
                page.getByText("Want to assess HPA axis health as part of longevity planning").isVisible

    }




    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With five simple saliva collections taken across the day, our certified labs map your cortisol rhythm. Results are physician-reviewed and delivered in a clear, easy-to-understand report within 3–5 days. You’ll see where your stress curve deviates from the healthy pattern and get practical recommendations for sleep, nutrition, stress management, and lifestyle adjustments. A 1-on-1 consult ensures you have an actionable plan to restore balance."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection:").isVisible &&
                page.getByText(
                    "Collect saliva samples at waking, 2 hrs, midday, afternoon, and evening using the provided kit."
                ).isVisible
    }

    fun isSendForProcessingVisible(): Boolean {
        return page.getByText("Send for Processing:").isVisible &&
                page.getByText(
                  "Ship your samples with the prepaid label to our certified lab."
                ).isVisible
    }

    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Access a full cortisol curve with interpretation in 3–5 days.\n").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText("Understand how stress reactivity and recovery impact your health.\n").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Translate results into daily strategies to improve stress resilience.\n").isVisible
    }


    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it works")).isVisible
    }

    fun isStep1ContentVisible(): Boolean {
        return page.getByText("01").nth(1).isVisible &&
                page.getByText("Cortisol Test Kit is Delivered").isVisible &&
                page.getByText("Your cortisol test kit arrives at your doorstep with an easy saliva collection tube.").isVisible
    }

    fun isStep2ContentVisible(): Boolean {
        return page.getByText("02").nth(1).isVisible &&
                page.getByText("At-Home Self-Test Kit").isVisible &&
                page.getByText("Collect your saliva sample as per the instructions and schedule a quick pickup from home.").isVisible
    }

    fun isStep3ContentVisible(): Boolean {
        return page.getByText("03").nth(1).isVisible &&
                page.getByText("Results in 3–5 Days").nth(1).isVisible &&
                page.getByText("Your sample is analysed in a certified lab, and results are shared on your dashboard.").isVisible
    }

    fun isStep4ContentVisible(): Boolean {
        return page.getByText("04").nth(1).isVisible &&
                page.getByText("-on-1 Expert Consultation").nth(1).isVisible &&
                page.getByText("Discuss your gut health report with our experts and get personalised guidance.").isVisible
    }

}
