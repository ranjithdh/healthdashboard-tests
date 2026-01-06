package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger


class LandingPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.WEBSITE_BASE_URL

    private val addOnTestPageType = AddOnTestPageType.LANDING
    val addOnTestCards = AddOnTestCards(page, addOnTestPageType)

    val faqSection = FaqSection(page)
    val everyThingYouNeedToKnowCard = EveryThingYouNeedToKnowCard(page, EveryThingYouNeedToKnowPageType.LANDING)
    val stopGuessingStartWithClaritySection = StopGuessingStartWithClaritySection(page,StopGuessingPageType.LANDING)

    private val heroSectionBookNow = page.locator("#join-now-btn-hero")
    private val whatWeTest =
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What we test").setExact(true))
    private val learnMore = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Learn more"))
    private val whatsIncludedSectionBookNow = page.locator("#join-now-btn-membership")
    private val readOurWhy = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Read our Why"))
    private val stopeGuessingBookNow = page.locator("#join-now-btn-foot-hero")

    fun waitForPageLoad(): LandingPage {
        page.locator("a#join-now-btn-hero").waitFor()
        logger.info { "Landing page loaded" }
        return this
    }

    fun isHeroHeadingVisible(): Boolean {
        return page.getByText(
            "Your health deserves a clear starting point."
        ).isVisible
    }

    fun isHeaderDescriptionVisible(): Boolean {
        return page.getByText(
            "Advanced blood test. Personalised insights. 1-on-1 Expert guidance.\n" +
                    "A clear action plan. All for ₹9,999."
        ).isVisible
    }

    fun isHeroBookNowVisible(): Boolean {
        return heroSectionBookNow.isVisible
    }

    fun clickHeroBookNow() {
        logger.info { "Clicking Hero Book Now button" }
        heroSectionBookNow.click()
    }

    fun isAtHomeTestVisible(): Boolean {
        return page.locator(".clock").isVisible &&
                page.getByText("At-Home Testing").isVisible &&
                page.getByText("Professional testing, done at").isVisible
    }

    fun isFastResultVisible(): Boolean {
        return page.locator(".clipboard-tick").isVisible &&
                page.getByText("Fast results").isVisible &&
                page.getByText("Get your results in 48 hours").isVisible
    }

    fun isSimpleAndConvenientVisible(): Boolean {
        return page.locator(".open-select-hand-gesture").isVisible &&
                page.getByText("Simple and convenient").isVisible &&
                page.getByText("Everything you need, hassle-").isVisible
    }

    fun isIntroducingTitleVisible(): Boolean {
        return page.getByText("INTRODUCING").isVisible
    }

    fun isBaselineVisible(): Boolean {
        val bassLine = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("B a s e l i n e"))
        bassLine.waitFor()
        return bassLine.isVisible
    }

    fun isBaseLineDescriptionVisible(): Boolean {
        return page.getByText("Tiredness, brain fog, poor sleep, and mood swings aren't random. They're signals. We test 100+ biomarkers from home to help you understand where you stand so that you can focus on what to do next.").isVisible
    }

    fun isWhatWeTestButtonVisible(): Boolean {
        return whatWeTest.isVisible
    }

    fun clickWhatWeTestButton(): WhatWeTestPage {
        whatWeTest.click()
        val whatWeTestPage = WhatWeTestPage(page)
        return whatWeTestPage
    }

    fun isCoverImageVisible(): Boolean {
        return page.locator(".dashboard-cover-l").isVisible
    }

    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByText("H", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("o", Page.GetByTextOptions().setExact(true)).nth(4).isVisible &&
                page.getByText("w", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
                page.getByText("i", Page.GetByTextOptions().setExact(true)).nth(5).isVisible &&
                page.locator(".heading_h2 > span:nth-child(2) > span:nth-child(2)").isVisible &&
                page.getByText("w", Page.GetByTextOptions().setExact(true)).nth(2).isVisible &&
                page.getByText("o", Page.GetByTextOptions().setExact(true)).nth(5).isVisible &&
                page.getByText("r", Page.GetByTextOptions().setExact(true)).nth(5).isVisible &&
                page.getByText("k", Page.GetByTextOptions().setExact(true)).nth(1).isVisible &&
                page.getByText("s", Page.GetByTextOptions().setExact(true)).nth(4).isVisible
    }

    fun isHowItWorksDescriptionVisible(): Boolean {
        return page.getByText("Baseline delivers the most advanced approach to preventive care, translating deep diagnostics into personalised insights and pairing them with expert-led support built around you.").isVisible
    }

    fun isLearnMoreButtonVisible(): Boolean {
        return learnMore.isVisible
    }

    fun clickLearnMoreButton(): HowItWorksPage {
        learnMore.click()
        val howItWorksPage = HowItWorksPage(page)
        return howItWorksPage
    }

    fun isStep1Visible(): Boolean {
        return page.getByText("Step 1").isVisible &&
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Advanced Blood Panel")).isVisible &&
                page.getByText("This isn’t just a blood test. It’s the most complete health evaluation, with advanced diagnostics to identify risk earlier and interpret data across systems.").isVisible
    }

    fun isStep2Visible(): Boolean {
        return page.getByText("Step 2").isVisible &&
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("1-on-1 Expert Consult")).isVisible &&
                page.getByText("We don’t just hand you a report. We turn complex results into clear insights, guided by a 1:1 consult with our Longevity Expert to help you prioritise what matters most.").isVisible
    }

    fun isStep3Visible(): Boolean {
        return page.getByText("Step 3").isVisible &&
                page.getByRole(
                    AriaRole.HEADING,
                    Page.GetByRoleOptions().setName("Personalised Action Plan").setExact(true)
                ).isVisible &&
                page.getByText("Turn your insights into action with expert guidance on supplements, exercise, nutrition, sleep, stress and follow-up testing, all tailored to helping you improve your health.").isVisible
    }

    fun isWhatIsIncludedHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("What's included in Baseline")
        ).isVisible
    }

    fun isWhatIsIncludedDescriptionVisible(): Boolean {
        return page.getByText("Baseline is more than a blood test. Access an ecosystem of diagnostics, expert-led guidance, and wellness solutions personalised to you.").isVisible
    }

    fun isWhatIncludedSectionBookNowButtonVisible(): Boolean {
        return whatsIncludedSectionBookNow.isVisible
    }

    fun clickWhatIsIncludedSectionBookNowButton() {
        whatsIncludedSectionBookNow.click()
    }


    fun isAllDateInOnePlaceSectionVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("All your data in one place")
        ).isVisible &&
                page.getByText("Connect the dots across your gene, gut, blood, symptoms and more through a powerful dashboard.").isVisible
    }

    fun isExpertGuidanceSectionVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Expert guidance for every test")
        ).isVisible &&
                page.getByText("Get expert guidance to understand and act on every test, with your action plan updated continuously.").isVisible
    }

    fun hyperPersonalizesSectionVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Hyper-personalised action plan")
        ).isVisible &&
                page.getByText("Lifestyle, supplements and follow-up tests tailored to your health that evolves continuously.").isVisible

    }


    fun addOnTestingSectionVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Add-on testing for deep")).isVisible &&
                page.getByText("Unlock deeper layers with advanced diagnostics, delivering holistic insights on your health.").isVisible
    }

    fun referAndEarnSectionVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Refer and earn DH points")
        ).isVisible &&
                page.getByText("Unlock welcome points, and redeem them across the platform for exclusive member discounts.").isVisible
    }

    fun isBuiltByExpertHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "Built by experts,\n" +
                        "backed by science"
            )
        ).isVisible
    }

    fun isBuiltByExpertDescriptionVisible(): Boolean {
        return page.getByText("Developed with top doctors and specialists in their fields to ensure every insight is accurate, actionable, and trusted.").isVisible
    }


    fun isDrVishalUsRaoSectionElementsVisible(): Boolean {
        return page.locator(".image-cover-5").first().isVisible &&
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Dr. Vishal US Rao")).isVisible &&
                page.getByText("PRINCIPLE SCIENTIFIC ADVISOR").isVisible &&
                page.getByText("MS, FRCS (Glasgow) FACS (USA) and Fellow of Royal Society of Medicine").isVisible &&
                page.locator(".team-socials.hide-mobile-portrait > .team-social-link-2").first().isVisible
    }

    fun isDrWasimMohideenElementsVisible(): Boolean {
        return page.locator("div:nth-child(2) > .team-avatar-large-2 > .image-cover-5").isVisible &&
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Dr. Wasim Mohideen")).isVisible &&
                page.getByText("CHIEF WELLNESS OFFICER").isVisible &&
                page.getByText("MBBS, MRCGP [INT], DFM (RCGP, UK), Dip IBLM, AFMCP (IFM)").isVisible &&
                page.locator("div:nth-child(2) > .team-column > .team-details > .team-socials.hide-mobile-portrait > .team-social-link-2").isVisible
    }

    fun isWordFromOurFounderHeadingVisible(): Boolean {
        val wordFromOurFounder =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Word from our founder"))
        wordFromOurFounder.scrollIntoViewIfNeeded()
        return wordFromOurFounder.isVisible
    }


    fun isWhyWeBuiltTextVisible(): Boolean {
        return page.getByText("That’s why we built Deep Holistics.").isVisible &&
                page.getByText("\"Most people don’t want six-pack abs. ").isVisible &&
                page.getByText("They just want to wake up with energy.").isVisible &&
                page.getByText("Focus without three cups of coffee.").isVisible &&
                page.getByText("Eat without bloating.").isVisible &&
                page.getByText("Feel sharp, light, and strong every day. ").isVisible &&
                page.getByText("We help people look, feel, and perform at their best. Not someday, but every single day. No jargon. No fluff. Just answers that make sense and actions that actually work\"").isVisible

    }

    fun isCeoNameVisible(): Boolean {
        return page.getByText("H R Sanath Kumar").isVisible && page.getByText("Co-Founder & CEO").isVisible
    }

    fun isWordFromOurFounderSectionReadOurWhyButtonVisible(): Boolean {
        return readOurWhy.isVisible
    }

    fun clickWordFromOurFounderSectionReadOurWhyButtonVisible(): OurWhyPage {
        readOurWhy.click()
        val ourWhyPage = OurWhyPage(page)
        ourWhyPage.waitForPageLoad()
        return ourWhyPage
    }


    fun isFaqHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "Got any common question in mind?\n" +
                        "Read our FAQs down below"
            )
        ).isVisible
    }

//    fun stopGuessingSectionElementsVisible(): Boolean {
//        val header = page.getByRole(
//            AriaRole.HEADING,
//            Page.GetByRoleOptions().setName("S t o p g u e s s i n g . S t a r t w i t h c l a r i t y .")
//        )
//        val description =
//            page.getByText("It’s time to reclaim control and address what’s holding you back so you can look, feel and perform 10/10, day after day")
//        header.waitFor()
//
//        return header.isVisible && description.isVisible
//    }
//
//    fun stopGuessingBookNowButtonVisible(): Boolean {
//        return stopeGuessingBookNow.isVisible
//    }
//
//    fun clickStopGuessingBookNowButtonVisible() {
//        stopeGuessingBookNow.click()
//    }

}
