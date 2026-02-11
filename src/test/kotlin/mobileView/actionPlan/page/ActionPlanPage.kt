package mobileView.actionPlan.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.model.FoodRecommendation
import mobileView.actionPlan.model.NutritionFoodType
import mobileView.actionPlan.model.NutritionRecommendationResponse
import mobileView.actionPlan.model.RecommendationData
import mobileView.actionPlan.utils.ActionPlanUtils.findSubCategoryExist
import mobileView.actionPlan.utils.ActionPlanUtils.getCategorySubtext
import mobileView.actionPlan.utils.ActionPlanUtils.ninetyPercent
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_RECOMMENDATION_DATA
import kotlin.collections.component1
import kotlin.collections.component2
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
    private var optimal = "Optimal"
    private var normal = "Normal"
    private var question = "question"

    init {
        //  monitorTraffic()
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

        val caloriesValues = "$apiCaloriesPercentage-$apiCalories Cals"
        val carbohydrateValues = "$apiCarbohydratePercentage-$apiCarbohydrate".plus("g")
        val proteinValues = "$apiProteinPercentage-$apiProtein".plus("g")
        val fatValues = "$apiFatPercentage-$apiFat".plus("g")
        val fiberValues = "$apiFiberPercentage-$apiFiber".plus("g")

        logger.info("API Daily Calories : ${caloriesValues}")
        logger.info("API Carbohydrate   : ${carbohydrateValues}")
        logger.info("API Protein        : ${proteinValues}")
        logger.info("API Fat            : ${fatValues}")
        logger.info("API Fiber          : ${fiberValues}")

        assertEquals(caloriesValues, uiDailyCalories)
        assertEquals(carbohydrateValues, uiCarbohydrate)
        assertEquals(proteinValues, uiProtein)
        assertEquals(fatValues, uiFat)
        assertEquals(fiberValues, uiFiber)
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

    fun nutritionMainCard() {
        val foodSectionLocators = listOf<Locator?>(
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Nutrition")),

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
        logger.info { "Clicking Food Eat section" }
        foodEatClick()

        val foodRecommendations = recommendationData?.food_recommendations
        logger.info { "Total food recommendations from API: ${foodRecommendations?.size}" }

        val eatList =
            foodRecommendations?.filter { it.suggestion.equals(NutritionFoodType.EAT.type, ignoreCase = true) }
                ?.groupBy { it.food?.category }

        val limitList =
            foodRecommendations?.filter { it.suggestion.equals(NutritionFoodType.LIMIT.type, ignoreCase = true) }
                ?.groupBy { it.food?.category }

        val avoidList =
            foodRecommendations?.filter { it.suggestion.equals(NutritionFoodType.AVOID.type, ignoreCase = true) }
                ?.groupBy { it.food?.category }

        logger.info { "Eat categories: ${eatList?.keys}" }
        logger.info { "Limit categories: ${limitList?.keys}" }
        logger.info { "Avoid categories: ${avoidList?.keys}" }

        val dialog = page.getByRole(AriaRole.DIALOG, Page.GetByRoleOptions().setName("Nutrition"))
        val searchBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search food item"))
        val eatTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Eat"))
        val limitTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Limit"))
        val avoidTab = page.getByRole(AriaRole.TAB, Page.GetByRoleOptions().setName("Avoid"))

        val titleAvoid = page.getByTestId("food-suggestion-section-avoid").getByTestId("food-suggestion-title")
        val titleLimit = page.getByTestId("food-suggestion-section-limit").getByTestId("food-suggestion-title")
        val titleEat = page.getByTestId("food-suggestion-section-eat").getByTestId("food-suggestion-title")

        val foodSectionLocators =
            listOf<Locator?>(dialog, searchBox, eatTab, limitTab, avoidTab, titleAvoid, titleLimit, titleEat)

        logger.info { "Waiting for Nutrition dialog UI elements" }
        foodSectionLocators.forEach { locator ->
            locator?.waitFor()
        }

        logger.info { "Starting food validation for EAT tab" }
        foodValidation(type = NutritionFoodType.EAT.type, eatList)
        foodValidation(type = NutritionFoodType.LIMIT.type, limitList)
        foodValidation(type = NutritionFoodType.AVOID.type, avoidList)
    }

    private fun foodValidation(type: String, foodList: Map<String?, List<FoodRecommendation>>?) {
        val parentId = when (type) {
            NutritionFoodType.EAT.type -> "food-suggestion-section-eat"
            NutritionFoodType.LIMIT.type -> "food-suggestion-section-limit"
            else -> "food-suggestion-section-avoid"
        }

        logger.info { "Validating food section: $type (parentId=$parentId)" }

        categoryValidation(parentId, type, foodList)
        foodNameValidation(type, foodList)

        if (type == NutritionFoodType.LIMIT.type || type == NutritionFoodType.AVOID.type) {
            foodToolTipsValidation(type, foodList)
        }

        logger.info { "Food validation completed for type: $type" }
    }

    private fun foodToolTipsValidation(
        type: String,
        foodList: Map<String?, List<FoodRecommendation>>?
    ) {
        logger.info { "Started food tool tips validation: $type" }
        foodList?.forEach { (category, recommendation) ->
            logger.info { "Validating food tool tips under category: $category (count=${recommendation.size})" }
            recommendation.forEach { foodRecommendation ->
                val food = foodRecommendation.food
                val impactBiomarkers = foodRecommendation.impact_biomarkers
                val hasBiomarker =
                    impactBiomarkers?.any { !it.inference.equals(optimal) && !it.inference.equals(normal) }

                if (hasBiomarker == true) {
                    val foodId = food?.food_id
                    logger.info { "Validating food item: id=$foodId" }

                    val foodElement = page.getByTestId("food-name-${foodId}")
                    foodElement.hover()

                    // optional: small delay so tooltip renders
                    page.waitForTimeout(300.0)
                    val toolTips = page.getByTestId("food-tooltip-${foodId}")

                    toolTips.scrollIntoViewIfNeeded()
                    toolTips.waitFor()

                    impactBiomarkers.forEachIndexed({ index, impactBiomarker ->
                        if (impactBiomarker.category == question) {
                            val impactBiomarkerElement = page.getByTestId("food-tooltip-reason-$foodId-$index").first()
                            val formattedReason = formatQuestionReason(
                                impactBiomarker.inference,
                                impactBiomarker.name,
                            )
                            if (!formattedReason.isNullOrBlank()) {
                                val context = if (type == NutritionFoodType.LIMIT.type) {
                                    "Limit this because"
                                } else {
                                    "Avoid this because"
                                }
                                val impactBiomarkerExpected = context.plus(" $formattedReason")

                                logger.info {
                                    "Food UI toolTips = '${impactBiomarkerElement.innerText()}', Expected = '$impactBiomarkerExpected'"
                                }

                                assertEquals(impactBiomarkerExpected, impactBiomarkerElement.innerText())
                            }
                        } else {
                            val impactBiomarkerElement =
                                page.getByTestId("food-tooltip-biomarker-$foodId-$index").first()
                            if (!impactBiomarker.inference.equals(optimal) && !impactBiomarker.inference.equals(normal)) {
                                val biomarkerName = impactBiomarker.name
                                val biomarkerInference = impactBiomarker.inference
                                val context = if (type == NutritionFoodType.LIMIT.type) {
                                    "Limit this because your"
                                } else {
                                    "Avoid this because your"
                                }

                                val impactBiomarkerExpected = context.plus(" $biomarkerName is $biomarkerInference")

                                logger.info {
                                    "Food UI toolTips = '${impactBiomarkerElement.innerText()}', Expected = '$impactBiomarkerExpected'"
                                }

                                assertEquals(impactBiomarkerExpected, impactBiomarkerElement.innerText())
                            }
                        }
                    })
                }
            }
        }
        logger.info { "Completed food tool tips validation: $type" }
    }


    private fun categoryValidation(parentId: String, type: String, foodList: Map<String?, List<FoodRecommendation>>?) {
        foodList?.forEach { (category, _) ->
            logger.info { "Validating category: $category" }
            val isCategoryExist = findSubCategoryExist(category)

            val categoryElement = page.getByTestId(parentId).getByTestId("food-category-title-${category}")
            var subCategoryElement: Locator? = null

            val elements = if (isCategoryExist) {
                subCategoryElement = page.getByTestId(parentId).getByTestId("food-category-subtext-${category}")
                listOf<Locator?>(categoryElement, subCategoryElement)
            } else {
                listOf<Locator?>(categoryElement)
            }

            elements.forEach {
                it?.scrollIntoViewIfNeeded()
                it?.waitFor()
            }

            val categoryUi = categoryElement.innerText()

            logger.info { "UI Category = '$categoryUi', Expected = '$category'" }

            assertEquals(category, categoryUi)

            if (isCategoryExist) {
                val subCategoryExpected = getCategorySubtext(type, category)
                val subCategoryUi = subCategoryElement?.innerText()
                logger.info { "UI SubCategory = '$subCategoryUi', Expected = '$subCategoryExpected'" }
                assertEquals(subCategoryExpected, subCategoryUi)
            }
        }
    }

    private fun foodNameValidation(
        type: String,
        foodList: Map<String?, List<FoodRecommendation>>?
    ) {
        logger.info { "Starting food name validation for type: $type" }

        foodList?.forEach { (category, recommendation) ->
            logger.info { "Validating food names under category: $category (count=${recommendation.size})" }

            recommendation.forEach { foodRecommendation ->
                val food = foodRecommendation.food
                val foodId = food?.food_id

                logger.info { "Validating food item: id=$foodId" }

                //TODO need to check icons
                val foodElement = page.getByTestId("food-name-${foodId}")
                val foodIcon = page.getByTestId("food-config-icon-${foodId}")
                val foodImage = page.getByTestId("food-icon-${foodId}")

                foodElement.scrollIntoViewIfNeeded()

                listOf(foodElement, foodIcon, foodImage).forEach {
                    it.waitFor()
                }

                val foodNameUi = foodElement.innerText()
                val foodNameExpected = food?.name

                logger.info {
                    "Food UI name = '$foodNameUi', Expected = '$foodNameExpected'"
                }

                assertEquals(foodNameExpected, foodNameUi)
            }
        }

        logger.info { "Food name validation completed for type: $type" }
    }


    private fun foodEatClick() {
        val foodToEat = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Food to Eat"))
        foodToEat.waitFor()
        foodToEat.click()
    }

    fun formatQuestionReason(
        inference: String?,
        name: String?
    ): String? {

        if (name.isNullOrBlank() || name.trim().equals("none", ignoreCase = true)) {
            return null
        }

        val question = inference?.trim().orEmpty()
        val answer = name.trim()

        return when {
            question.uppercase().contains("INTOLERANCES") -> {
                "you are intolerant to $answer"
            }

            question.uppercase().contains("FOOD ALLERGIES") ||
                    question.uppercase().contains("ALLERGIES") -> {
                "you are allergic to $answer"
            }

            question.uppercase().contains("SKIN CONDITIONS") -> {
                "you currently have, or have had, $answer in the past"
            }

            else -> {
                "you have $answer"
            }
        }
    }


}