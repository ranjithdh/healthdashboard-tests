package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import utils.logger.logger


class LandingPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = ""

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


    private val appointmentAndProcess = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Appointments & Process"))
    private val testCoverage = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Test Coverage"))
    private val privacyAndDataSecurity = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Privacy & Data Security"))
    private val consult = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Consult"))
    private val actionPlan = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Action Plan"))
    private val points = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Points"))


    fun isGeneralTabVisible(): Boolean {
        return page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("General")).isVisible
    }

    fun isAppointmentAndProcessVisible(): Boolean {
        return appointmentAndProcess.isVisible
    }

    fun isTestCoverageTabVisible(): Boolean {
        return testCoverage.isVisible
    }

    fun isPrivacyAndDateSecurityTabVisible(): Boolean {
        return privacyAndDataSecurity.isVisible
    }

    fun isConsultTabVisible(): Boolean {
        return consult.isVisible
    }

    fun isActionPlanTabVisible(): Boolean {
        return actionPlan.isVisible
    }

    fun isPointsTabVisible(): Boolean {
        return points.isVisible
    }

    fun clickProcessAndAppointmentTab(){
        appointmentAndProcess.scrollIntoViewIfNeeded()
        appointmentAndProcess.click()
    }

    fun clickTestCoverageTab() {
        testCoverage.scrollIntoViewIfNeeded()
        testCoverage.click()
    }

    fun clickPrivacyAndDateSecurityTab() {
        privacyAndDataSecurity.scrollIntoViewIfNeeded()
        privacyAndDataSecurity.click()
    }

    fun clickConsultTab() {
        consult.scrollIntoViewIfNeeded()
        consult.click()
    }

    fun clickActionPlanTab() {
        actionPlan.scrollIntoViewIfNeeded()
        actionPlan.click()
    }

    fun clickPointsTab() {
        points.scrollIntoViewIfNeeded()
        points.click()
    }


    fun isWhoHelpsMeUnderStandMyResultQuestionVisible(): Boolean {
        val question = page.getByText("Who helps me understand my results")
        val answer =
            page.getByText("A longevity health expert reviews your results with you, explains priorities, and answers questions so you leave with clarity.")

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isDoYouTestEverythingOrOnlWhatMattersVisible(): Boolean {
        val question = page.getByText("Do you test everything or only what matters")
        val answer =
            page.getByText("Baseline tests only what matters. Each biomarker is included because it adds clarity and actionability, not because it is commonly bundled in standard check ups.")

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhyMostHealthTestsFeelConfusingVisible(): Boolean {
        val question = page.getByText("Why most health tests feel confusing")
        val answer =
            page.getByText("Most health tests deliver long reports filled with numbers and medical terms but little context. They rarely explain what matters most or how to act, leaving people unsure and overwhelmed.")

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhatMakesBaselineDifferentFromOtherBloodTestsVisible(): Boolean {
        val question = page.getByText("What makes Baseline different from other blood tests")
        val answer =
            page.getByText("Most blood tests give numbers and reference ranges. Baseline connects those numbers into patterns, priorities, and actions so you understand what matters, why it matters, and what to do next.")

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhatExactlyIsBaselineTestsVisible(): Boolean {
        val question = page.getByText("What exactly is Baseline")
        val answer =
            page.getByText(
                "Baseline is a preventive health starting point that combines an advanced blood test and expert guidance to show where your health stands today and what to focus on next."
            )

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhoIsBaselineForVisible(): Boolean {
        val question = page.getByText("Who is Baseline for")
        val answer =
            page.getByText(
                "Baseline is for people who want clarity about their health without guesswork. It suits those who feel tired, stuck, overwhelmed by advice, or want to be proactive before problems appear."
            )

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isCanBaselineHelpMeUnderstandWhyIFeelLowEnergyOrUnwellVisible(): Boolean {
        val question = page.getByText("Can Baseline help me understand why I feel low energy or unwell?")
        val answer =
            page.getByText(
                "Yes. Our dashboard connects how you feel with what’s going on inside your body, using data from your blood to explain fatigue, sleep issues, mood dips, or metabolic sluggishness."
            )

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhoShouldTakeThisTestVisible(): Boolean {
        val question = page.getByText("Who should take this test?")
        val answer =
            page.getByText(
                "Anyone looking to take control of their health. Whether it’s prevention, performance or understanding your body better, this is for you."
            )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isIveDoneARecentHealthCheckUpShouldIStillGoForBaselineVisible(): Boolean {
        val question = page.getByText("I’ve done a recent health check-up. Should I still go for Baseline?")
        val answer =
            page.getByText(
                "Our tests go beyond routine screenings to uncover deeper patterns and trends in your health. Each test establishes a clear snapshot of where you stand today, helping identify what is working well and what needs attention going forward."
            )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhenShouldICheckMyBaselineVisible(): Boolean {
        val question = page.getByText("When should I check my Baseline?")
        val answer =
            page.getByText(
                "The best time is now. Baseline gives you deeper insights into chronic risks like diabetes, heart disease, and even early cancer markers helping you take control of your health today, not when it’s too late."
            )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }



    fun isWhoToContactForHelpVisible(): Boolean {
        val question = page.getByText("Who to contact for help")
        val answer = page.getByText(
            "Our Concierge support is available throughout the process to help with booking, questions, or follow up. You can message the DH Concierge on WhatsApp at +91-63844-85138"
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhatHappensAfterResultsArriveVisible(): Boolean {
        val question = page.getByText("What happens after results arrive")
        val answer = page.getByText(
            "Once results are ready, you receive your report and can schedule your consultation to review insights and next steps."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhyTwoBloodDrawsVisible(): Boolean {
        val question = page.getByText("Why are there two blood draws—one before and one after a meal?")
        val answer = page.getByText(
            "Some tests, like blood glucose and insulin response, require comparing your fasting levels with your levels after eating. This helps us assess how your body processes sugar and insulin. The post-meal (postprandial) test is ideally done 2 hours after you finish a standard meal."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowLongDoesBloodDrawTakeVisible(): Boolean {
        val question = page.getByText("How long does the blood draw take?")
        val answer = page.getByText(
            "The blood draw is done in two quick sessions, each taking less than 10 minutes. First, we collect a fasting sample. After your meal, we’ll take a post-prandial sample to complete the collection. The entire process is smooth, efficient, and designed to fit easily into your day."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowAddOnsImproveClarityVisible(): Boolean {
        val question = page.getByText("How add ons improve clarity")
        val answer = page.getByText(
            "Add on tests help deepen understanding by narrowing in on specific systems or patterns seen in your Baseline results."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhetherAddOnsMandatoryVisible(): Boolean {
        val question = page.getByText("Whether add ons are mandatory")
        val answer = page.getByText(
            "No. Add on tests are completely optional and never mandatory."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhatAddOnTestsAvailableVisible(): Boolean {
        val question = page.getByText("What add on tests are available")
        val answer = page.getByText(
            "Add on tests include specialised panels that explore specific health areas based on your results or goals."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isCanIAddTestsLaterVisible(): Boolean {
        val question = page.getByText("Can I add tests later")
        val answer = page.getByText(
            "Yes. Additional tests can be added later if recommended or requested."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isBaselineMedicallyReliableVisible(): Boolean {
        val question = page.getByText("Is Baseline medically reliable")
        val answer = page.getByText(
            "Yes. Baseline is medically reliable as a preventive and health optimisation assessment, though it does not replace clinical diagnosis."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhoCanAccessMyDataVisible(): Boolean {
        val question = page.getByText("Who can access my data")
        val answer = page.getByText(
            "Only you and authorised professionals involved in your care can access your health data."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowPrivacyProtectedVisible(): Boolean {
        val question = page.getByText("How privacy is protected")
        val answer = page.getByText(
            "Privacy is protected through strict internal policies, access controls, and secure infrastructure."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isCanIDeleteMyDataVisible(): Boolean {
        val question = page.getByText("Can I delete my data")
        val answer = page.getByText(
            "Yes. You can request deletion of your health data in accordance with applicable data protection guidelines."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhetherMyDataSharedVisible(): Boolean {
        val question = page.getByText("Whether my data is shared")
        val answer = page.getByText(
            "Your data is not sold or shared with third parties without your explicit consent."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowHealthDataStoredVisible(): Boolean {
        val question = page.getByText("How my health data is stored")
        val answer = page.getByText(
            "Your health data is stored securely using encrypted systems designed to protect sensitive personal information."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isConsultationIncludedVisible(): Boolean {
        val question = page.getByText("Is consultation included")
        val answer = page.getByText(
            "Yes. One expert consultation is included as part of the Baseline experience."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isConsultationDurationVisible(): Boolean {
        val question = page.getByText("How long the consultation takes")
        val answer = page.getByText(
            "Most consultations last between 30 and 45 minutes, allowing time for explanation and questions."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhatHappensDuringConsultationVisible(): Boolean {
        val question = page.getByText("What happens during the consultation")
        val answer = page.getByText(
            "Your results are reviewed in context, key patterns are explained, priorities are identified, and next steps are discussed clearly."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowPersonalisedGuidanceVisible(): Boolean {
        val question = page.getByText("How personalised the guidance is")
        val answer = page.getByText(
            "Guidance is fully personalised based on your data, symptoms, and lifestyle, rather than generic protocols or one size fits all plans."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhoConductsConsultationVisible(): Boolean {
        val question = page.getByText("Who conducts the consultation")
        val answer = page.getByText(
            "Consultations are conducted by qualified longevity experts trained to interpret biomarkers alongside lifestyle factors, symptoms, and health goals."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhenToRetestVisible(): Boolean {
        val question = page.getByText("When to retest")
        val answer = page.getByText(
            "Retesting is typically suggested after lifestyle changes or as advised, depending on what you are tracking."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhenAddOnsRecommendedVisible(): Boolean {
        val question = page.getByText("When add ons are recommended")
        val answer = page.getByText(
            "Add on tests are recommended only when they provide additional clarity or help investigate a specific concern."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowPlanEvolvesVisible(): Boolean {
        val question = page.getByText("How the plan evolves over time")
        val answer = page.getByText(
            "The plan evolves as your biomarkers change and goals shift, ensuring recommendations remain relevant and effective."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowFollowUpTestsSuggestedVisible(): Boolean {
        val question = page.getByText("How follow up tests are suggested")
        val answer = page.getByText(
            "Follow up tests are suggested only when they add clarity or help track progress, not as a default recommendation."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowPrioritiesDecidedVisible(): Boolean {
        val question = page.getByText("How priorities are decided")
        val answer = page.getByText(
            "Priorities are set based on impact, urgency, and feasibility, focusing on changes that meaningfully improve health outcomes."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhereICanSeeMyPointsVisible(): Boolean {
        val question = page.getByText("Where I can see my points")
        val answer = page.getByText(
            "You can view your DH Points balance and activity in your account dashboard."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhetherPointsExpireVisible(): Boolean {
        val question = page.getByText("Whether points expire")
        val answer = page.getByText(
            "Any expiry details for DH Points are clearly shown in your dashboard."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowGiftingPointsWorksVisible(): Boolean {
        val question = page.getByText("How gifting points works")
        val answer = page.getByText(
            "DH Points can be gifted to others, allowing them to use the value towards eligible services."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isWhatPointsUsedForVisible(): Boolean {
        val question = page.getByText("What DH Points can be used for")
        val answer = page.getByText(
            "DH Points can be redeemed for future tests, consultations, or other eligible Deep Holistics services."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    fun isHowReferralsWorkVisible(): Boolean {
        val question = page.getByText("How referrals work")
        val answer = page.getByText(
            "You can refer friends or family to Baseline. When they complete their test, you earn DH Points as a reward."
        )
        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

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
