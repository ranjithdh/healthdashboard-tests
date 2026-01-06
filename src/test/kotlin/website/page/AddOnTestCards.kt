package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

enum class AddOnTestPageType {
    LANDING,
    HOW_IT_WORKS,
    WHAT_WE_TEST
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
        }
    }

    fun getAddOnTestHeader(): Locator? {
        return when (pageType) {
            AddOnTestPageType.LANDING -> {
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("O p t i o n a l a d d - o n s f o r d e e p e r i n s i g h t"))
            }

            AddOnTestPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("O p t i o n a l a d d - o n s f o r d e e p e r i n s i g h t"))
            }

            AddOnTestPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("P l u s a d d - o n d i a g n o s t i c t e s t i n g a v a i l a b l e"))
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
        }

        return description.isVisible
    }


    fun isViewAllAddOnTestButtonVisible(): Boolean {
        return getViewAllAddOnTestButton()?.isVisible ?: false
    }

    fun clickViewAllAddOnTestButton(): AllTestPage {
        getViewAllAddOnTestButton()?.click()
        val allTestPage = AllTestPage(page)
        allTestPage.waitForPageLoad()
        return allTestPage
    }

    fun isAllergyVisible(): Boolean {
        return checkVisibility(
            "Allergies",
            "Checks for allergic responses and sensitivities"
        )
    }

    fun isGutMicrobiomeVisible(): Boolean {
        return checkVisibility(
            "Gut Microbiome",
            "Profiles gut microbes to reveal imbalances"
        )
    }

    fun isStressAndCortisolVisible(): Boolean {
        return checkVisibility(
            "Stress and Cortisol",
            "Tracks stress hormones throughout the day"
        )
    }

    fun isGeneVisible(): Boolean {
        return checkVisibility(
            "Genetic Analysis",
            "Comprehensive screening for genetic traits and risks"
        )
    }

    fun isOmegaVisible(): Boolean {
        return checkVisibility(
            "Omega Profile",
            "Examines fatty acid profiles and ratios"
        )
    }

    fun isToxicMetalsVisible(): Boolean {
        return checkVisibility(
            "Toxic Metals",
            "Detects heavy metal exposure in the bloodstream"
        )
    }

    fun isThyroidHealthVisible(): Boolean {
        return checkVisibility(
            "Thyroid Health",
            "Evaluates thyroid hormones and related issues"
        )
    }

    fun isWomensHealthVisible(): Boolean {
        return checkVisibility(
            "Women’s Health",
            "Assesses key factors for women’s wellbeing"
        )
    }

    fun isEssentialNutrientsVisible(): Boolean {
        return checkVisibility(
            "Essential Nutrients",
            "Measures vital nutrient, vitamin, and mineral levels"
        )
    }

    fun isAdvancedThyroidVisible(): Boolean {
        return checkVisibility(
            "Advanced Thyroid",
            "Detects autoimmune thyroid conditions"
        )
    }

    fun isLiverHealthVisible(): Boolean {
        return checkVisibility(
            "Liver Health",
            "Gauges liver enzymes and performance"
        )
    }

    fun isAutoImmuneVisible(): Boolean {
        return checkVisibility(
            "AutoImmune",
            "Identifies immune system disorder"
        )
    }

    fun isAdvancedHeartHealthVisible(): Boolean {
        return checkVisibility(
            "Advanced Heart Health",
            "Analyses indicators for heart health"
        )
    }

    fun isWomensFertilityVisible(): Boolean {
        return checkVisibility(
            "Women's Fertility",
            "Assess fertility readiness and reproductive health"
        )
    }

    fun isBloodHealthVisible(): Boolean {
        return checkVisibility(
            "Blood Health",
            "Evaluates blood cell health and overall vitality"
        )
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
        }
    }
}
