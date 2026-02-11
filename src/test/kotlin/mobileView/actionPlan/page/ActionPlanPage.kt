package mobileView.actionPlan.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.model.NutritionRecommendationResponse
import mobileView.actionPlan.model.RecommendationData
import mobileView.actionPlan.utils.ActionPlanUtils.ninetyPercent
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_RECOMMENDATION_DATA
import kotlin.test.assertEquals

class ActionPlanPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.RECOMMENDATIONS_URL

    private val actionPlanTitle: Locator =
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Action Plan"))

    fun waitForConfirmation(): ActionPlanPage {
        page.waitForURL(TestConfig.Urls.RECOMMENDATIONS_URL)
        actionPlanTitle.waitFor()
        return this
    }

    private var recommendationData: RecommendationData? = null

    init {
        monitorTraffic()
    }

    private fun monitorTraffic() {
        captureRecommendationData()
    }

    fun captureRecommendationData() {
        StepHelper.step(FETCH_RECOMMENDATION_DATA)
        try {
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()?.contains(TestConfig.APIs.API_RECOMMENDATION) == true &&
                            response.request().method() == "GET"
                }, {

                }
            )

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.info { "API response body is empty" }
                return
            }

            //logger.info { "API response...${responseBody}" }

            val responseObj = json.decodeFromString<NutritionRecommendationResponse>(responseBody)

            if (responseObj.data != null) {
                recommendationData = responseObj.data
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response or API call failed..${e.message}" }
        }
    }

    fun dailyCaloriesIntakeCard() {
        dailyCaloriesFieldCheck()
        dailyCaloriesValidation()
    }

    private fun dailyCaloriesValidation() {
        val nutritionProfile = recommendationData?.nutrient_profile

        val dailyCalorieValue = page.getByTestId("daily-calorie-value")
        val carbohydrateValue = page.getByTestId("nutrient-value-carbohydrate")
        val proteinValue = page.getByTestId("nutrient-value-protein")
        val fatValue = page.getByTestId("nutrient-value-fat")
        val fiberValue = page.getByTestId("nutrient-value-fiber")

        val uiDailyCalories = dailyCalorieValue.innerText()
        val uiCarbohydrate = carbohydrateValue.innerText()
        val uiProtein = proteinValue.innerText()
        val uiFat = fatValue.innerText()
        val uiFiber = fiberValue.innerText()

        logger.info("UI Daily Calories : $uiDailyCalories")
        logger.info("UI Carbohydrate   : $uiCarbohydrate")
        logger.info("UI Protein        : $uiProtein")
        logger.info("UI Fat            : $uiFat")
        logger.info("UI Fiber          : $uiFiber")

        val apiCalories = nutritionProfile?.calories
        val apiCarbohydrate = nutritionProfile?.carbohydrate
        val apiProtein = nutritionProfile?.protein
        val apiFat = nutritionProfile?.fat
        val apiFiber = nutritionProfile?.fiber

        val apiCaloriesPercentage = ninetyPercent(apiCalories?.toDouble() ?: 0.0)
        val apiCarbohydratePercentage = ninetyPercent(apiCarbohydrate?.toDouble() ?: 0.0)
        val apiProteinPercentage = ninetyPercent(apiProtein?.toDouble() ?: 0.0)
        val apiFatPercentage = ninetyPercent(apiFat?.toDouble() ?: 0.0)
        val apiFiberPercentage = ninetyPercent(apiFiber?.toDouble() ?: 0.0)

        val caloriesValues="$apiCaloriesPercentage-$apiCalories Cals"
        val carbohydrateValues="$apiCarbohydratePercentage-$apiCarbohydrate".plus("g")
        val proteinValues="$apiProteinPercentage-$apiProtein".plus("g")
        val fatValues="$apiFatPercentage-$apiFat".plus("g")
        val fiberValues="$apiFiberPercentage-$apiFiber".plus("g")

        logger.info("API Daily Calories : ${caloriesValues}")
        logger.info("API Carbohydrate   : ${carbohydrateValues}")
        logger.info("API Protein        : ${proteinValues}")
        logger.info("API Fat            : ${fatValues}")
        logger.info("API Fiber          : ${fiberValues}")

        assertEquals(caloriesValues,uiDailyCalories)
        assertEquals(carbohydrateValues,uiCarbohydrate)
        assertEquals(proteinValues,uiProtein)
        assertEquals(fatValues,uiFat)
        assertEquals(fiberValues,uiFiber)
    }


    private fun dailyCaloriesFieldCheck() {
        val nutritionTestIds = listOf(
            "daily-calorie-title",
            "daily-calorie-value",
            "nutrient-label-carbohydrate",
            "nutrient-value-carbohydrate",
            "nutrient-label-protein",
            "nutrient-value-protein",
            "nutrient-label-fat",
            "nutrient-value-fat",
            "nutrient-label-fiber",
            "nutrient-value-fiber"
        )

        nutritionTestIds.forEach { testId ->
            page.getByTestId(testId).waitFor()
        }

    }

    fun nutritionCard() {
        val foodSectionLocators = listOf<Locator?>(
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Food to Eat")),
            page.getByText("You can have these foods regularly in significant quantities."),

            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Food to Limit")),
            page.getByText("You can have these foods 1–2"),

            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Food to Avoid")),
            page.getByText("These can be consumed")
        )

        foodSectionLocators.forEach { locator -> locator?.waitFor() }
    }

    fun whatToEat() {

    }


}