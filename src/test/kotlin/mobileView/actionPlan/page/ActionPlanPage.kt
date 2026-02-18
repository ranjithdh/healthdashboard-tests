package mobileView.actionPlan.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.model.*
import mobileView.actionPlan.utils.ActionPlanUtils.findSubCategoryExist
import mobileView.actionPlan.utils.ActionPlanUtils.formatConsultationDate
import mobileView.actionPlan.utils.ActionPlanUtils.formatNumber
import mobileView.actionPlan.utils.ActionPlanUtils.getCategorySubtext
import mobileView.actionPlan.utils.ActionPlanUtils.isTestBooked
import mobileView.actionPlan.utils.ActionPlanUtils.ninetyPercent
import mobileView.actionPlan.utils.ActionPlanUtils.normalizeForUiCompare
import mobileView.actionPlan.utils.ActionPlanUtils.splitByNewLine
import mobileView.diagnostics.TestDetailPage
import model.healthdata.HealthData
import model.home.HomeData
import model.home.HomeDataResponse
import utils.Normalize.refactorTimeZone
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.FETCH_GOAL_DATA
import utils.report.StepHelper.FETCH_HEALTH_DATA
import utils.report.StepHelper.FETCH_HOME_DATA
import utils.report.StepHelper.FETCH_LAB_TEST_DATA
import utils.report.StepHelper.FETCH_RECOMMENDATION_DATA
import utils.report.StepHelper.OPENING_ACTIVITY_PANEL
import utils.report.StepHelper.OPENING_SLEEP_PANEL
import utils.report.StepHelper.OPENING_STRESS_PANEL
import utils.report.StepHelper.OPENING_SUPPLEMENTS_PANEL
import utils.report.StepHelper.VALIDATING_ACTIVITY_RECOMMENDATIONS
import utils.report.StepHelper.VALIDATING_DAILY_CALORIES_CARD
import utils.report.StepHelper.VALIDATING_EMPTY_ACTION_PLAN_PAGE
import utils.report.StepHelper.VALIDATING_FOOD_RECOMMENDATIONS
import utils.report.StepHelper.VALIDATING_FURTHER_TESTS
import utils.report.StepHelper.VALIDATING_NUTRITION_MAIN_CARD
import utils.report.StepHelper.VALIDATING_SEARCH_FOOD_ITEMS
import utils.report.StepHelper.VALIDATING_SLEEP_RECOMMENDATIONS
import utils.report.StepHelper.VALIDATING_STRESS_RECOMMENDATIONS
import utils.report.StepHelper.VALIDATING_SUPPLEMENTS
import utils.report.StepHelper.logApiResponse
import kotlin.test.assertEquals

class ActionPlanPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.RECOMMENDATIONS_URL

    private val actionPlanTitle: Locator =
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Action Plan").setExact(true))


    fun waitForConfirmation(): ActionPlanPage {
        page.waitForURL(TestConfig.Urls.RECOMMENDATIONS_URL)
        actionPlanTitle.waitFor()
        return this
    }

    private var recommendationData: RecommendationData? = null
    private var healthData: HealthData? = null
    private var homeData: HomeData? = HomeData()
    private var programGoalData: ProgramGoalData? = null

    private var optimal = "Optimal"
    private var normal = "Normal"
    private var question = "question"
    private var supplementsDisclaimer =
        "Disclaimer: The supplement recommendations shared are based on the nutrient form, dosage, and intended therapeutic benefit. You are free to choose any trusted and good-quality brand available to you, ensuring that it matches the specified ingredient and dosage. Please consult your healthcare provider before purchasing, especially if you have concerns regarding allergies, specific ingredients, or medication interactions. The brand names are only indicative and not mandatory."

    var labTestData: RecommendationLabTest? = null
    var allTests = listOf<RecommendationLabTestPackage>()

    fun captureRecommendationData() {

        if (recommendationData === null) {
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

                val responseObj = json.decodeFromString<NutritionRecommendationResponse>(responseBody)

                if (responseObj.data != null) {
                    recommendationData = responseObj.data
                    logApiResponse(TestConfig.APIs.API_RECOMMENDATION, responseObj)
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

    fun getLabTestsData() {
        StepHelper.step(FETCH_LAB_TEST_DATA)
        try {
            val timeZone = java.util.TimeZone.getDefault().id

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.LAB_TEST_API_URL,
                RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", refactorTimeZone(timeZone))
            )


            if (response.status() != 200) {
                logger.error { "API returned error status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            val responseObj = json.decodeFromString<RecommendationLabTest>(responseBody)

            if (responseObj.status == "success") {
                labTestData = responseObj
                val productList = labTestData?.data?.diagnostic_product_list

                val packages = productList?.packages ?: emptyList()
                val testProfiles = productList?.test_profiles ?: emptyList()
                val tests = productList?.tests ?: emptyList()

                allTests = (packages + testProfiles + tests)
                logApiResponse(TestConfig.APIs.LAB_TEST_API_URL, responseObj)
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch lab tests data: ${e.message}" }
        }
    }

    fun getHealthData() {
        StepHelper.step(FETCH_HEALTH_DATA)
        try {
            val timeZone = java.util.TimeZone.getDefault().id

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.HEALTH_DATA,
                RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", refactorTimeZone(timeZone))
            )


            if (response.status() != 200) {
                logger.error { "API returned error status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            val responseData = json.decodeFromString<HealthData>(responseBody)

            if (responseData.status == "success") {
                healthData = responseData
                logApiResponse(TestConfig.APIs.HEALTH_DATA, responseData)
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch health data: ${e.message}" }
        }
    }

    fun getHomeData() {
        StepHelper.step(FETCH_HOME_DATA)
        try {
            val timeZone = java.util.TimeZone.getDefault().id

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.API_HOME,
                RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", refactorTimeZone(timeZone))
            )


            if (response.status() != 200) {
                logger.error { "API returned error status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            val responseData = json.decodeFromString<HomeDataResponse>(responseBody)

            if (responseData.status == "success") {
                homeData = responseData.data
                logApiResponse(TestConfig.APIs.API_HOME, responseData)
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch home data: ${e.message}" }
        }
    }

    fun getGoalData() {
        StepHelper.step(FETCH_GOAL_DATA)
        try {
            val timeZone = java.util.TimeZone.getDefault().id

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.API_GOAL,
                RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", refactorTimeZone(timeZone))
            )


            if (response.status() != 200) {
                logger.error { "API returned error status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            val responseData = json.decodeFromString<ProgramGoalResponse>(responseBody)

            if (responseData.status == "success") {
                programGoalData = responseData.data
                logApiResponse(TestConfig.APIs.API_GOAL, responseData)
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch goal data: ${e.message}" }
        }
    }


    /**-----------------Nutrition-----------------------*/

    fun dailyCaloriesIntakeCard() {
        StepHelper.step(VALIDATING_DAILY_CALORIES_CARD)
        val nutritionProfile = recommendationData?.nutrient_profile
        nutritionProfile?.let {
            dailyCaloriesFieldCheck()
            dailyCaloriesValidation()
        }
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

        logger.info { "UI Daily Calories : $uiDailyCalories" }
        logger.info { "UI Carbohydrate   : $uiCarbohydrate" }
        logger.info { "UI Protein        : $uiProtein" }
        logger.info { "UI Fat            : $uiFat" }
        logger.info { "UI Fiber          : $uiFiber" }

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

        logger.info { "API Daily Calories : $caloriesValues" }
        logger.info { "API Carbohydrate   : $carbohydrateValues" }
        logger.info { "API Protein        : $proteinValues" }
        logger.info { "API Fat            : $fatValues" }
        logger.info { "API Fiber          : $fiberValues" }

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
        StepHelper.step(VALIDATING_NUTRITION_MAIN_CARD)
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
        StepHelper.step(VALIDATING_FOOD_RECOMMENDATIONS)
        logger.info { "Clicking Food Eat section" }
        val foodRecommendations = recommendationData?.food_recommendations

        foodRecommendations?.let {
            nutritionMainCard()
            foodEatClick()


            logger.info { "Total food recommendations from API: ${foodRecommendations.size}" }

            val eatList =
                foodRecommendations.filter { it.suggestion.equals(NutritionFoodType.EAT.type, ignoreCase = true) }
                    .groupBy { it.food?.category }

            val limitList =
                foodRecommendations.filter { it.suggestion.equals(NutritionFoodType.LIMIT.type, ignoreCase = true) }
                    .groupBy { it.food?.category }

            val avoidList =
                foodRecommendations.filter { it.suggestion.equals(NutritionFoodType.AVOID.type, ignoreCase = true) }
                    .groupBy { it.food?.category }

            logger.info { "Eat categories: ${eatList.keys}" }
            logger.info { "Limit categories: ${limitList.keys}" }
            logger.info { "Avoid categories: ${avoidList.keys}" }

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
            if (eatList.isNotEmpty()) {
                foodValidation(type = NutritionFoodType.EAT.type, eatList)
            }
            if (limitList.isNotEmpty()) {
                foodValidation(type = NutritionFoodType.LIMIT.type, limitList)
            }
            if (avoidList.isNotEmpty()) {
                foodValidation(type = NutritionFoodType.AVOID.type, avoidList)
            }
        }
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

    fun searchValidation() {
        val foodRecommendations = recommendationData?.food_recommendations

        foodRecommendations?.let {

            StepHelper.step(VALIDATING_SEARCH_FOOD_ITEMS)
            logger.info { "Starting search validation" }

            val searchBox =
                page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search food item"))

            // First search with junk value to validate empty state
            searchBox.fill("abcd123")

            val titleAvoid = page.getByTestId("food-suggestion-section-avoid")
                .getByTestId("food-suggestion-title")
            val titleLimit = page.getByTestId("food-suggestion-section-limit")
                .getByTestId("food-suggestion-title")
            val titleEat = page.getByTestId("food-suggestion-section-eat")
                .getByTestId("food-suggestion-title")

            val emptyEat = page.getByText("No foods found in Eat")
            val emptyLimit = page.getByText("No foods found in Limit")
            val emptyAvoid = page.getByText("No foods found in Avoid")

            val foodSectionLocators = listOf(
                searchBox,
                emptyEat,
                emptyLimit,
                emptyAvoid,
                titleAvoid,
                titleLimit,
                titleEat
            )

            foodSectionLocators.forEach {
                it.waitFor()
            }

            logger.info { "Empty state validated successfully" }

            if (foodRecommendations.isEmpty()) {
                logger.warn { "No food recommendations available" }
                return
            }

            validateSearchForType(foodRecommendations, NutritionFoodType.EAT.type, searchBox)
            validateSearchForType(foodRecommendations, NutritionFoodType.LIMIT.type, searchBox)
            validateSearchForType(foodRecommendations, NutritionFoodType.AVOID.type, searchBox)

            logger.info { "Search validation completed" }
        }
    }

    private fun validateSearchForType(
        foodRecommendations: List<FoodRecommendation>,
        type: String,
        searchBox: Locator
    ) {
        val filteredList =
            foodRecommendations
                .filter { it.suggestion.equals(type, ignoreCase = true) }
                .groupBy { it.food?.category }

        if (filteredList.isEmpty()) {
            logger.warn { "No food items found for type=$type" }
            return
        }

        val singleItem = filteredList.entries.firstOrNull()
        if (singleItem == null || singleItem.value.isEmpty()) {
            logger.warn { "No valid category/item found for type=$type" }
            return
        }

        val singleValue = singleItem.value.firstOrNull()
        if (singleValue?.food?.name.isNullOrBlank()) {
            logger.warn { "Food name is null/blank for type=$type" }
            return
        }

        logger.info {
            "Validating search for type=$type, category=${singleItem.key}, food=${singleValue?.food?.name}"
        }

        searchBox.fill(singleValue?.food?.name)

        val tempMap = mapOf(singleItem.key to listOf(singleValue!!))

        foodValidation(type = type, foodList = tempMap)

        logger.info { "Search validation passed for type=$type" }
    }


    /**-----------------Activity-----------------------*/

    fun activityMainCards() {
        StepHelper.step(VALIDATING_ACTIVITY_RECOMMENDATIONS)
        logger.info { "Fetching activity recommendations from data" }

        val activityList = recommendationData?.recommendations?.filter { it.category == ActionPlanType.ACTIVITY.type }

        logger.info("Filtered activity list size: ${activityList?.size ?: 0}")

        if (activityList?.isNotEmpty() == true) {
            logger.info("Activity list is not empty, waiting for Exercise heading")

            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Exercise")).waitFor()

            validatingActivityMainCards(activityList)

            validatingActivitySidePanel(activityList)
        } else {
            logger.warn("No activity recommendations found")
        }
    }

    private fun validatingActivitySidePanel(activityList: List<Recommendation>) {
        logger.info { "Starting activity side panel validation for ${activityList.size} activities" }

        activityList.forEach { activity ->
            StepHelper.step("${OPENING_ACTIVITY_PANEL}: ${activity.display_name}")
            logger.info { "Opening activity panel for activityId=${activity.id}" }

            val descriptiveMeta = activity.descriptive_meta
            val descriptionExpected = descriptiveMeta?.description

            val title = page.getByTestId("exercise-title-${activity.id}")
            title.click()

            val dialog = page.getByRole(AriaRole.DIALOG)
            dialog.waitFor()

            logger.info { "Activity dialog opened for activityId=${activity.id}" }

            activityHeaderSection(activity)

            descriptionSection(descriptionExpected, ActionPlanType.ACTIVITY.type)

            potentialBiomarker(activity, ActionPlanType.ACTIVITY.type)

            validatingViewMore(activity)

            logger.info { "Clicked View More for activityId=${activity.id}" }

            whyItWorks(activity, ActionPlanType.ACTIVITY.type)
            intoPractice(activity, ActionPlanType.ACTIVITY.type)
            whatToExpect(activity, ActionPlanType.ACTIVITY.type)

            val closePanel = page.getByTestId("exercise-panel-close")
            closePanel.waitFor()
            closePanel.click()

            logger.info { "Closed activity panel for activityId=${activity.id}" }
        }

        logger.info { "Completed activity side panel validation" }
    }


    private fun potentialBiomarker(activity: Recommendation, type: String) {
        logger.info { "Validating Potential Biomarker section for activityId=${activity.id}" }

        val down = "down"
        val metricRecommendations = activity.metric_recommendations

        val header = page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Potential biomarker impact")
        )
        header.waitFor()
        header.click()

        if (type == ActionPlanType.STRESS.type || type == ActionPlanType.SLEEP.type) {
            val descriptiveMeta = activity.descriptive_meta
            val potentialBiomarkerImpact = descriptiveMeta?.potential_biomarker_impact
            potentialBiomarkerImpact?.let { biomarkerImpact ->
                val biomarkerImpactUiElement = if (type == ActionPlanType.SLEEP.type) {
                    page.getByTestId("sleep-potential-biomarker-impact")
                } else {
                    page.getByTestId("stress-potential-biomarker-impact")
                }
                biomarkerImpactUiElement.waitFor()
                val expected = biomarkerImpact.normalizeForUiCompare()
                val actual = biomarkerImpactUiElement.innerText().normalizeForUiCompare()
                logger.info { "stress expected:$expected\nactual:$actual" }
                assertEquals(expected, actual)
            }
        }

        metricRecommendations?.forEachIndexed { index, recommendations ->
            val metric = recommendations.metric

            val biomarkerNameExpected = metric?.display_name
            val trendArrow = metric?.trend_arrow

            logger.info {
                "Validating biomarker index=$index, name=$biomarkerNameExpected, trend=$trendArrow"
            }

            val arrowUi = if (trendArrow == down) {
                when (type) {
                    ActionPlanType.ACTIVITY.type -> page.getByTestId("exercise-impact-down-$index")
                    ActionPlanType.STRESS.type -> page.getByTestId("stress-impact-down-$index")
                    else -> page.getByTestId("sleep-impact-down-$index")
                }
            } else {
                when (type) {
                    ActionPlanType.ACTIVITY.type -> page.getByTestId("exercise-impact-up-$index")
                    ActionPlanType.STRESS.type -> page.getByTestId("stress-impact-up-$index")
                    else -> page.getByTestId("sleep-impact-up-$index")
                }
            }

            arrowUi.waitFor()


            val biomarkerUi = when (type) {
                ActionPlanType.ACTIVITY.type -> page.getByTestId("exercise-impact-$index")
                ActionPlanType.STRESS.type -> page.getByTestId("stress-impact-$index")
                else -> page.getByTestId("sleep-impact-$index")
            }

            biomarkerUi.waitFor()

            assertEquals(biomarkerNameExpected, biomarkerUi.innerText())
        }

        logger.info { "Potential Biomarker section validated for activityId=${activity.id}" }
    }


    private fun whyItWorks(activity: Recommendation, type: String) {
        logger.info { "Validating Why It Works section for activityId=${activity.id}" }

        val descriptiveMeta = activity.descriptive_meta
        val whyItWorks = descriptiveMeta?.why_it_works
        val workList = splitByNewLine(whyItWorks)

        if (workList.isNotEmpty()) {
            val heading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Why it works"))
            heading.waitFor()
            heading.click()

            workList.forEachIndexed { index, work ->
                logger.info { "Validating Why It Works item index=$index, value=$work" }

                val whyItWorkElement = when (type) {
                    ActionPlanType.ACTIVITY.type -> page.getByTestId("exercise-why-it-works-$index")
                    ActionPlanType.STRESS.type -> page.getByTestId("stress-why-it-works-$index")
                    else -> page.getByTestId("sleep-why-it-works-$index")
                }
                whyItWorkElement.waitFor()

                assertEquals(work, whyItWorkElement.innerText())
            }
        } else {
            logger.warn { "Why It Works list is empty for activityId=${activity.id}" }
        }
    }


    private fun intoPractice(activity: Recommendation, type: String) {
        logger.info { "Validating How To Practice section for activityId=${activity.id}" }

        val descriptiveMeta = activity.descriptive_meta
        val howToPractice = descriptiveMeta?.how_to_practice
        val practiceList = splitByNewLine(howToPractice)

        if (practiceList.isNotEmpty()) {
            val heading = when (type) {

                ActionPlanType.ACTIVITY.type -> page.getByRole(
                    AriaRole.HEADING,
                    Page.GetByRoleOptions().setName("How to put it into practice")
                )

                else -> {
                    page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How to Implement"))
                }
            }
            heading.waitFor()
            heading.click()

            practiceList.forEachIndexed { index, practice ->
                logger.info { "Validating Practice item index=$index, value=$practice" }

                val practiceElement = when (type) {
                    ActionPlanType.ACTIVITY.type -> page.getByTestId("exercise-how-to-practice-$index")
                    ActionPlanType.STRESS.type -> page.getByTestId("stress-how-to-practice-$index")
                    else -> page.getByTestId("sleep-how-to-practice-$index")
                }
                practiceElement.waitFor()

                assertEquals(practice, practiceElement.innerText())
            }
        } else {
            logger.warn { "Practice list is empty for activityId=${activity.id}" }
        }
    }


    private fun whatToExpect(activity: Recommendation, type: String) {
        logger.info { "Validating What To Expect section for activityId=${activity.id}" }

        val descriptiveMeta = activity.descriptive_meta
        val whatToExpect = descriptiveMeta?.what_to_expect

        if (!whatToExpect.isNullOrBlank()) {

            val heading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What to expect"))
            heading.waitFor()
            heading.click()


            val subHeadingElement = when (type) {
                ActionPlanType.ACTIVITY.type -> page.getByTestId("exercise-what-to-expect")
                ActionPlanType.STRESS.type -> page.getByTestId("stress-what-to-expect")
                else -> page.getByTestId("sleep-what-to-expect")
            }
            subHeadingElement.waitFor()

            logger.info { "What To Expect actual=${subHeadingElement.innerText().normalizeForUiCompare()}" }
            logger.info { "What To Expect expected=${whatToExpect.normalizeForUiCompare()}" }

            assertEquals(whatToExpect.normalizeForUiCompare(), subHeadingElement.innerText().normalizeForUiCompare())

            logger.info { "What To Expect section validated for activityId=${activity.id}" }
        } else {
            logger.warn { "What to expect empty for activityId=${activity.id}" }
        }
    }


    private fun activityHeaderSection(activity: Recommendation) {
        val exercise = "Exercise"

        logger.info { "🔹 Validating Activity Header Section for activity: ${activity.name}" }

        val heading = page.getByTestId("exercise-panel-heading")
        val nameElement = page.getByTestId("exercise-detail-name")
        val displayElement = page.getByTestId("exercise-detail-display-name")
        val imageElement = page.getByTestId("exercise-detail-image")

        listOf(heading, nameElement, displayElement, imageElement).forEach {
            it.scrollIntoViewIfNeeded()
            it.waitFor()
            logger.info { "✅ Element visible: ${it}" }
        }

        val headingText = heading.innerText()
        logger.info { "Heading text: $headingText" }
        assertEquals(exercise, headingText)

        val nameText = nameElement.innerText()
        logger.info { "Name text: $nameText, Expected: ${activity.name}" }
        assertEquals(activity.name?.uppercase(), nameText)

        val displayText = displayElement.innerText()
        logger.info { "Display name text: $displayText, Expected: ${activity.display_name}" }
        assertEquals(activity.display_name, displayText)

        val variantDescription = activity.variant_description
        logger.info { "Variant description from API: $variantDescription" }

        val bagList = variantDescription
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

        logger.info { "Parsed variant list: $bagList" }

        if (variantDescription?.isBlank() == false && bagList.isNotEmpty()) {
            bagList.forEachIndexed { index, expectedVariant ->
                val uiVariant = page.getByTestId("exercise-variant-value-$index")
                uiVariant.waitFor()

                val uiText = uiVariant.innerText()
                logger.info { "Variant[$index] UI: $uiText, Expected: $expectedVariant" }

                assertEquals(expectedVariant, uiText)
            }
        } else {
            logger.info { "ℹ️ No variant description available, skipping variant validation" }
        }
    }


    private fun validatingActivityMainCards(activityList: List<Recommendation>) {
        logger.info { "Validating ${activityList.size} activity main cards" }

        activityList.forEach { activity ->

            logger.info { "Validating card for activity id=${activity.id}, name=${activity.display_name}" }

            val title = page.getByTestId("exercise-title-${activity.id}")
            val image = page.getByTestId("exercise-image-${activity.id}")

            listOf(title, image).forEach {
                it.waitFor()
                logger.info { "✅ Element visible: ${it}" }
            }

            val expectedName = activity.display_name
            val uiName = title.innerText()

            logger.info { "Expected name: $expectedName | UI name: $uiName" }

            assertEquals(expectedName, uiName)

            val variantDescription = activity.variant_description
            logger.info { "Variant description: $variantDescription" }

            val bagList = variantDescription
                ?.split(",")
                ?.map { it.trim() }
                ?: emptyList()

            logger.info { "Bag list: $bagList" }

            if (variantDescription?.isBlank() == false && bagList.isNotEmpty()) {
                bagList.forEach { bag ->
                    logger.info { "Validating bag text: $bag" }
                    page.getByText(bag).waitFor()
                }
            } else {
                logger.info { "No bags to validate for activity id=${activity.id}" }
            }
        }
    }

    /**---------------Sleep-------------------*/
    fun sleepMainCards() {
        StepHelper.step(VALIDATING_SLEEP_RECOMMENDATIONS)
        logger.info { "Fetching sleep recommendations from data" }

        val sleepList = recommendationData?.recommendations?.filter { it.category == ActionPlanType.SLEEP.type }


        if (sleepList?.isNotEmpty() == true) {
            logger.info { "Sleep list is not empty, waiting for Exercise heading" }

            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Exercise")).waitFor()

            validatingSleepMainCards(sleepList)

            validatingSleepSidePanel(sleepList)
        } else {
            logger.warn { "No sleep recommendations found" }
        }
    }


    private fun validatingSleepMainCards(sleepList: List<Recommendation>) {
        logger.info { "Validating ${sleepList.size} sleep main cards" }
        sleepList.forEach { sleep ->
            logger.info { "Validating card for sleep id=${sleep.id}, name=${sleep.display_name}" }

            val title = page.getByTestId("sleep-card-name-${sleep.id}")
            val image = page.getByTestId("sleep-card-image-${sleep.id}")

            listOf(title, image).forEach {
                title.scrollIntoViewIfNeeded()
                it.waitFor()
                logger.info { "✅ Element visible: ${it}" }
            }

            val expectedName = sleep.variant_description ?: sleep.display_name
            val uiName = title.innerText()

            logger.info { "Expected name: $expectedName | UI name: $uiName" }

            assertEquals(expectedName, uiName)
        }
    }


    private fun validatingSleepSidePanel(sleepList: List<Recommendation>) {
        logger.info { "Starting sleep side panel validation for ${sleepList.size} items" }
        sleepList.forEach { sleep ->
            StepHelper.step("${OPENING_SLEEP_PANEL}: ${sleep.display_name}")
            logger.info { "Opening sleep panel for id=${sleep.id}" }
            val descriptiveMeta = sleep.descriptive_meta
            val descriptionExpected = descriptiveMeta?.description


            val title = page.getByTestId("sleep-card-name-${sleep.id}")
            title.click()

            val dialog = page.getByRole(AriaRole.DIALOG)
            dialog.waitFor()

            logger.info { "Sleep dialog opened for id=${sleep.id}" }

            sleepHeaderSection(sleep)


            descriptionSection(descriptionExpected, ActionPlanType.SLEEP.type)

            potentialBiomarker(sleep, ActionPlanType.SLEEP.type)

            validatingViewMore(sleep)

            whyItWorks(sleep, ActionPlanType.SLEEP.type)
            intoPractice(sleep, ActionPlanType.SLEEP.type)
            whatToExpect(sleep, ActionPlanType.SLEEP.type)

            val closePanel = page.getByTestId("sleep-panel-close")
            closePanel.waitFor()
            closePanel.click()

            logger.info { "Closed sleep panel for id=${sleep.id}" }
        }
    }

    private fun descriptionSection(descriptionExpected: String?, type: String) {
        descriptionExpected?.let {
            logger.info { "Validating description for type: $type" }
            val description = when (type) {
                ActionPlanType.ACTIVITY.type -> {
                    page.getByTestId("exercise-description")
                }

                ActionPlanType.STRESS.type -> {
                    page.getByTestId("stress-description")
                }

                else -> {
                    page.getByTestId("sleep-description")
                }

            }
            description.waitFor()

            val actual = description.innerText().normalizeForUiCompare()
            val expected = descriptionExpected.normalizeForUiCompare()
            logger.info { "Description actual  : $actual" }
            logger.info { "Description expected: $expected" }

            assertEquals(expected, actual)
        }
    }

    private fun validatingViewMore(sleep: Recommendation) {
        val descriptiveMeta = sleep.descriptive_meta
        if (!descriptiveMeta?.why_it_works.isNullOrBlank() || !descriptiveMeta?.how_to_practice.isNullOrBlank() || !descriptiveMeta?.what_to_expect.isNullOrBlank()) {
            val viewMore = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View More"))
            viewMore.waitFor()
            viewMore.click()
        }
    }


    private fun sleepHeaderSection(sleep: Recommendation) {
        val sleepTitle = "Sleep"
        logger.info { "🔹 Validating Sleep Header Section for: ${sleep.display_name}" }

        val heading = page.getByTestId("sleep-panel-heading")
        val nameElement = page.getByTestId("sleep-detail-name")
        val displayElement = page.getByTestId("sleep-detail-display-name")
        val imageElement = page.getByTestId("sleep-detail-image")


        listOf(heading, nameElement, displayElement, imageElement).forEach {
            it.scrollIntoViewIfNeeded()
            it.waitFor()
            logger.info { "✅ Element visible: ${it}" }
        }

        val headingText = heading.innerText()
        logger.info { "Heading text: $headingText" }
        assertEquals(sleepTitle, headingText)

        val nameText = nameElement.innerText()
        logger.info { "Name text: $nameText, Expected: ${sleep.name}" }
        assertEquals(sleep.name?.uppercase(), nameText)

        val displayText = displayElement.innerText()
        val expectedName = sleep.variant_description ?: sleep.display_name
        logger.info { "Display name text: $displayText, Expected: $expectedName" }
        assertEquals(expectedName, displayText)
    }

    /**---------------Stress-------------------*/
    fun stressMainCards() {
        StepHelper.step(VALIDATING_STRESS_RECOMMENDATIONS)
        logger.info { "Fetching stress recommendations from data" }

        val stressList = recommendationData?.recommendations?.filter { it.category == ActionPlanType.STRESS.type }


        if (stressList?.isNotEmpty() == true) {
            logger.info { "Stress list is not empty, waiting for Stress heading" }

            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Stress").setExact(true)).waitFor()

            validatingStressMainCards(stressList)

            validatingStressSidePanel(stressList)
        } else {
            logger.warn { "No stress recommendations found" }
        }
    }


    private fun validatingStressMainCards(stressList: List<Recommendation>) {
        logger.info { "Validating ${stressList.size} activity main cards" }

        stressList.forEach { stress ->

            logger.info { "Validating card for activity id=${stress.id}, name=${stress.display_name}" }

            val title = page.getByTestId("stress-card-name-${stress.id}")
            val image = page.getByTestId("stress-card-image-${stress.id}")

            listOf(title, image).forEach {
                it.waitFor()
                logger.info { "✅ Element visible: ${it}" }
            }

            val expectedName = stress.display_name
            val uiName = title.innerText()

            logger.info { "Expected name: $expectedName | UI name: $uiName" }

            assertEquals(expectedName, uiName)

            val variantDescription = stress.variant_description
            logger.info { "Variant description: $variantDescription" }

            val bagList = variantDescription
                ?.split(",")
                ?.map { it.trim() }
                ?: emptyList()

            logger.info { "Bag list: $bagList" }

            if (variantDescription?.isBlank() == false && bagList.isNotEmpty()) {
                bagList.forEachIndexed { index, bag ->
                    val bagText = page.getByTestId("stress-card-${stress.id}").getByTestId("stress-card-badge-${index}")
                    bagText.waitFor()
                    logger.info { "Validating bag text: expected:$bag, actual:${bagText.innerText()}" }
                    assertEquals(bagText.innerText(), bag)
                }
            } else {
                logger.info { "No bags to validate for activity id=${stress.id}" }
            }
        }
    }

    private fun validatingStressSidePanel(stressList: List<Recommendation>) {
        logger.info { "Starting stress side panel validation for ${stressList.size} items" }
        stressList.forEach { stress ->
            StepHelper.step("${OPENING_STRESS_PANEL}: ${stress.display_name}")
            logger.info { "Opening stress panel for id=${stress.id}" }
            val descriptiveMeta = stress.descriptive_meta
            val descriptionExpected = descriptiveMeta?.description

            val title = page.getByTestId("stress-card-name-${stress.id}")
            title.click()

            val dialog = page.getByRole(AriaRole.DIALOG)
            dialog.waitFor()

            logger.info("Stress dialog opened for id=${stress.id}")

            stressHeaderSection(stress)

            descriptionSection(descriptionExpected, ActionPlanType.STRESS.type)

            potentialBiomarker(stress, ActionPlanType.STRESS.type)

            validatingViewMore(stress)

            whyItWorks(stress, ActionPlanType.STRESS.type)
            intoPractice(stress, ActionPlanType.STRESS.type)
            whatToExpect(stress, ActionPlanType.STRESS.type)

            val closePanel = page.getByTestId("stress-panel-close")
            closePanel.waitFor()
            closePanel.click()

            logger.info("Closed stress panel for id=${stress.id}")
        }
    }

    private fun stressHeaderSection(stress: Recommendation) {
        val stressTitle = "Stress"
        logger.info("🔹 Validating Stress Header Section for: ${stress.display_name}")

        val heading = page.getByTestId("stress-panel-heading")
        val nameElement = page.getByTestId("stress-detail-name")
        val displayElement = page.getByTestId("stress-detail-display-name")
        val imageElement = page.getByTestId("stress-detail-image")

        listOf(heading, nameElement, displayElement, imageElement).forEach {
            it.scrollIntoViewIfNeeded()
            it.waitFor()
            logger.info("✅ Element visible: ${it}")
        }

        val headingText = heading.innerText()
        logger.info("Heading text: $headingText")
        assertEquals(stressTitle, headingText)

        val nameText = nameElement.innerText()
        logger.info("Name text: $nameText, Expected: ${stress.name}")
        assertEquals(stress.name?.uppercase(), nameText)

        val displayText = displayElement.innerText()
        val expectedName = stress.display_name
        logger.info("Display name text: $displayText, Expected: $expectedName")
        assertEquals(expectedName, displayText)
    }


    /**---------------Supplements-------------------*/
    fun supplementsMainCards() {
        StepHelper.step(VALIDATING_SUPPLEMENTS)
        val supplementList =
            recommendationData?.recommendations?.filter { it.category == ActionPlanType.SUPPLEMENT.type }
        if (supplementList?.isNotEmpty() == true) {
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Supplements")).waitFor()

            validatingSupplementsMainCards(supplementList)
            validatingSupplementsSideCards(supplementList)
        }
    }


    fun validatingSupplementsMainCards(supplementList: List<Recommendation>) {
        supplementList.forEach { supplement ->
            val id = supplement.id
            val image = page.getByTestId("supplement-image-${id}")
            val nameUiElement = page.getByTestId("supplement-name-${id}")
            listOf(image, nameUiElement).forEach { it.waitFor() }

            val nameExpected = nameUiElement.innerText()
            val nameActual = supplement.variant_meta?.name
                ?: supplement.display_name
                ?: supplement.name


            assertEquals(nameExpected, nameActual)

            if (supplement.supplement_intraday_frequency != null ||
                supplement.supplement_weekday_frequency != null
            ) {
                val expectedFrequency = if (supplement.supplement_weekday_frequency != null) "Weekly" else "Daily"

                val frequencyUiElement = page.getByTestId("supplement-frequency-badge-${id}")
                frequencyUiElement.waitFor()
                val actualFrequency = frequencyUiElement.innerText()
                assertEquals(actualFrequency, expectedFrequency)
            }

            supplement.supplement_meta?.dosage?.get("1")?.serving?.let {
                val dosageUiElement = page.getByTestId("supplement-dosage-badge-1-${id}")

                val item = supplement.supplement_meta.dosage["1"]

                val text = "${item?.serving}x " +
                        "${if (item?.timing == "after_food") "After" else "Before"} " +
                        item?.meal

                dosageUiElement.waitFor()

                assertEquals(text, dosageUiElement.innerText())
            }


            supplement.supplement_meta?.dosage?.get("2")?.serving?.let {
                val dosageUiElement = page.getByTestId("supplement-dosage-badge-2-${id}")

                val item = supplement.supplement_meta.dosage["2"]

                val text = "${item?.serving}x " +
                        "${if (item?.timing == "after_food") "After" else "Before"} " +
                        item?.meal

                dosageUiElement.waitFor()

                assertEquals(text, dosageUiElement.innerText())
            }

            supplement.supplement_duration?.let {
                val durationUiElement = page.getByTestId("supplement-duration-badge-${id}")
                durationUiElement.waitFor()
                assertEquals(it, durationUiElement.innerText())
            }

            val note1 = supplement.supplement_meta?.dosage?.get("1")?.notes
            val note2 = supplement.supplement_meta?.dosage?.get("2")?.notes

            if (!note1.isNullOrBlank() || !note1.isNullOrBlank()) {
                val noteUiElement = page.getByTestId("supplement-notes-$id")
                val expected = buildString {
                    append("Note: ")
                    if (note1.isNotBlank()) append(note1)

                    if (note1.isNotBlank() && !note2.isNullOrBlank()) {
                        append("\n")
                    }

                    if (!note2.isNullOrBlank()) append(note2)
                }

                assertEquals(expected.normalizeForUiCompare(), noteUiElement.innerText().normalizeForUiCompare())

            }

            val disclaimerUiElement = page.getByTestId("supplements-disclaimer")
            disclaimerUiElement.waitFor()
            val actual = disclaimerUiElement.innerText()

            assertEquals(supplementsDisclaimer, actual)
        }
    }

    private fun validatingSupplementsSideCards(supplementList: List<Recommendation>) {
        supplementList.forEach { supplement ->
            val id = supplement.id
            val nameUiElement = page.getByTestId("supplement-name-${id}")
            val totalRatings = supplement.variant_meta?.price?.totalRatings

            nameUiElement.waitFor()
            StepHelper.step("${OPENING_SUPPLEMENTS_PANEL}: ${supplement.display_name ?: supplement.name}")
            nameUiElement.click()

            val dialog = page.getByRole(AriaRole.DIALOG)
            dialog.waitFor()

            val imageUrls = supplement.variant_meta?.imageUrls

            if (imageUrls?.isNotEmpty() == true) {

                val toLast = imageUrls.dropLast(1) // removes last item
                toLast.forEach { _ ->
                    page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Next slide")).click()
                }
                toLast.forEach {
                    page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Previous slide")).click()
                }

            }

            if (totalRatings != null) {
                val totalRatingNumber = page.getByTestId("supplements-total-ratings")
                logger.info { "Rating Start.... ${totalRatingNumber.innerText()}" }

                assertEquals("(${formatNumber(totalRatings)})", totalRatingNumber.innerText())
            }


            val cardNameUiElement = page.getByTestId("supplements-detail-name")

            cardNameUiElement.waitFor()

            val expectedName = cardNameUiElement.innerText()
            assertEquals(expectedName, supplement.name)

            val brand = supplement.variant_meta?.brand
            if (!brand.isNullOrBlank() && brand != "NA") {
                page.getByText(brand, Page.GetByTextOptions().setExact(true)).waitFor()
            }

            val amount = supplement.variant_meta?.price?.amount
            if (amount != null) {
                page.getByText("₹${formatNumber(amount)}", Page.GetByTextOptions().setExact(true)).waitFor()
                page.getByText("Inclusive of all taxes").waitFor()
            }

            page.getByTestId("supplements-view-details-heading").waitFor()

            val keyIngredient = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Key Ingredients"))
            keyIngredient.waitFor()

            keyIngredient.click()


            val source =
                if (supplement.variant_meta?.ingredients?.isNotEmpty() == true) supplement.variant_meta?.ingredients else supplement.variant_meta?.nutritionalFacts

            val filteredItems = source?.filter { item ->
                !item.name.isNullOrBlank() &&
                        item.name.trim().lowercase() !in listOf(
                    "excipient", "excipients", "purified water", "flavour"
                )
            }

            filteredItems?.forEachIndexed { index, fact ->
                val ingredientUiElement = page.getByTestId("supplements-ingredient-$index")
                ingredientUiElement.waitFor()
                val expectedFact = "${fact.name} ${formatNumber(fact.amount ?: 0.0)} ${fact.unit ?: ""}"
                assertEquals(expectedFact, ingredientUiElement.innerText().normalizeForUiCompare())
            }

            val buyButton = page.getByTestId("supplements-buy-button")
            buyButton.waitFor()

            val popup = page.waitForPopup {
                buyButton.click()
            }
            logger.info { "Supplement Buy Button Clicked, opened: ${popup.url()}" }
            popup.close()

            val closePanel = page.getByTestId("supplements-panel-close")
            closePanel.waitFor()
            closePanel.click()

        }
    }

    /**---------------Recommendation Test-------------------*/
    fun testCards() {
        StepHelper.step(VALIDATING_FURTHER_TESTS)
        val testList = recommendationData?.recommendations?.filter { it.category == ActionPlanType.TEST.type }


        if (testList?.isNotEmpty() == true) {
            getLabTestsData()
            logger.info("Test.... Stress list is not empty, waiting for Stress heading")
            logger.info("Test.... allTests .. ${allTests.size}")

            page.getByTestId("further-test-heading").waitFor()

            validatingTestMainCards(testList)

        } else {
            logger.warn("No stress recommendations found")
        }
    }

    private fun validatingTestMainCards(testList: List<Recommendation>) {
        testList.forEach { test ->
            val id = test.id
            val image = page.getByTestId("further-test-card-image-$id")
            val nameUiElement = page.getByTestId("further-test-card-name-$id")
            val bookTest = page.getByTestId("further-test-book-button-$id")
            val addTestButton = page.getByTestId("further-test-added-button-$id")

            listOf(image, nameUiElement).forEach { it.waitFor() }

            val expected = nameUiElement.innerText()

            assertEquals(expected, test.display_name)


            val isBooked = isTestBooked(
                labTestsData = allTests,
                testId = test.test_id,
                testType = test.test_type
            )

            val isCompleted = test.actions
                ?.getOrNull(0)
                ?.user_recommendation_actions
                ?.getOrNull(0)
                ?.is_completed


            if (isCompleted == true) {
                addTestButton.waitFor()
                assertEquals("Added to Plan", addTestButton.innerText())
            } else {
                bookTest.waitFor()
                val bookTestActual = bookTest.innerText()
                val bookTestExpected = if (isBooked) {
                    "Booked"
                } else {
                    "Book a Test"
                }

                if (!isBooked) {
                    page.waitForTimeout(2000.0)
                    bookTest.click()
                    TestDetailPage(page)
                        .waitForPageLoad()
                        .clickBackButton()
                }
                assertEquals(bookTestExpected, bookTestActual)
            }
        }
    }

    /**-------------Empty Action Plan-----------------*/
    fun emptyActionPlanPage() {
        StepHelper.step(VALIDATING_EMPTY_ACTION_PLAN_PAGE)
        getHomeData()
        getHealthData()
        getGoalData()
        val isTestPending = isShowEmptyTestInProgress()
        val isConsultationPending = isShouldShowEmptyState()
        val isRecommendationPending = isShouldRecommendationInProgress()

        when {
            isTestPending -> {
                bloodTestInProgress()
            }

            isConsultationPending -> {
                consultationBookPending()
            }

            isRecommendationPending -> {

            }
        }

        logger.info { "Action Plan isTestPending:$isTestPending" }
        logger.info { "Action Plan isConsultationPending:$isConsultationPending" }
        logger.info { "Action Plan isRecommendationPending:$isRecommendationPending" }

    }

    private fun consultationBookPending() {
        val status = homeData?.next_steps?.free_consultation?.status
        val productId = homeData?.next_steps?.free_consultation?.product_id

        val image = page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("generate action-plan"))
        val title = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Action plan will be generated"))

        val subtitle = page.getByText("Your test results are ready,")
        val buttonStatus = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Book consultation"))


        val formattedDate = formatConsultationDate(
            homeData?.next_steps?.free_consultation?.scheduled_at
        )


        listOf(image, title, subtitle, buttonStatus).forEach { it.waitFor() }

        val isConsultationBooked =
            (status == "completed" || status == "booked") &&
                    !productId.isNullOrBlank()

        val isConsultationPending =
            status == "not_booked"

        val titleExpected =
            messages[if (!isConsultationBooked) ActionPlanStatus.NOT_SCHEDULED else ActionPlanStatus.SCHEDULED]
        var subTitleExpected =
            subText[if (!isConsultationBooked) ActionPlanStatus.NOT_SCHEDULED else ActionPlanStatus.SCHEDULED]

        if (isConsultationBooked) {
            subTitleExpected = subTitleExpected?.plus(" $formattedDate.")
        }

        assertEquals(titleExpected, title.innerText())
        assertEquals(subTitleExpected, subtitle.innerText())

        val hasQuestionnaireDone = programGoalData?.program?.is_questionnaire_taken == true

        buttonStatus.click()

        when {
            hasQuestionnaireDone && !isConsultationBooked -> {
                //Symptoms

            }

            !hasQuestionnaireDone || isConsultationPending -> {
                freeConsultationsInfo()
            }
        }
    }

    fun freeConsultationsInfo() {
        val headImage = page.locator(".absolute.inset-0").first()
        val headTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What's included"))
        val paragraphOne =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Quick overview of dashboard"))
        val paragraphTwo =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Symptoms check-in and mapping"))
        val paragraphThree =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("State of existing medical"))
        val paragraphFour =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Highlight any urgent or"))
        val paragraphFive =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Suggest further medical"))
        val paragraphSix =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Action Plan - Activity,"))
        val noteText =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Note: Consultations will not"))
        val nextButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Next"))

        listOf(
            headImage,
            headTitle,
            paragraphOne,
            paragraphTwo,
            paragraphThree,
            paragraphFour,
            paragraphFive,
            paragraphSix,
            noteText,
            nextButton
        ).forEach { it.waitFor() }

        val paragraphComponent = listOf(
            paragraphOne,
            paragraphTwo,
            paragraphThree,
            paragraphFour,
            paragraphFive,
            paragraphSix
        )

        paragraphComponent.forEachIndexed { index, component ->
            assertEquals(CONSULTATION_CALENDLY_INCLUSIONS[index], component.innerText())
        }

        nextButton.click()
    }

    private fun bloodTestInProgress() {
        val image = page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("generate action-plan"))
        val title = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Action plan will be generated"))

        val subtitle = page.getByText("Currently your test results")
        val buttonStatus = page.getByText("Track test status")

        listOf(image, title, subtitle, buttonStatus).forEach { it.waitFor() }


        val titleExpected = messages[ActionPlanStatus.TEST_IN_PROGRESS]
        val subTitleExpected = subText[ActionPlanStatus.TEST_IN_PROGRESS]

        assertEquals(titleExpected, title.innerText())
        assertEquals(subTitleExpected, subtitle.innerText())
        buttonStatus.click()

        page.waitForURL {
            page.url().contains(TestConfig.Urls.TRACK_RESULT)
        }

    }

    private fun isShowEmptyTestInProgress(): Boolean {
        return homeData?.next_steps?.has_completed_consultation != true &&
                healthData?.data?.blood?.data != null &&
                healthData?.data?.blood?.data?.isEmpty() == true
    }

    private fun isShouldShowEmptyState(): Boolean {
        return homeData?.next_steps?.has_completed_consultation != true &&
                healthData?.data?.blood?.data != null &&
                healthData?.data?.blood?.data?.isNotEmpty() == true

    }

    private fun isShouldRecommendationInProgress(): Boolean {
        val hasQuestionnaireDone = programGoalData?.program?.is_questionnaire_taken == true
        return hasQuestionnaireDone &&
                homeData?.next_steps?.has_completed_consultation == true &&
                recommendationData?.recommendations?.isEmpty() == true &&
                recommendationData?.food_recommendations?.isEmpty() == true

    }


}