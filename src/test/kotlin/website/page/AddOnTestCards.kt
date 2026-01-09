package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

enum class AddOnTestPageType {
    LANDING,
    HOW_IT_WORKS,
    WHAT_WE_TEST,
    ALL_TESTS
}


class AddOnTestCards(val page: Page, private val pageType: AddOnTestPageType) {


    fun getViewAllAddOnTestButton(): Locator? {
        return when (pageType) {
            AddOnTestPageType.LANDING -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("View All Add-on Tests"))
            }

            AddOnTestPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("View All Add-on Tests"))
            }

            AddOnTestPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("View All Add-on Tests")).first()
            }

            else -> {
                null
            }
        }
    }

    fun getAddOnTestHeader(): Locator? {
        return when (pageType) {
            AddOnTestPageType.LANDING -> {
                page.getByRole(
                    AriaRole.HEADING,
                    Page.GetByRoleOptions().setName("O p t i o n a l a d d - o n s f o r d e e p e r i n s i g h t")
                )
            }

            AddOnTestPageType.HOW_IT_WORKS -> {
                page.getByRole(
                    AriaRole.HEADING,
                    Page.GetByRoleOptions().setName("O p t i o n a l a d d - o n s f o r d e e p e r i n s i g h t")
                )
            }

            AddOnTestPageType.WHAT_WE_TEST -> {
                page.getByRole(
                    AriaRole.HEADING,
                    Page.GetByRoleOptions()
                        .setName("P l u s a d d - o n d i a g n o s t i c t e s t i n g a v a i l a b l e")
                )
            }

            else -> {
                null
            }
        }
    }

    fun isAddOnTestHeadingVisible(): Boolean {
        val header = getAddOnTestHeader()
        header?.waitFor()
        return header?.isVisible ?: false
    }

    fun isAddOnTestDescriptionVisible(): Boolean {
        val description = when (pageType) {
            AddOnTestPageType.LANDING -> {
                page.getByText("Your Baseline helps you know where you stand. That’s why we’ve built a holistic platform of advanced diagnostics to measure and improve every aspect of your health journey.")
            }

            AddOnTestPageType.HOW_IT_WORKS -> {
                page.getByText("Your Baseline helps you know where you stand. That’s why we’ve built a holistic platform of advanced diagnostics to measure and improve every aspect of your health journey.")
            }

            AddOnTestPageType.WHAT_WE_TEST -> {
                page.getByText("Access comprehensive tests well beyond mainstream healthcare limits.")
            }

            else -> {
                return false
            }
        }

        return description.isVisible
    }

    fun isViewAllAddOnTestButtonVisible(): Boolean {
        return getViewAllAddOnTestButton()?.isVisible ?: false
    }

    fun clickViewAllAddOnTestButton(): AddOnTestPage {
        getViewAllAddOnTestButton()?.click()
        val allTestPage = AddOnTestPage(page)
        allTestPage.waitForPageLoad()
        return allTestPage
    }


    fun isTestVisible(testType: TestType, pageType: AddOnTestPageType): Boolean {
        return checkVisibility(
            testType.testName,
            testType.getDescription(pageType)
        )
    }

    fun clickTest(testType: TestType, pageType: AddOnTestPageType, page: Page) {
        page.getByRole(
            AriaRole.LINK,
            Page.GetByRoleOptions().setName(testType.testName.plus(testType.getDescription(pageType)))
        )
    }

    fun isAllergyVisible(): Boolean {
        return isTestVisible(TestType.ALLERGIES, pageType)
    }

    fun clickAllergyTest() {
        clickTest(TestType.ALLERGIES, pageType, page)
    }

    fun isGutMicrobiomeVisible(): Boolean {
        return isTestVisible(TestType.GUT_MICROBIOME, pageType)
    }

    fun clickGutMicrobiomeTest() {
        clickTest(TestType.GUT_MICROBIOME, pageType, page)
    }

    fun isStressAndCortisolVisible(): Boolean {
        return isTestVisible(TestType.STRESS_CORTISOL, pageType)
    }

    fun clickStressCortisolTest() {
        clickTest(TestType.STRESS_CORTISOL, pageType, page)
    }

    fun isGeneVisible(): Boolean {
        return isTestVisible(TestType.GENETIC_ANALYSIS, pageType)
    }

    fun clickGeneTest() {
        clickTest(TestType.GENETIC_ANALYSIS, pageType, page)
    }

    fun isOmegaVisible(): Boolean {
        return isTestVisible(TestType.OMEGA_PROFILE, pageType)
    }

    fun clickOmegaTest() {
        clickTest(TestType.OMEGA_PROFILE, pageType, page)
    }

    fun isToxicMetalsVisible(): Boolean {
        return isTestVisible(TestType.TOXIC_METALS, pageType)
    }

    fun clickToxicMetalsTest() {
        clickTest(TestType.TOXIC_METALS, pageType, page)
    }

    fun isThyroidHealthVisible(): Boolean {
        return isTestVisible(TestType.THYROID_HEALTH, pageType)
    }

    fun clickThyroidHealthTest() {
        clickTest(TestType.THYROID_HEALTH, pageType, page)
    }

    fun isWomensHealthVisible(): Boolean {
        return isTestVisible(TestType.WOMEN_HEALTH, pageType)
    }

    fun clickWomensHealthTest() {
        clickTest(TestType.WOMEN_HEALTH, pageType, page)
    }

    fun isEssentialNutrientsVisible(): Boolean {
        return isTestVisible(TestType.ESSENTIAL_NUTRIENTS, pageType)
    }

    fun clickEssentialNutrientsTest() {
        clickTest(TestType.ESSENTIAL_NUTRIENTS, pageType, page)
    }

    fun isAdvancedThyroidVisible(): Boolean {
        return isTestVisible(TestType.ADVANCED_THYROID, pageType)
    }

    fun clickAdvancedThyroidTest() {
        clickTest(TestType.ADVANCED_THYROID, pageType, page)
    }

    fun isLiverHealthVisible(): Boolean {
        return isTestVisible(TestType.LIVER_HEALTH, pageType)
    }

    fun clickLiverHealthTest() {
        clickTest(TestType.LIVER_HEALTH, pageType, page)
    }

    fun isAutoImmuneVisible(): Boolean {
        return isTestVisible(TestType.AUTO_IMMUNE, pageType)
    }

    fun clickAutoImmuneTest() {
        clickTest(TestType.AUTO_IMMUNE, pageType, page)
    }

    fun isAdvancedHeartHealthVisible(): Boolean {
        return isTestVisible(TestType.ADVANCED_HEART_HEALTH, pageType)
    }

    fun clickAdvancedHeartHealthTest() {
        clickTest(TestType.ADVANCED_HEART_HEALTH, pageType, page)
    }

    fun isWomensFertilityVisible(): Boolean {
        return isTestVisible(TestType.WOMEN_FERTILITY, pageType)
    }

    fun clickWomensFertilityTest() {
        clickTest(TestType.WOMEN_FERTILITY, pageType, page)
    }

    fun isBloodHealthVisible(): Boolean {
        return isTestVisible(TestType.BLOOD_HEALTH, pageType)
    }

    fun clickBloodHealthTest() {
        clickTest(TestType.BLOOD_HEALTH, pageType, page)
    }

    private fun checkVisibility(title: String, description: String): Boolean {
        return when (pageType) {
            AddOnTestPageType.LANDING -> {
                page.getByRole(
                    AriaRole.LINK,
                    Page.GetByRoleOptions().setName("$title\n$description")
                ).isVisible
            }

            AddOnTestPageType.HOW_IT_WORKS -> {
                page.getByText(title).isVisible && page.getByText(description).isVisible
            }

            AddOnTestPageType.WHAT_WE_TEST -> {
                page.getByText(title).first().isVisible
            }

            AddOnTestPageType.ALL_TESTS -> {
                page.getByRole(
                    AriaRole.LINK,
                    Page.GetByRoleOptions().setName("$title\n$description")
                ).isVisible
            }
        }
    }
}

