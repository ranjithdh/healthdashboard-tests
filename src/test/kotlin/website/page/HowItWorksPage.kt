package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig


class HowItWorksPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.HOW_IT_WORKS

    val everyThingYouNeedToKnowCard = EveryThingYouNeedToKnowCard(page, EveryThingYouNeedToKnowPageType.HOW_IT_WORKS)
    val stopGuessingStartWithClaritySection = StopGuessingStartWithClaritySection(page,StopGuessingPageType.HOW_IT_WORKS)

    private val step1Title = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("B a s e l i n e s t a r t s w i t h y o u r h e a l t h a s s e s s m e n t")
    )

    private val step2Title = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("Y o u r r e s u l t s a r e a n a l y s e d a n d e x p l a i n e d")
    )

    private val step3Title = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("E x p e r t g u i d a n c e t o t u r n i n s i g h t s i n t o b e t t e r h e a l t h")
    )

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("F r o m T e s t t o C l a r i t y ."))

    fun waitForPageLoad(): HowItWorksPage {
        header.waitFor()
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isFromTestToClarifyDescriptionVisible(): Boolean {
        return page.getByText("Start with a comprehensive health assessment, then let our experts guide you with personalised insights to uncover risks early and optimise your health journey.").isVisible
    }

    // ************* ---------------------------------- STEP 1 ------------------------ ******************** //

    fun isStep1Visible(): Boolean {
        return page.getByText("Step 1").isVisible
    }

    fun waitForStep1Title() {
        step1Title.scrollIntoViewIfNeeded()
        step1Title.waitFor()
    }

    fun isStep1TitleVisible(): Boolean {
        return step1Title.isVisible
    }

    fun isStep1DescriptionVisible(): Boolean {
        return page.getByText("This isn’t just a blood test. It’s the most complete health evaluation, with advanced diagnostics to identify risk earlier and interpret data across systems.").isVisible
    }

    fun isStep1Point1TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Get started with an at-home blood draw")
        ).isVisible
    }

    fun isStep1Point1DescriptionVisible(): Boolean {
        return page.getByText("Begin your journey with convenient testing at home. From scheduling to blood draw, enjoy a seamless experience from start to finish").isVisible
    }

    fun isStep1Point2TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Curated 100+ advanced diagnostics")
        ).isVisible
    }

    fun isStep1Point2DescriptionVisible(): Boolean {
        return page.getByText(
            "Curated by our experts to uncover a connected picture of your health. Every test is designed through a preventive health lens."
        ).isVisible
    }

    fun isStep1Point3TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Your very own Baseline Dashboard")
        ).isVisible
    }

    fun isStep1Point3DescriptionVisible(): Boolean {
        return page.getByText(
            "Tracking trends and highlighting status made effortless, delivered in a format that makes sense to you."
        ).isVisible
    }


    // ************* ---------------------------------- STEP 2 ------------------------ ******************** //


    fun isStep2Visible(): Boolean {
        return page.getByText("Step 2").isVisible
    }

    fun waitForStep2Title() {
        step2Title.waitFor()
    }

    fun isStep2TitleVisible(): Boolean {
        step2Title.scrollIntoViewIfNeeded()
        return step2Title.isVisible
    }

    fun isStep2DescriptionVisible(): Boolean {
        return page.getByText(
            "We don’t just hand you a report. We turn complex results into clear insights, guided by a 1:1 consult with our Longevity Expert to help you prioritise what matters most."
        ).isVisible
    }

    fun isStep2Point1TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Discover what your results reveal")
        ).isVisible
    }

    fun isStep2Point1DescriptionVisible(): Boolean {
        return page.getByText(
            "Your health data is transformed into a insight map, turning complexity into clarity so you can focus on what matters most."
        ).isVisible
    }

    fun isStep2Point2TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("1,000+ data points define your unique biology")
        ).isVisible
    }

    fun isStep2Point2DescriptionVisible(): Boolean {
        return page.getByText(
            "Connect the dots across biomarkers to uncover causes, patterns, effects, and meaningful insights into your biology."
        ).isVisible
    }

    fun isStep2Point3TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Expert-led review of your results")
        ).isVisible
    }

    fun isStep2Point3DescriptionVisible(): Boolean {
        return page.getByText(
            "A guided walkthrough of your results with expert assessment of health profile, leading to clear, personalised next steps"
        ).isVisible
    }


    // ************* ---------------------------------- STEP 3 ------------------------ ******************** //


    fun isStep3Visible(): Boolean {
        return page.getByText("Step 3").isVisible
    }

    fun waitForStep3Title() {
        step3Title.waitFor()
    }

    fun isStep3TitleVisible(): Boolean {
        step3Title.scrollIntoViewIfNeeded()
        return step3Title.isVisible
    }

    fun isStep3DescriptionVisible(): Boolean {
        return page.getByText(
            "Turn your insights into action with expert guidance on supplements, exercise, nutrition, sleep, stress and follow-up testing, all tailored to helping you steadily improve your health."
        ).isVisible
    }


    fun isStep3Point1TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Personalised exercise and nutrition guidance")
        ).isVisible
    }

    fun isStep3Point1DescriptionVisible(): Boolean {
        return page.getByText(
            "Work with our nutrition and fitness experts to translate your results into personalised strategies tailored to your health profile."
        ).isVisible
    }

    fun isStep3Point2TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Additional testing for deeper insight")
        ).isVisible
    }

    fun isStep3Point2DescriptionVisible(): Boolean {
        return page.getByText(
            "Uncover deeper insights with optional diagnostics and 1:1 expert led reviews designed to identify what regular tests can't."
        ).isVisible
    }

    fun isStep3Point3TitleVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Access your concierge for every step ahead")
        ).isVisible
    }

    fun isStep3Point3DescriptionVisible(): Boolean {
        return page.getByText(
            "Year-round access to expert concierge care for personalised guidance, timely answers, and continuous support throughout the year."
        ).isVisible
    }


    private val addOnTestPageType = AddOnTestPageType.HOW_IT_WORKS
    val addOnTestCards = AddOnTestCards(page, addOnTestPageType)


}
