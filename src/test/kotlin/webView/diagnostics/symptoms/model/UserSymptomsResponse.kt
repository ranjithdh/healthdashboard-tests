package webView.diagnostics.symptoms.model

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
    val symptoms: List<Symptom> = emptyList()
)

@Serializable
data class Symptom(
    @SerialName("symptom_id")
    val symptomId: String? = null,

    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val count: Int? = null,

    @SerialName("is_latest")
    val isLatest: Boolean? = null,

    @SerialName("last_reported")
    val lastReported: String? = null,

    @SerialName("personalized_generated_description")
    var personalizedGeneratedDescription: PersonalizedGeneratedDescription? = null,

    val biomarkers: List<Biomarker> = emptyList(),

    @SerialName("is_data_refresh_required")
    val isDataRefreshRequired: Boolean? = null
)


@Serializable
data class PersonalizedGeneratedDescription(
    @SerialName("what_it_means_to_you")
    var whatItMeansToYou: List<String> = emptyList(),

    @SerialName("causing_factors_explanation")
    var causingFactorsExplanation: List<CausingFactorExplanation> = emptyList()
)


@Serializable
data class CausingFactorExplanation(
    val factor: String? = null,
    val explanation: String? = null
)


@Serializable
data class Biomarker(
    @SerialName("metric_id")
    val metricId: String? = null,

    val metric: String? = null,

    @SerialName("display_name")
    val displayName: String? = null,

    val value: Double? = null,

    val unit: String? = null,

    val inference: String? = null,
    val description: String? = null,

    @SerialName("group_name")
    val groupName: String? = null,

    @SerialName("rating_rank")
    val ratingRank: Int? = null,

    @SerialName("source_type")
    val sourceType: String? = null,

    val tag: String? = null
)


@Serializable
data class UserSymptomDetailResponse(
    val status: String,
    val message: String,
    val data: SymptomDetailData
)

@Serializable
data class SymptomDetailData(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("symptom_id")
    val symptomId: String,

    @SerialName("reported_at")
    val reportedAt: String,

    @SerialName("is_latest")
    val isLatest: Boolean,

    val description: SymptomDescription,

    @SerialName("data_hash")
    val dataHash: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class SymptomDescription(
    @SerialName("what_it_means_to_you")
    val whatItMeansToYou: List<String>,

    @SerialName("causing_factors_explanation")
    val causingFactorsExplanation: List<CausingFactorExplanation>
)