enum class TestType(
    val testName: String,
    val shortDescription: String,
    val longDescription: String
) {
    ALLERGIES(
        "Allergies Test Panel ",
        "Checks for allergic responses and sensitivities",
        "A detailed IgE-based allergy screen measuring reactions to common foods, inhalants, contact triggers and medications. Learn more"
    ),
    GUT_MICROBIOME(
        "Advanced Gut Microbiome Analysis ",
        "Profiles gut microbes to reveal imbalances",
        "An advanced at-home stool test that analyses your gut bacteria, digestion, immunity and overall microbiome health for personalized insights. Learn more"
    ),
    STRESS_CORTISOL(
        "Stress and Cortisol Rhythm Panel ",
        "Tracks stress hormones throughout the day",
        "A non-invasive saliva test that maps your daily cortisol rhythm, revealing stress reactivity, recovery, and potential HPA axis imbalance. Learn more"
    ),
    GENETIC_ANALYSIS(
        "Advanced Genetic Analysis ",
        "Comprehensive screening for genetic traits and risks",
        "A cutting-edge cheek swab DNA test that reveals genetic predispositions across nutrition, fitness, mental health, disease risk and more. Learn more"
    ),
    OMEGA_PROFILE(
        "Omega Profile Panel ",
        "Examines fatty acid profiles and ratios",
        "An advanced fatty acid blood test that measures Omega-3, Omega-6, and lipid balance to evaluate inflammation, metabolism, and heart health. Learn more"
    ),
    TOXIC_METALS(
        "Toxic Metals Panel ",
        "Detects heavy metal exposure in the bloodstream",
        "An at-home test for detecting toxic and heavy metal exposure, safeguarding long-term health and organ function. Learn more"
    ),
    THYROID_HEALTH(
        "Thyroid Health Panel ",
        "Evaluates thyroid hormones and related issues",
        "A targeted blood test that measures thyroid hormones and ratios to assess metabolism, energy, and overall thyroid health. Learn more"
    ),
    WOMEN_HEALTH(
        "Women’s Health Panel ",
        "Assesses key factors for women's wellbeing",
        "An at-home hormone panel designed to provide insights into ovulation, and menstrual cycle health for women. Learn more"
    ),
    ESSENTIAL_NUTRIENTS(
        "Essential Nutrients Panel ",
        "Measures vital nutrient, vitamin, and mineral levels",
        "A focused blood test that measures key vitamins and minerals essential for energy, metabolism, and overall long-term health. Learn more"
    ),
    ADVANCED_THYROID(
        "Advanced Thyroid Panel ",
        "Detects autoimmune thyroid conditions",
        "A comprehensive at-home test for detecting autoimmune thyroid conditions, aiding in early diagnosis of thyroid disorders. Learn more"
    ),
    LIVER_HEALTH(
        "Liver Health Panel ",
        "Gauges liver enzymes and performance",
        "A focused blood test that evaluates liver enzymes, proteins, and bilirubin levels to assess liver function, detox capacity, and health. Learn more"
    ),
    AUTO_IMMUNE(
        "Autoimmune Panel ",
        "Identifies immune system disorder",
        "An at-home test that detects autoimmune activity by measuring antibodies associated with lupus and rheumatoid arthritis. Learn more"
    ),
    ADVANCED_HEART_HEALTH(
        "Advanced Heart Health Panel ",
        "Analyses indicators for heart health",
        "A comprehensive blood test that measures advanced cardiac biomarkers to assess heart health, inflammation, and long-term cardiovascular risk. Learn more"
    ),
    WOMEN_FERTILITY(
        "Women's Fertility Panel ",
        "Assess fertility readiness and reproductive health",
        "A complete at-home hormone and metabolic panel to assess fertility readiness and reproductive health. Learn more"
    ),
    BLOOD_HEALTH(
        "Blood Health Panel ",
        "Evaluates blood cell health and overall vitality",
        "Comprehensive blood panel evaluating red cells, hemoglobin, and platelets for oxygen transport, clotting, and overall vitality. Learn more"
    );

    fun getDescription(pageType: AddOnTestPageType): String {
        return if (pageType == AddOnTestPageType.ALL_TESTS) {
            longDescription
        } else {
            shortDescription
        }
    }
}

