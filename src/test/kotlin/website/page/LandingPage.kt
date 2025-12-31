package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger


class LandingPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.MARKETING_BASE_URL

    private val allergy = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Allergies\n" + "Checks for allergic responses and sensitivities"
        )
    )

    private val gut = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Gut Microbiome\n" + "Profiles gut microbes to reveal imbalances"
        )
    )

    private val stressAndCortisol = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Stress and Cortisol\n" + "Tracks stress hormones throughout the day"
        )
    )

    private val gene = page.getByRole(
        AriaRole.LINK, Page.GetByRoleOptions().setName(
            "Genetic Analysis\n" + "Comprehensive screening for genetic traits and risks"
        )
    )

    private val omega = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Omega Profile\n" + "Examines fatty acid profiles and ratios"
        )
    )

    private val toxicMetals = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Toxic Metals\n" +
                    "Detects heavy metal exposure in the bloodstream"
        )
    )

    private val thyroidHealth = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Thyroid Health\n" +
                    "Evaluates thyroid hormones and related issues"
        )
    )

    private val womensHealth = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Women’s Health\n" +
                    "Assesses key factors for women’s wellbeing"
        )
    )

    private val essentialNutrients = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Essential Nutrients\n" +
                    "Measures vital nutrient, vitamin, and mineral levels"
        )
    )

    private val advancedThyroid = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Advanced Thyroid\n" +
                    "Detects autoimmune thyroid conditions"
        )
    )

    private val liverHealth = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Liver Health\n" +
                    "Gauges liver enzymes and performance"
        )
    )

    private val autoImmune = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "AutoImmune\n" +
                    "Identifies immune system disorder"
        )
    )

    private val advanceHeartHealth = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Advanced Heart Health\n" +
                    "Analyses indicators for heart health"
        )
    )

    private val womensFertility = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Women's Fertility\n" +
                    "Assess fertility readiness and reproductive health"
        )
    )

    private val bloodHealth = page.getByRole(
        AriaRole.LINK,
        Page.GetByRoleOptions().setName(
            "Blood Health\n" +
                    "Evaluates blood cell health and overall vitality"
        )
    )

    // Delegate FAQ functionality to FaqSection
    val faqSection = FaqSection(page)


    fun waitForPageLoad(): LandingPage {
        element("a#join-now-btn-hero").waitFor()
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
        return page.locator("#join-now-btn-hero").isVisible
    }

    fun clickHeroBookNow() {
        logger.info { "Clicking Hero Book Now button" }
        element("a#join-now-btn-hero").click()
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
        return page.getByText("Baseline").isVisible
    }

    fun isBaseLineDescriptionVisible(): Boolean {
        return page.getByText("Tiredness, brain fog, poor sleep, and mood swings aren't random. They're signals. We test 100+ biomarkers from home to help you understand where you stand so that you can focus on what to do next.").isVisible
    }

    fun isWhatWeTestButtonVisible(): Boolean {
        return page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What we test").setExact(true)).isVisible
    }

    fun clickWhatWeTestButton(): WhatWeTestPage {
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What we test").setExact(true)).click()
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
        return page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Learn more")).isVisible
    }

    fun clickLearnMoreButton(): HowItWorksPage {
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Learn more")).click()
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


    fun waitForForAddOnTestHeader() {
        page.getByText("O", Page.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("p", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.locator(".heading_h2-mt > span > span:nth-child(3)").first().waitFor()
        page.locator(".heading_h2-mt > span > span:nth-child(4)").first().waitFor()
        page.locator(".heading_h2-mt > span > span:nth-child(5)").first().waitFor()
        page.locator(".heading_h2-mt > span > span:nth-child(6)").first().waitFor()
        page.locator(".heading_h2-mt > span > span:nth-child(7)").first().waitFor()
        page.locator(".heading_h2-mt > span > span:nth-child(8)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(2) > span").first().waitFor()
        page.getByText("d", Page.GetByTextOptions().setExact(true)).nth(1).waitFor()
        page.getByText("d", Page.GetByTextOptions().setExact(true)).nth(2).waitFor()
        page.getByText("-", Page.GetByTextOptions().setExact(true)).waitFor()
        page.locator(".heading_h2-mt > span:nth-child(2) > span:nth-child(5)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(2) > span:nth-child(6)").waitFor()
        page.getByText("s", Page.GetByTextOptions().setExact(true)).nth(5).waitFor()
        page.getByText("f", Page.GetByTextOptions().setExact(true)).waitFor()
        page.locator(".heading_h2-mt > span:nth-child(3) > span:nth-child(2)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(3) > span:nth-child(3)").waitFor()
        page.getByText("d", Page.GetByTextOptions().setExact(true)).nth(3).waitFor()
        page.locator(".heading_h2-mt > span:nth-child(4) > span:nth-child(2)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(4) > span:nth-child(3)").waitFor()
        page.getByText("p", Page.GetByTextOptions().setExact(true)).nth(2).waitFor()
        page.locator(".heading_h2-mt > span:nth-child(4) > span:nth-child(5)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(4) > span:nth-child(6)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(5) > span").first().waitFor()
        page.locator(".heading_h2-mt > span:nth-child(5) > span:nth-child(2)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(5) > span:nth-child(3)").waitFor()
        page.locator(".heading_h2-mt > span:nth-child(5) > span:nth-child(4)").waitFor()
        page.getByText("g", Page.GetByTextOptions().setExact(true)).nth(2).waitFor()
        page.getByText("h", Page.GetByTextOptions().setExact(true)).nth(4).waitFor()
        page.locator(".heading_h2-mt > span:nth-child(5) > span:nth-child(7)").waitFor()
    }

    fun isAddOnTestHeadingVisible(): Boolean {

        waitForForAddOnTestHeader()

        return page.getByText(
            "Optional add-ons for deeper\ninsight"
        ).isVisible

    }


    fun isAddOnTestDescriptionVisible(): Boolean {
        return page.getByText("Your Baseline helps you know where you stand. That’s why we’ve built a holistic platform of advanced diagnostics to measure and improve every aspect of your health journey.").isVisible
    }


    fun isAllergyTestVisible(): Boolean {
        return allergy.isVisible
    }

    fun isGutTestVisible(): Boolean {
        return gut.isVisible
    }

    fun isStressAndCortisolVisible(): Boolean {
        return stressAndCortisol.isVisible
    }

    fun isGeneVisible(): Boolean {
        return gene.isVisible
    }

    fun isOmegaTestVisible(): Boolean {
        return omega.isVisible
    }

    fun isToxicTestVisible(): Boolean {
        return toxicMetals.isVisible
    }

    fun isThyroidHealthVisible(): Boolean {
        return thyroidHealth.isVisible
    }

    fun isWomenHealthVisible(): Boolean {
        return womensHealth.isVisible
    }

    fun isEssentialNutrientsVisible(): Boolean {
        return essentialNutrients.isVisible
    }

    fun isAdvancedThyroidVisible(): Boolean {
        return advancedThyroid.isVisible
    }

    fun isLiverHealthVisible(): Boolean {
        return liverHealth.isVisible
    }

    fun isAutoImmuneVisible(): Boolean {
        return autoImmune.isVisible
    }

    fun isAdvanceHeartHealthVisible(): Boolean {
        return advanceHeartHealth.isVisible
    }

    fun isWomensFertilityVisible(): Boolean {
        return womensFertility.isVisible
    }

    fun isBloodHealthVisible(): Boolean {
        return bloodHealth.isVisible
    }

    fun isViewAllAddOnTestButtonVisible(): Boolean {
        return page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("View All Add-on Tests")).isVisible
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
        return page.locator("#join-now-btn-membership").isVisible
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


    fun isEverythingYouNeedToKnowHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Everything you need to know")
        ).isVisible
    }

    fun isEverythingYouNeedToKnowDescriptionVisible(): Boolean {
        return page.getByText("Baseline brings together a preventive health system to give you clarity and help you take action with confidence.").isVisible
    }

    fun isWhatsIncludedPointsVisible(): Boolean {
        return page.getByText("What's Included", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Advanced Biomarker at-home").isVisible &&
                page.getByText(":1 Expert Consults for all the tests").isVisible &&
                page.getByText("Longevity platform to manage").isVisible &&
                page.getByText("Add-on tests to help you go").isVisible &&
                page.getByText("DH Reward points and").isVisible &&
                page.getByText("This isn’t a check-up. It’s").isVisible &&
                page.getByText("₹9,999", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isEveryThingYouNeedToKnowBookNowVisible(): Boolean {
        return page.locator("#join-now-btn-membership-pricing").isVisible
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
        val wordFromOurFounder = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Word from our founder"))
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
        return page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Read our Why")).isVisible
    }


    //faq - faq - faq -faq -----------------------------------------------------------------------------------------

    fun isFaqHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "Got any common question in mind?\n" +
                        "Read our FAQs down below"
            )
        ).isVisible
    }


    // ---------------------- Other Methods ----------------------

    fun clickLearnMoreLink(): HowItWorksPage {
        logger.info { "Clicking Learn More link" }
        element("a#cta_how_it_works").click()
        return HowItWorksPage(page)
    }

    fun isDiagnosticCardsSectionVisible(): Boolean {
        return element("a.diagnostic_card").first().isVisible
    }

    fun getDiagnosticCardsCount(): Int {
        return element("a.diagnostic_card").count()
    }

    fun scrollToBottom() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
    }
}
