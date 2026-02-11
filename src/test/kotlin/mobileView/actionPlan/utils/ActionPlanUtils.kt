package mobileView.actionPlan.utils

import mobileView.actionPlan.model.NutritionCategorySubtext
import mobileView.actionPlan.model.NutritionFoodType
import kotlin.math.roundToInt

object ActionPlanUtils {
    val nutritionCategorySubtext = mapOf(
        "Edible Oil & Fat" to NutritionCategorySubtext(
            eat = "3–4 tsp/day total visible fat (from all \"Eat\" oils)",
            limit = "≤2 tsp/use, ≤2x per week",
            avoid = "Rarely; ≤1 tsp/use, ≤1–2x per month if at all"
        ),
        "Fruits" to NutritionCategorySubtext(
            eat = "1 medium fruit or 1 cup chopped (≈100–150 g), 1–2 servings/day",
            limit = "½ medium fruit or ½ cup (≈60–80 g), ≤2x/week",
            avoid = "Occasionally; ≤½ portion ≤1x/month"
        ),
        "Cereals & Millets" to NutritionCategorySubtext(
            eat = "1 medium katori cooked (≈100 g), per main meal",
            limit = "½ medium katori (≈50 g), ≤3x/week",
            avoid = "Avoid or ≤½ katori ≤1x/month"
        ),
        "Legumes" to NutritionCategorySubtext(
            eat = "1 medium katori cooked (≈100 g) daily or alternate days",
            limit = "½ katori (≈50 g) ≤2–3x/week",
            avoid = "Rarely; ≤¼ katori ≤1x/month"
        ),
        "Nuts & Oilseeds" to NutritionCategorySubtext(
            eat = "4–6 whole nuts or 1 tbsp seeds/day",
            limit = "2–3 nuts or ½ tbsp seeds ≤2–3x/week",
            avoid = "Rarely; small tasting quantity ≤1x/month"
        ),
        "Vegetables" to NutritionCategorySubtext(
            eat = "Unlimited, focus on variety, color, and rotation (at least 3–5 types/day)",
            limit = "Up to 1 cup cooked (≈100 g), ≤3x/week",
            avoid = "Avoid until retest / clearance, or ≤1x/month"
        ),
        "Roots & Tubers" to NutritionCategorySubtext(
            eat = "½–1 katori cooked (≈80–100 g), ≤3x/week",
            limit = "½ katori (≈50 g), ≤1–2x/week",
            avoid = "≤1x/month / NA"
        ),
        "Green Leafy Vegetables" to NutritionCategorySubtext(
            eat = "1–2 cups cooked or raw daily",
            limit = "½–1 cup cooked, ≤3x/week",
            avoid = "≤1x/month / NA"
        ),
        "Beverages" to NutritionCategorySubtext(
            eat = "Up to 2 cups (≈200 ml each), 4–6 times/week",
            limit = "Up to 1 cup (≈200 ml), 1–3 times/week",
            avoid = "Rarely (≤1 serving/month) or only in social contexts"
        ),
        "Sugars" to NutritionCategorySubtext(
            eat = "Up to 1 tsp/day, ≤ 3–4x/week",
            limit = "1–2 tsp, ≤ 1–2x/week",
            avoid = "Rarely, ≤ 1x/month"
        ),
        "Condiments & Spices" to NutritionCategorySubtext(
            eat = "Use freely for flavour as tolerated",
            limit = "",
            avoid = ""
        ),
        "Processed Foods" to NutritionCategorySubtext(
            eat = "",
            limit = "Bakery/snack items ≤1 serving ≤1x/week",
            avoid = "Fried, packaged, or ultra-processed foods — avoid"
        ),
        "Culinary Creations" to NutritionCategorySubtext(
            eat = "1 medium portion (≈100 g / 1 katori), 3–5x/week Use minimal oil/ghee; mostly whole grains, lentils, vegetables; avoid deep-frying",
            limit = "1 portion, 1–2x/week Portion-controlled; watch oil/fat",
            avoid = "≤1x/month or for special occasions only Portion-controlled; watch oil/fat"
        )
    )

    fun ninetyPercent(value: Double): Int {
        return (value * 0.9).roundToInt()
    }

    fun getCategorySubtext(type: String, category: String?): String? {
        val foodType = nutritionCategorySubtext[category]
        return when (type) {
            NutritionFoodType.EAT.type -> {
                foodType?.eat
            }

            NutritionFoodType.LIMIT.type -> {
                foodType?.limit
            }

            NutritionFoodType.AVOID.type -> {
                foodType?.avoid
            }

            else -> null
        }
    }

    fun findSubCategoryExist(category: String?): Boolean {
        return nutritionCategorySubtext[category]!=null
    }


}