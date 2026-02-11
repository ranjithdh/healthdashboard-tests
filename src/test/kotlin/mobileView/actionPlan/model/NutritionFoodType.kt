package mobileView.actionPlan.model

sealed class NutritionFoodType(val type: String) {
    object EAT : NutritionFoodType("Eat")
    object LIMIT : NutritionFoodType("Limit")
    object AVOID : NutritionFoodType("Avoid")
}