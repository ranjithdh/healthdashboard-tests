package mobileView.baseline

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import model.baseline.BaselineScoreDetailResponse
import model.healthdata.Range
import utils.DateHelper
import utils.logger.logger
import utils.report.StepHelper
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt


class BaselineScoreDetailPage(page: Page) : BasePage(page) {

    override val pageUrl: String = TestConfig.Urls.BASELINE_SCORE_URL

    private var baselineScoreDetailResponse: BaselineScoreDetailResponse? = null

    fun saveBaseLineScoreDetails(details: BaselineScoreDetailResponse?) {
        baselineScoreDetailResponse = details
    }

    fun getBaselineScoreDetails(): BaselineScoreDetailResponse? {
        return baselineScoreDetailResponse
    }

    fun waitForPageLoad(): BaselineScoreDetailPage {
        page.waitForURL(TestConfig.Urls.BASELINE_SCORE_URL)
        return this
    }

    fun isBaselineScoreTitleVisible(): Boolean {
        StepHelper.step("Verify Baseline Score title is visible")
        val locator = page.locator("h1").filter(Locator.FilterOptions().setHasText("Baseline Score")).first()
        locator.waitFor()
        return locator.isVisible
    }

    fun isBetaTagVisible(): Boolean {
        StepHelper.step("Verify Beta tag is visible")
        return page.getByText("Beta").isVisible
    }

    fun isLastUpdatedTimeVisible(): Boolean {
        StepHelper.step("Verify last updated time is visible")
        val lastUpdatedTime = baselineScoreDetailResponse?.data?.trend_history?.last()
        val localTime = DateHelper.utcToLocalDateTime(lastUpdatedTime?.calculated_at)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)
        logger.info { "lastUpdatedTime...${dateTimeFormatter.format(localTime)}" }

