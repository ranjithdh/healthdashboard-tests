package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

/**
 * Reusable FAQ section component that can be used across multiple pages.
 * This class encapsulates all FAQ tab locators and related methods.
 */
class FaqSection(private val page: Page,private val isFaqPage: Boolean = false) {


    private val generalTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("General"))
    private val appointmentAndProcessTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Appointment & Process"))
    private val testCoverageTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Test Coverage"))
    private val consultTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Consult"))
    private val actionPlanTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Action Plan"))
    private val privacyAndDataSecurityTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Privacy & Data Security"))
    private val pointsTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Points"))


    fun isGeneralTabVisible(): Boolean {
        return generalTab.isVisible
    }

    fun isAppointmentAndProcessVisible(): Boolean {
        return appointmentAndProcessTab.isVisible
    }

    fun isTestCoverageTabVisible(): Boolean {
        return testCoverageTab.isVisible
    }

    fun isPrivacyAndDataSecurityTabVisible(): Boolean {
        return privacyAndDataSecurityTab.isVisible
    }

    fun isConsultTabVisible(): Boolean {
        return consultTab.isVisible
    }

    fun isActionPlanTabVisible(): Boolean {
        return actionPlanTab.isVisible
    }

    fun isPointsTabVisible(): Boolean {
        return pointsTab.isVisible
    }


    fun clickGeneralTab() {
        generalTab.scrollIntoViewIfNeeded()
        generalTab.click()
    }

    fun clickAppointmentAndProcessTab() {
        appointmentAndProcessTab.scrollIntoViewIfNeeded()
        appointmentAndProcessTab.click()
    }

    fun clickTestCoverageTab() {
        testCoverageTab.scrollIntoViewIfNeeded()
        testCoverageTab.click()
    }

    fun clickPrivacyAndDataSecurityTab() {
        privacyAndDataSecurityTab.scrollIntoViewIfNeeded()
        privacyAndDataSecurityTab.click()
    }

    fun clickConsultTab() {
        consultTab.scrollIntoViewIfNeeded()
        consultTab.click()
    }

    fun clickActionPlanTab() {
        actionPlanTab.scrollIntoViewIfNeeded()
        actionPlanTab.click()
    }

    fun clickPointsTab() {
        pointsTab.scrollIntoViewIfNeeded()
        pointsTab.click()
    }


    fun isFaqQuestionAndAnswerVisible(questionText: String, answerText: String): Boolean {

        val question = if (isFaqPage) {
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(questionText))
        }else{
            page.getByText(questionText)
        }

        val answer = page.getByText(answerText)

        question.waitFor()
        question.click()
        return question.isVisible && answer.isVisible
    }

    // ---------------------- Common FAQ Questions ----------------------

    fun isWhoHelpsMeUnderstandMyResultQuestionVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who helps me understand my results",
            "A longevity health expert reviews your results with you, explains priorities, and answers questions so you leave with clarity."
        )
    }

    fun isDoYouTestEverythingOrOnlyWhatMattersVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Do you test everything or only what matters",
            "Baseline tests only what matters. Each biomarker is included because it adds clarity and actionability, not because it is commonly bundled in standard check ups."
        )
    }

    fun isWhyMostHealthTestsFeelConfusingVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Why most health tests feel confusing",
            "Most health tests deliver long reports filled with numbers and medical terms but little context. They rarely explain what matters most or how to act, leaving people unsure and overwhelmed."
        )
    }

    fun isWhatMakesBaselineDifferentFromOtherBloodTestsVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What makes Baseline different from other blood tests",
            "Most blood tests give numbers and reference ranges. Baseline connects those numbers into patterns, priorities, and actions so you understand what matters, why it matters, and what to do next."
        )
    }

    fun isWhatExactlyIsBaselineVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What exactly is Baseline",
            "Baseline is a preventive health starting point that combines an advanced blood test and expert guidance to show where your health stands today and what to focus on next."
        )
    }

    fun isWhoIsBaselineForVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who is Baseline for",
            "Baseline is for people who want clarity about their health without guesswork. It suits those who feel tired, stuck, overwhelmed by advice, or want to be proactive before problems appear."
        )
    }

    fun isCanBaselineHelpMeUnderstandWhyIFeelLowEnergyOrUnwellVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can Baseline help me understand why I feel low energy or unwell?",
            "Yes. Our dashboard connects how you feel with what’s going on inside your body, using data from your blood to explain fatigue, sleep issues, mood dips, or metabolic sluggishness."
        )
    }

    fun isWhoShouldTakeThisTestVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who should take this test?",
            "Anyone looking to take control of their health. Whether it’s prevention, performance or understanding your body better, this is for you."
        )
    }

    fun isIveDoneARecentHealthCheckUpShouldIStillGoForBaselineVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "I’ve done a recent health check-up. Should I still go for Baseline?",
            "Our tests go beyond routine screenings to uncover deeper patterns and trends in your health. Each test establishes a clear snapshot of where you stand today, helping identify what is working well and what needs attention going forward."
        )
    }

    fun isWhenShouldICheckMyBaselineVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "When should I check my Baseline?",
            "The best time is now. Baseline gives you deeper insights into chronic risks like diabetes, heart disease, and even early cancer markers helping you take control of your health today, not when it’s too late."
        )
    }

    fun isBaselineAReplacementForADoctorVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Is Baseline a replacement for a doctor",
            "No. Baseline complements medical care but does not replace a doctor or clinical diagnosis"
        )
    }


    fun isIsBaselineForDiagnosingDisease():Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Is Baseline for diagnosing disease",
            "No. Baseline is designed for preventive insight and health optimisation, not for diagnosing diseases."
        )
    }

    fun shouldIUseBaselineDuringAcuteIllness(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Should I use Baseline during acute illness",
            "No. If you are experiencing acute symptoms or illness, medical care should be prioritised before any preventive testing."
        )
    }

    fun areThereAnyHiddenCosts(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Are there any hidden costs",
            "No. All inclusions are clearly communicated upfront with no hidden charges."
        )
    }

    fun whatIsNotIncluded(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What is not included",
            "Add on tests, external medical treatments, and specialist care outside Baseline are not included."
        )
    }

    fun whatIsIncludedInTheBaselinePrice(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What is included in the Baseline price",
            "The Baseline price includes the blood test, detailed report, expert consultation, and a personalised action plan."
        )
    }

    fun whichLabsAreUsed(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Which labs are used",
            "Baseline works with accredited partner laboratories that follow strict quality and compliance standards."
        )
    }

    fun whatNormalOptimalAndOutOfRangeMean(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What normal, optimal, and out of range mean",
            "Normal means within a broad population range. Optimal means supportive of long term health. Out of range means it needs attention, not that something is wrong."
        )
    }

    fun howResultsAreOrganisedAndExplained(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How results are organised and explained",
            "Your results are organised by health themes rather than isolated lab values, making it easier to see patterns, connections, and overall meaning."
        )
    }

    fun howBiomarkersAreSelected(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How biomarkers are selected",
            "Biomarkers are selected based on clinical relevance, scientific evidence, and their ability to provide actionable insight when viewed together."
        )
    }

    fun whatKindOfBiomarkersAreIncluded(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What kind of biomarkers are included",
            "Baseline measures biomarkers linked to metabolism, inflammation, hormonal balance, nutrient status, liver function, cardiovascular health, and overall physiological stability."
        )
    }

    fun whatProblemBaselineIsDesignedToSolve(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What problem Baseline is designed to solve",
            "Baseline is designed to close the gap between testing and action by turning complex health data into clear priorities and practical next steps."
        )
    }

    fun whyClarityAndPrioritisationMatterMoreThanMoreTests(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Why clarity and prioritisation matter more than more tests",
            "More data without direction often leads to anxiety and inaction. Health improves faster when you focus on a few high impact areas instead of trying to fix everything at once."
        )
    }

    fun howBaselineFitsIntoAModernHealthJourney(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How Baseline fits into a modern health journey",
            "Baseline acts as a reference point you can return to, helping you make informed choices, track progress over time, and adapt as your body and goals change."
        )
    }

    fun howBaselineHelpsYouUnderstandWhereYouStand(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How Baseline helps you understand where you stand",
            "Baseline organises health data into a structured view that highlights what is working, what needs attention, and what matters most right now."
        )
    }

    fun isBaselineOneTimeOrOngoing(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Is Baseline a one time test or something ongoing",
            "Baseline starts as a snapshot of your current health. Many people repeat it over time to track progress, but it can also be done as a one time clarity exercise."
        )
    }

    fun howOftenShouldIGetTested(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How often should I get tested?",
            "Twice a year is ideal. This helps track progress, measure the effectiveness of interventions, and catch any changes early."
        )
    }

    fun canBaselineAssessMyHeartHealth(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can Baseline assess my heart health?",
            "Yes. We test markers like ApoB and Lp(a) which are among the most predictive for heart disease. These aren’t part of most Indian full-body check-ups."
        )
    }

    fun whyDontRegularHealthCheckupsIncludeTheseBiomarkers(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Why don’t regular health check-ups include the biomarkers Baseline covers?",
            "Most standard health check-ups only test for the basics. We have curated an advanced blood panel which includes advanced biomarkers like ApoB, Lp(a), Insulin, Homocysteine, Interleukin 6 (IL-6), Vitamins panel etc. that require specific lab protocols and are often skipped. Our goal is proactive care, not reactive diagnostics."
        )
    }

    fun doYouDoYourOwnTesting(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Do you do your own testing?",
            "We collaborate with NABL and CAP accredited (the highest certifying bodies in the world for Labs), reputable third-party labs to ensure the highest accuracy and quality in testing. Our role is to integrate these results into a comprehensive, personalised health profile through our proprietary analysis and interpretation."
        )
    }

    fun whatValueDoYouAddWithThirdPartyTesting(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What value do you add if you are using third-party testing?",
            "Our value lies in what happens after the test. The dashboard connects data from your lab results with your symptoms, lifestyle, and wearable insights to reveal patterns and correlations you won’t find in a standalone report. We help you track progress over time, understand how your biomarkers impact your energy, mood, sleep, and more, and turn complex data into personalised insights and clear next steps for your health journey."
        )
    }

    fun doIHaveToGoToAPhysicalCentre(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Do I have to go to a physical centre?",
            "No, you don’t need to visit a physical centre. Our process is designed to be convenient and accessible, with blood samples collected at your home and digital consultations available for ongoing support."
        )
    }

    fun howAccurateAreYourResults(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How accurate are your results?",
            "Our results are based on data from accredited labs and cutting-edge analytical algorithms. We combine precision testing with advanced analysis to provide highly accurate and personalised health insights."
        )
    }

    fun whatDoIDoWithTheData(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What do I do with the data?",
            "Baseline helps you make informed decisions by highlighting the areas that matter most to your health, from cholesterol and metabolism to recovery and inflammation. It also allows you to track changes over time, refine your approach as your health evolves, and have more meaningful conversations with experts. Together, this clarity supports better daily choices today while helping reduce future health risks with confidence."
        )
    }




    // Appointments & Process Questions

    fun isWhoToContactForHelpVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who to contact for help",
            "Our Concierge support is available throughout the process to help with booking, questions, or follow up. You can message the DH Concierge on WhatsApp at +91-63844-85138"
        )
    }

    fun isWhatHappensAfterResultsArriveVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What happens after results arrive",
            "Once results are ready, you receive your report and can schedule your consultation to review insights and next steps."
        )
    }

    fun isWhyTwoBloodDrawsVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Why are there two blood draws—one before and one after a meal?",
            "Some tests, like blood glucose and insulin response, require comparing your fasting levels with your levels after eating. This helps us assess how your body processes sugar and insulin. The post-meal (postprandial) test is ideally done 2 hours after you finish a standard meal."
        )
    }

    fun isHowLongDoesBloodDrawTakeVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How long does the blood draw take?",
            "The blood draw is done in two quick sessions, each taking less than 10 minutes. First, we collect a fasting sample. After your meal, we’ll take a post-prandial sample to complete the collection. The entire process is smooth, efficient, and designed to fit easily into your day."
        )
    }

    fun whatToDoBeforeTheTest(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What to do before the test",
            "You will receive simple preparation instructions before the test, including fasting or timing guidance if required."
        )
    }

    fun howLongItTakesToGetResults(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How long it takes to get results",
            "Results are typically available within a few days after sample collection."
        )
    }

    fun howToBookBaseline(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How to book Baseline",
            "You can book Baseline online through the website or with help from the customer support team."
        )
    }

    fun whatToDoIfINeedMedicalCare(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What to do if I need medical care",
            "If you need medical care, consult a qualified medical professional or visit a healthcare facility immediately."
        )
    }

    fun howSamplesAreHandled(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How samples are handled",
            "Samples are collected, transported, and processed under controlled conditions to preserve integrity and accuracy."
        )
    }

    fun whatIfINeedToReschedule(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What if I need to reschedule",
            "If you need to reschedule, customer support will help adjust your appointment easily."
        )
    }

    fun howHomeSampleCollectionWorks(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How home sample collection works",
            "After booking, a trained professional visits your chosen location at the scheduled time to collect your blood sample."
        )
    }

    fun howHygieneAndSafetyAreEnsured(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How hygiene and safety are ensured",
            "Sterile, single use equipment is used and standard medical safety protocols are followed during collection and handling."
        )
    }

    fun howLongTheProcessTakes(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How long the process takes",
            "The sample collection usually takes around 10 to 15 minutes."
        )
    }

    fun whoCollectsTheSample(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who collects the sample",
            "Samples are collected by certified phlebotomists trained in safe and hygienic procedures."
        )
    }

    fun canBaselineDiagnoseMe(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can Baseline diagnose me?",
            "No. We offer insights, not diagnoses. Your results should be reviewed with your doctor for medical advice."
        )
    }

    fun doINeedToStopMedicationsBeforeMyTest(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Do I need to stop medications before my test?",
            "No—never stop any medication unless advised by your doctor. Some tests may require specific timing; we’ll notify you beforehand."
        )
    }

    fun doINeedToFastOrPrepareBeforeMyTest(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Do I need to fast or prepare before my test?",
            "Some of the tests we conduct require fasting, typically for about 8-12 hours. If fasting is needed, you’ll receive clear instructions in advance via email and WhatsApp."
        )
    }

    fun canIGetTestedIfImPregnantOrNursing(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can I get tested if I’m pregnant or nursing?",
            "We suggest waiting at least 6 months postpartum and after you stop nursing as hormonal changes during this period may affect accuracy. These tests and interpretations are not meant to be taken during either pregnancy or the nursin[c]g period."
        )
    }





    // Test Coverage Questions

    fun isHowAddOnsImproveClarityVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How add ons improve clarity",
            "Add on tests help deepen understanding by narrowing in on specific systems or patterns seen in your Baseline results."
        )
    }

    fun isWhetherAddOnsMandatoryVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Whether add ons are mandatory",
            "No. Add on tests are completely optional and never mandatory."
        )
    }

    fun isWhatAddOnTestsAvailableVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What add on tests are available",
            "Add on tests include specialised panels that explore specific health areas based on your results or goals."
        )
    }

    fun isCanIAddTestsLaterVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can I add tests later",
            "Yes. Additional tests can be added later if recommended or requested."
        )
    }

    fun isBaselineMedicallyReliableVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Is Baseline medically reliable",
            "Yes. Baseline is medically reliable as a preventive and health optimisation assessment, though it does not replace clinical diagnosis."
        )
    }

    fun howAccurateAreTheTests(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How accurate are the tests",
            "The tests are conducted using clinically validated methods and standardised procedures to ensure reliable and consistent results."
        )
    }

    fun willIUnderstandAllTheResults(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Will I understand all the results",
            "Yes. Results are explained in simple language, grouped by health themes, and reviewed with an expert so you understand what they mean for you."
        )
    }

    fun howSoonWillIGetMyResults(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How soon will I get my results?",
            "Within 48-72 hours after your test. Your personalized insights will be available on your secure dashboard."
        )
    }

    fun whatMoreDoesBaselineConsultOfferComparedToMyFamilyDoctor(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What more does Baseline consult offer compared to my family doctor?",
            "Doctors often treat symptoms. We assess root causes. Our panel is more comprehensive, focused on prevention, and personalized to your body’s long-term needs."
        )
    }

    fun canBaselineTellMeAboutMyFertilityHealth(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can Baseline tell me about my fertility health?",
            "We include hormone panels that can give valuable insights into fertility for both men and women. However, we do not diagnose fertility conditions—please discuss with your physician."
        )
    }

    fun canDeepHolisticsTellMeIfImMetabolicallyFit(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can Deep Holistics tell me if I’m metabolically fit?",
            "Yes. We measure insulin, fasting glucose, HOMA-IR, triglycerides, and more, giving you a clear picture of your metabolic health and how to improve it."
        )
    }

    fun whatTestsAreIncludedInTheBaseline(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What tests are included in the Baseline?",
            "Our Baseline plan includes tests for heart health, metabolic health, hormonal balance, inflammation, essential vitamins and minerals, liver and kidney health, going well beyond routine blood tests. Check out our what we test page to know more."
        )
    }




    // Privacy & Data Security Questions

    fun isWhoCanAccessMyDataVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who can access my data",
            "Only you and authorised professionals involved in your care can access your health data."
        )
    }

    fun isHowPrivacyProtectedVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How privacy is protected",
            "Privacy is protected through strict internal policies, access controls, and secure infrastructure."
        )
    }

    fun isCanIDeleteMyDataVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can I delete my data",
            "Yes. You can request deletion of your health data in accordance with applicable data protection guidelines."
        )
    }

    fun isWhetherMyDataSharedVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Whether my data is shared",
            "Your data is not sold or shared with third parties without your explicit consent."
        )
    }

    fun isHowHealthDataStoredVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How my health data is stored",
            "Your health data is stored securely using encrypted systems designed to protect sensitive personal information."
        )
    }

    fun howDataQualityIsEnsured(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How data quality is ensured",
            "Multiple quality checks are applied across collection, processing, and reporting to minimise errors."
        )
    }

    fun canIUpdateMyPersonalInformation(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can I update my personal information?",
            "Yes. Write to support@deepholistics.com with your request. We’ll process it securely and swiftly."
        )
    }

    fun whoCanSeeMyTestResults(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who can see my test results?",
            "Only you, our licensed lab partners, and the Deep Holistics medical team who help interpret your data."
        )
    }

    fun isMyDataSafeWithDeepHolistics(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Is my data safe with Deep Holistics?",
            "Yes. Your data is encrypted end-to-end and stored with industry-grade security. Only healthcare personnel involved in your care can access it."
        )
    }




    // Consult Questions

    fun isConsultationIncludedVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Is consultation included",
            "Yes. One expert consultation is included as part of the Baseline experience."
        )
    }

    fun isConsultationDurationVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How long the consultation takes",
            "Most consultations last between 30 and 45 minutes, allowing time for explanation and questions."
        )
    }

    fun isWhatHappensDuringConsultationVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What happens during the consultation",
            "Your results are reviewed in context, key patterns are explained, priorities are identified, and next steps are discussed clearly."
        )
    }

    fun isHowPersonalisedGuidanceVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How personalised the guidance is",
            "Guidance is fully personalised based on your data, symptoms, and lifestyle, rather than generic protocols or one size fits all plans."
        )
    }

    fun isWhoConductsConsultationVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Who conducts the consultation",
            "Consultations are conducted by qualified longevity experts trained to interpret biomarkers alongside lifestyle factors, symptoms, and health goals."
        )
    }

    fun isHowSymptomsAreConnectedToResultsVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How symptoms are connected to results",
            "Symptoms help interpret results by providing context. Patterns across multiple markers often explain how you feel better than a single value."
        )
    }



    // Action Plan Questions

    fun isWhenToRetestVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "When to retest",
            "Retesting is typically suggested after lifestyle changes or as advised, depending on what you are tracking."
        )
    }

    fun isWhenAddOnsRecommendedVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "When add ons are recommended",
            "Add on tests are recommended only when they provide additional clarity or help investigate a specific concern."
        )
    }

    fun isHowPlanEvolvesVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How the plan evolves over time",
            "The plan evolves as your biomarkers change and goals shift, ensuring recommendations remain relevant and effective."
        )
    }

    fun isHowFollowUpTestsSuggestedVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How follow up tests are suggested",
            "Follow up tests are suggested only when they add clarity or help track progress, not as a default recommendation."
        )
    }

    fun isHowPrioritiesDecidedVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How priorities are decided",
            "Priorities are set based on impact, urgency, and feasibility, focusing on changes that meaningfully improve health outcomes."
        )
    }


    fun whatTheActionPlanIncludes(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What the action plan includes",
            "Your action plan includes clear priorities, recommended lifestyle adjustments, and nutrition or supplement guidance where relevant."
        )
    }

    fun canIAskFollowUpQuestions(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can I ask follow up questions",
            "Yes. You can ask clarifying questions during the consultation and seek further support if something remains unclear."
        )
    }

    fun whatIfSomethingLooksConcerning(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What if something looks concerning",
            "If a result needs attention, it is clearly highlighted and explained along with guidance on lifestyle action, monitoring, or medical follow up."
        )
    }



    // Points Questions

    fun isWhereICanSeeMyPointsVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Where I can see my points",
            "You can view your DH Points balance and activity in your account dashboard."
        )
    }

    fun isWhetherPointsExpireVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Whether points expire",
            "Any expiry details for DH Points are clearly shown in your dashboard."
        )
    }

    fun isHowGiftingPointsWorksVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How gifting points works",
            "DH Points can be gifted to others, allowing them to use the value towards eligible services."
        )
    }

    fun isWhatPointsUsedForVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "What DH Points can be used for",
            "DH Points can be redeemed for future tests, consultations, or other eligible Deep Holistics services."
        )
    }

    fun isHowReferralsWorkVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "How referrals work",
            "You can refer friends or family to Baseline. When they complete their test, you earn DH Points as a reward."
        )
    }

    fun isCanIGiftBaselineToMyFamilyOrFriendsVisible(): Boolean {
        return isFaqQuestionAndAnswerVisible(
            "Can I gift Baseline to my family or friends?",
            "Yes. Health is the best gift. You can purchase a plan on their behalf through our website."
        )
    }
}
