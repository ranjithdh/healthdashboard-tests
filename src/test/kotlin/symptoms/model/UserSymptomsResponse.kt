package symptoms.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSymptomsResponse(
    val status: String,
    val message: String,
    val data: SymptomsData
)

@Serializable
data class SymptomsData(
    val symptoms: List<Symptom>
)

@Serializable
data class Symptom(
    @SerialName("symptom_id")
    val symptomId: String,

    val name: String,
    val description: String,
    val category: String,
    val count: Int,

    @SerialName("is_latest")
    val isLatest: Boolean,

    @SerialName("last_reported")
    val lastReported: String,

    @SerialName("personalized_generated_description")
    val personalizedGeneratedDescription: PersonalizedGeneratedDescription,

    val biomarkers: List<Biomarker>,

    @SerialName("is_data_refresh_required")
    val isDataRefreshRequired: Boolean
)

@Serializable
data class PersonalizedGeneratedDescription(
    @SerialName("what_it_means_to_you")
    val whatItMeansToYou: List<String>,

    @SerialName("causing_factors_explanation")
    val causingFactorsExplanation: List<CausingFactorExplanation>
)

@Serializable
data class CausingFactorExplanation(
    val factor: String,
    val explanation: String
)

@Serializable
data class Biomarker(
    @SerialName("metric_id")
    val metricId: String,

    val metric: String,

    @SerialName("display_name")
    val displayName: String,

    val value: Double?,

    val unit: String,

    val inference: String,
    val description: String,

    @SerialName("group_name")
    val groupName: String? = null,

    @SerialName("rating_rank")
    val ratingRank: Int,

    @SerialName("source_type")
    val sourceType: String,

    val tag: String? = null
)

