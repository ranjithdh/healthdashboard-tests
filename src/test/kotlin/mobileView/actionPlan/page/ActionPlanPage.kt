package mobileView.actionPlan.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.model.*
import mobileView.actionPlan.utils.ActionPlanUtils.findSubCategoryExist
import mobileView.actionPlan.utils.ActionPlanUtils.getCategorySubtext
import mobileView.actionPlan.utils.ActionPlanUtils.ninetyPercent
import mobileView.actionPlan.utils.ActionPlanUtils.removeWhitespace
import mobileView.actionPlan.utils.ActionPlanUtils.splitByNewLine
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
    private var optimal = "Optimal"
    private var normal = "Normal"
    private var question = "question"
    private var activity = "activity"

    init {
        //  monitorTraffic()
    }

    private fun monitorTraffic() {
        captureRecommendationData()
    }

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

                //logger.info { "API response...${responseBody}" }

                val responseObj = json.decodeFromString<NutritionRecommendationResponse>(responseBody)

                if (responseObj.data != null) {
                    recommendationData = responseObj.data
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

    /**-----------------Nutrition-----------------------*/

    fun dailyCaloriesIntakeCard() {
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

        logger.info("API Daily Calories : $caloriesValues")
        logger.info("API Carbohydrate   : $carbohydrateValues")
        logger.info("API Protein        : $proteinValues")
        logger.info("API Fat            : $fatValues")
        logger.info("API Fiber          : $fiberValues")

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
        logger.info("Fetching activity recommendations from data")

        val activityList = recommendationData?.recommendations?.filter { it.category == activity }

        logger.info("Filtered activity list size: ${activityList?.size ?: 0}")

        if (activityList?.isNotEmpty() == true) {
            logger.info("Activity list is not empty, waiting for Exercise heading")

            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Exercise")).waitFor()

            validatingMainCards(activityList)

            validatingActivitySidePanel(activityList)
        } else {
            logger.warn("No activity recommendations found")
        }
    }

    private fun validatingActivitySidePanel(activityList: List<Recommendation>) {
        logger.info { "Starting activity side panel validation for ${activityList.size} activities" }

        activityList.forEach { activity ->
            logger.info { "Opening activity panel for activityId=${activity.id}" }

            val descriptiveMeta = activity.descriptive_meta
            val descriptionExpected = descriptiveMeta?.description

            val title = page.getByTestId("exercise-title-${activity.id}")
            title.click()

            val dialog = page.getByRole(AriaRole.DIALOG)
            dialog.waitFor()

            logger.info { "Activity dialog opened for activityId=${activity.id}" }

            activityHeaderSection(activity)

            val description = page.getByTestId("exercise-description")
            description.waitFor()

            logger.info { "Validating description for actual=${removeWhitespace(descriptionExpected)}" }
            logger.info { "Validating description for actual=${removeWhitespace(description.innerText())}" }
            assertEquals(removeWhitespace(descriptionExpected), removeWhitespace(description.innerText()))

            potentialBiomarker(activity)

            val viewMore = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View More"))
            viewMore.waitFor()
            viewMore.click()

            logger.info { "Clicked View More for activityId=${activity.id}" }

            whyItWorks(activity)
            intoPractice(activity)
            whatToExpect(activity)

            val closePanel = page.getByTestId("exercise-panel-close")
            closePanel.waitFor()
            closePanel.click()

            logger.info { "Closed activity panel for activityId=${activity.id}" }
        }

        logger.info { "Completed activity side panel validation" }
    }


    private fun potentialBiomarker(activity: Recommendation) {
        logger.info { "Validating Potential Biomarker section for activityId=${activity.id}" }

        val down = "down"
        val metricRecommendations = activity.metric_recommendations

        val header = page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Potential biomarker impact")
        )
        header.waitFor()
        header.click()

        metricRecommendations?.forEachIndexed { index, recommendations ->
            val metric = recommendations.metric

            val biomarkerNameExpected = metric?.metric
            val trendArrow = metric?.trend_arrow

            logger.info {
                "Validating biomarker index=$index, name=$biomarkerNameExpected, trend=$trendArrow"
            }

            val arrowUi = if (trendArrow == down) {
                page.getByTestId("exercise-impact-down-$index")
            } else {
                page.getByTestId("exercise-impact-up-$index")
            }

            arrowUi.waitFor()

            val biomarkerUi = page.getByTestId("exercise-impact-$index")
            biomarkerUi.waitFor()

            assertEquals(biomarkerNameExpected, biomarkerUi.innerText())
        }

        logger.info { "Potential Biomarker section validated for activityId=${activity.id}" }
    }


    private fun whyItWorks(activity: Recommendation) {
        logger.info { "Validating Why It Works section for activityId=${activity.id}" }

        val heading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Why it works"))
        heading.waitFor()
        heading.click()

        val descriptiveMeta = activity.descriptive_meta
        val whyItWorks = descriptiveMeta?.why_it_works
        val workList = splitByNewLine(whyItWorks)

        if (workList.isNotEmpty()) {
            workList.forEachIndexed { index, work ->
                logger.info { "Validating Why It Works item index=$index, value=$work" }

                val whyItWorkElement = page.getByTestId("exercise-why-it-works-$index")
                whyItWorkElement.waitFor()

                assertEquals(work, whyItWorkElement.innerText())
            }
        } else {
            logger.warn { "Why It Works list is empty for activityId=${activity.id}" }
        }
    }


    private fun intoPractice(activity: Recommendation) {
        logger.info { "Validating How To Practice section for activityId=${activity.id}" }

        val heading = page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("How to put it into practice")
        )
        heading.waitFor()
        heading.click()

        val descriptiveMeta = activity.descriptive_meta
        val howToPractice = descriptiveMeta?.how_to_practice
        val practiceList = splitByNewLine(howToPractice)

        if (practiceList.isNotEmpty()) {
            practiceList.forEachIndexed { index, practice ->
                logger.info { "Validating Practice item index=$index, value=$practice" }

                val practiceElement = page.getByTestId("exercise-how-to-practice-$index")
                practiceElement.waitFor()

                assertEquals(practice, practiceElement.innerText())
            }
        } else {
            logger.warn { "Practice list is empty for activityId=${activity.id}" }
        }
    }


    private fun whatToExpect(activity: Recommendation) {
        logger.info { "Validating What To Expect section for activityId=${activity.id}" }

        val descriptiveMeta = activity.descriptive_meta
        val whatToExpect = descriptiveMeta?.what_to_expect

        val heading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What to expect"))
        heading.waitFor()
        heading.click()


        val subHeadingElement = page.getByTestId("exercise-what-to-expect")
        subHeadingElement.waitFor()

        logger.info { "What To Expect actual=${removeWhitespace(subHeadingElement.innerText())}" }
        logger.info { "What To Expect expected=${removeWhitespace(whatToExpect)}" }

        assertEquals(removeWhitespace(whatToExpect), removeWhitespace(subHeadingElement.innerText()))

        logger.info { "What To Expect section validated for activityId=${activity.id}" }
    }


    private fun activityHeaderSection(activity: Recommendation) {
        val exercise = "Exercise"

        logger.info { "🔹 Validating Activity Header Section for activity: ${activity.name}" }

        val heading = page.getByTestId("exercise-panel-heading")
        val nameElement = page.getByTestId("exercise-detail-name")
        val displayElement = page.getByTestId("exercise-detail-display-name")
        val imageElement = page.getByTestId("exercise-detail-image")

        listOf(heading, nameElement, displayElement, imageElement).forEach {
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


    private fun validatingMainCards(activityList: List<Recommendation>) {
        logger.info("Validating ${activityList.size} activity main cards")

        activityList.forEach { activity ->

            logger.info("Validating card for activity id=${activity.id}, name=${activity.display_name}")

            val title = page.getByTestId("exercise-title-${activity.id}")
            val image = page.getByTestId("exercise-image-${activity.id}")

            listOf(title, image).forEach {
                it.waitFor()
                logger.info("Element visible: ${it}")
            }

            val expectedName = activity.display_name
            val uiName = title.innerText()

            logger.info("Expected name: $expectedName | UI name: $uiName")

            assertEquals(expectedName, uiName)

            val variantDescription = activity.variant_description
            logger.info("Variant description: $variantDescription")

            val bagList = variantDescription
                ?.split(",")
                ?.map { it.trim() }
                ?: emptyList()

            logger.info("Bag list: $bagList")

            if (variantDescription?.isBlank() == false && bagList.isNotEmpty()) {
                bagList.forEach { bag ->
                    logger.info("Validating bag text: $bag")
                    page.getByText(bag).waitFor()
                }
            } else {
                logger.info("No bags to validate for activity id=${activity.id}")
            }
        }
    }


}