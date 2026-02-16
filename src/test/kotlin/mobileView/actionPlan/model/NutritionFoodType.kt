package mobileView.actionPlan.model

sealed class NutritionFoodType(val type: String) {
    object EAT : NutritionFoodType("Eat")
    object LIMIT : NutritionFoodType("Limit")
    object AVOID : NutritionFoodType("Avoid")
}

sealed class ActionPlanType(val type: String) {
    object ACTIVITY : NutritionFoodType("activity")
    object SLEEP : NutritionFoodType("sleep")
    object STRESS : NutritionFoodType("stress")
    object SUPPLEMENT : NutritionFoodType("supplement")
    object TEST : NutritionFoodType("test")
}