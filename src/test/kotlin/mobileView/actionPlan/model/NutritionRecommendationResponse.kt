package mobileView.actionPlan.model

import kotlinx.serialization.Serializable

@Serializable
data class NutritionRecommendationResponse(
    val status: String,
    val message: String,
    val data: RecommendationData? = null
)

@Serializable
data class RecommendationData(
    val nutrition_recommendation_status: String? = null,
    val is_recommendation_notification_sent: Boolean? = null,
    val recommendation_notification_sent_at: String? = null,
    val last_recommendation_updated_at: String? = null,
    val is_personalized: Boolean? = null,
    val nutrition_plan_status: String? = null,
    val recommendations: List<String>? = null,
    val food_recommendations: List<FoodRecommendation>? = null,
    val nutrient_profile: NutrientProfile? = null,
    val recommendation_pdf_url: String? = null,
    val weight: String? = null,
    val bmr: Int? = null
)

@Serializable
data class FoodRecommendation(
    val id: String? = null,
    val food_id: String? = null,
    val metric_id: String? = null,
    val inference_bucket_id: String? = null,
    val suggestion: String? = null,
    val age_variation: String? = null,
    val sex_variation: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val food: Food? = null,
    val metric: Metric? = null,
    val inference_bucket: InferenceBucket? = null,
    val impact_biomarkers: List<ImpactBiomarker>? = null
)

@Serializable
data class Food(
    val id: String? = null,
    val food_id: String? = null,
    val name: String? = null,
    val category: String? = null,
    val food_avoid_tag: List<String>? = null,
    val allergy_tag: List<String>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Metric(
    val id: String? = null,
    val metric_id: String? = null,
    val reference_metric_id: String? = null,
    val metric: String? = null,
    val reference_metric: String? = null,
    val display_name: String? = null,
    val short_name: String? = null,
    val unit: String? = null,
    val description: String? = null,
    val range_type: String? = null,
    val category: String? = null,
    val sub_category: String? = null,
    val group_name: String? = null,
    val sub_group_name: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class InferenceBucket(
    val id: String? = null,
    val inference_bucket_id: String? = null,
    val type: String? = null,
    val inferences: List<String>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class ImpactBiomarker(
    val name: String? = null,
    val inference: String? = null,
    val category: String? = null
)
@Serializable
data class NutrientProfile(
    val calories: String? = null,
    val carbohydrate: String? = null,
    val protein: String? = null,
    val fat: String? = null,
    val fiber: String? = null
)