        return page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Last updated: $dateTimeFormatter")).isVisible
    }


    fun isBaseLineScoreIsMatching(): Boolean {
        StepHelper.step("Verify baseline score value matches API response")
        val score = baselineScoreDetailResponse?.data?.score_details?.normalized_baseline_score?.roundToInt().toString()
        return page.locator("span").filter(FilterOptions().setHasText(score)).isVisible &&
                page.getByText("of 100").isVisible
    }

    fun isBaseLineScoreStatusMatching(): Boolean {
        StepHelper.step("Verify baseline score status matches API response")
        val status = findScoreRange(
            normalizedScore = baselineScoreDetailResponse?.data?.score_details?.normalized_baseline_score ?: 0.0,
            ranges = baselineScoreDetailResponse?.data?.score_details?.ranges ?: emptyList()
        )

        return page.getByText(status?.display_rating).first().isVisible
    }

    fun isScoreRangeElementsVisible(): Boolean {
        StepHelper.step("Verify score range elements are visible")
        return page.getByText("Score Range").isVisible
                && page.getByText("50").isVisible
                && page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText(Pattern.compile("^65$"))).isVisible
                && page.getByText("75").isVisible
                && page.getByText("85", Page.GetByTextOptions().setExact(true)).first().isVisible
    }

    fun isBaselineScoreDescriptionMatching(): Boolean {
        StepHelper.step("Verify baseline score description matches API response")
        return page.getByText(baselineScoreDetailResponse?.data?.score_details?.baseline_score_description).isVisible
    }


    fun isWhatNeedsAttentionTitleAndDescriptionVisible(): Boolean {
        StepHelper.step(StepHelper.VERIFY_SECTION + "What Needs Attention")
        StepHelper.step("Verify 'What needs attention' heading is visible")
        StepHelper.step("Verify negative contributors description text is visible")
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What needs attention")).isVisible
                && page.getByText("Biomarkers contributing negatively to your score, listed by priority.").isVisible
    }

    fun clickViewAllNegativeContributors() {
        StepHelper.step("Click 'View All' for negative contributors")
        page.locator("#nagative_contributors")
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("View All")).click()
    }

    fun clickViewLessNegativeContributors() {
        StepHelper.step("Click 'View Less' for negative contributors")
        page.locator("#nagative_contributors")
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("View Less")).click()
    }

    fun clickViewAllPositiveContributors() {
        StepHelper.step("Click 'View All' for positive contributors")
        page.locator("#positive_contributors")
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("View All")).click()
    }

    fun clickViewLessPositiveContributors() {
        StepHelper.step("Click 'View Less' for positive contributors")
        page.locator("#positive_contributors")
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("View Less")).click()
    }

    fun isContributorDetailsVisible(
        displayName: String,
        currentValue: String,
        unit: String,
        inference: String
    ): Boolean {
        StepHelper.step("Verify contributor details: $displayName")
        val formattedValue = formatContributorValue(currentValue)

        var isVisible = page.getByText(displayName, Page.GetByTextOptions().setExact(true)).first().isVisible &&
                page.getByText(formattedValue, Page.GetByTextOptions().setExact(true)).first().isVisible &&
                page.getByText(inference, Page.GetByTextOptions().setExact(true)).first().isVisible

        if (unit.isNotEmpty()) {
            isVisible = isVisible && page.getByText(unit, Page.GetByTextOptions().setExact(true)).first().isVisible
        }

        return isVisible
    }

    private fun formatContributorValue(value: String): String {
        return try {
            val doubleValue = value.toDouble()
            if (doubleValue == doubleValue.toInt().toDouble()) {
                doubleValue.toInt().toString()
            } else {
                value
            }
        } catch (e: Exception) {
            value
        }
    }


    fun findScoreRange(
        normalizedScore: Double,
        ranges: List<Range>
    ): Range? {

        return ranges.firstOrNull { scoreRange ->
            val (minStr, maxStr) = scoreRange.range?.split("-") ?: return@firstOrNull false
            val min = minStr.toDouble()
            val max = maxStr.toDouble()

            normalizedScore in min..max
        }
    }


    fun verifyWhatIsBaselineScoreSection(): Boolean {
        StepHelper.step(StepHelper.VERIFY_SECTION + "What is Baseline Score")
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What is Baseline Score")).isVisible
                && page.getByText("The Baseline Score is Deep Holistics’ proprietary health score, developed through clinical research, systems-based analysis, and years of preventive health insight.").isVisible
                && page.getByText("Instead of looking at individual markers in isolation, it brings together data across key body systems to reflect how your body is actually functioning today.").isVisible
                && page.getByText("This allows you to move beyond “normal” ranges and focus on what needs attention now.").isVisible
    }


    fun verifyBiologicalAgeSection(): Boolean {
        StepHelper.step(StepHelper.VERIFY_SECTION + "Biological Age")
        val age = baselineScoreDetailResponse?.data?.age
        val biologicalAge = baselineScoreDetailResponse?.data?.biological_age

        var description = ""

        if (age != null && biologicalAge != null) {
            if (age > biologicalAge) {
                val younger = age - biologicalAge
                description = "$younger years younger than your actual age"
            } else {
                biologicalAge - age
                description = "$biologicalAge years older than your actual age"
            }
        }

        logger.info { "biologicalAge.....$description" }

        return page.getByText("YOUR BIOLOGICAL AGE").isVisible
                && page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(biologicalAge.toString())).isVisible
                && page.getByText(description).isVisible
                && page.getByText("How is Baseline Score different from Biological Age?").isVisible
                && page.getByText("The Baseline Score offers a more powerful approach to health optimisation than biological age calculators like PhenoAge. While biological age compares you to population statistics and uses only nine blood markers focused on predicting mortality, the Baseline Score tracks you against yourself and incorporates comprehensive data across metabolic health, hormonal balance, nutritional status, inflammation, and physical performance.").isVisible

    }


    fun isDisclaimerVisible(): Boolean {
        StepHelper.step("Verify disclaimer text is visible")
        return page.getByText("Disclaimer:").isVisible
                && page.getByText(" These insights are informational only and not a substitute for medical advice. Please consult a qualified healthcare professional before taking any clinical or significant lifestyle actions.").isVisible
    }

}