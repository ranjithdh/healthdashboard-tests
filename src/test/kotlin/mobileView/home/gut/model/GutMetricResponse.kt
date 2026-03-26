package mobileView.home.gut.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GutMetricResponse(
    val status: String? = null,
    val message: String? = null,
    val data: GutMetricDetails? = null
)

@Serializable
data class GutMetricDetails(
    val metrics: List<GutMetricItem>? = null
)

@Serializable
data class GutMetricItem(
    val summary: MetricSummary? = null,
    val metric: MetricInfo? = null,
    val details: List<MetricDetail>? = null,
    val correlations: List<MetricCorrelation>? = null,
    @SerialName("reported_symptoms")
    val reportedSymptoms: List<ReportedSymptom>? = null
)

@Serializable
data class MetricSummary(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("metric_id")
    val metricId: String? = null,

    val value: Double? = null,
    val inference: String? = null,

    @SerialName("released_at")
    val releasedAt: String? = null,

    @SerialName("is_latest")
    val isLatest: Boolean? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("rating_rank")
    val ratingRank: Int? = null,

    @SerialName("display_description")
    val displayDescription: String? = null
)

@Serializable
data class MetricInfo(
    val id: String? = null,

    @SerialName("metric_id")
    val metricId: String? = null,

    @SerialName("reference_metric_id")
    val referenceMetricId: String? = null,

    val metric: String? = null,

    @SerialName("reference_metric")
    val referenceMetric: String? = null,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("short_name")
    val shortName: String? = null,

    val unit: String? = null,
    val description: String? = null,

    @SerialName("range_type")
    val rangeType: String? = null,

    val category: String? = null,

    @SerialName("sub_category")
    val subCategory: String? = null,

    @SerialName("group_name")
    val groupName: String? = null,

    @SerialName("sub_group_name")
    val subGroupName: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class MetricDetail(
    val id: String? = null,

    @SerialName("metric_id")
    val metricId: String? = null,

    val category: String? = null,
    val title: String? = null,
    val content: String? = null,

    @SerialName("key_points")
    val keyPoints: String? = null,

    val subgroups: String? = null,
    val notes: String? = null,

    @SerialName("content_type")
    val contentType: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class MetricCorrelation(
    val id: String? = null,

    @SerialName("source_metric_id")
    val sourceMetricId: String? = null,

    @SerialName("source_metric_name")
    val sourceMetricName: String? = null,

    @SerialName("source_type")
    val sourceType: String? = null,

    @SerialName("source_unit")
    val sourceUnit: String? = null,

    @SerialName("source_metric")
    val sourceMetric: String? = null,

    @SerialName("target_metric_id")
    val targetMetricId: String? = null,

    val description: String? = null,

    @SerialName("source_inference")
    val sourceInference: String? = null,

    @SerialName("target_inference")
    val targetInference: String? = null,

    @SerialName("short_description")
    val shortDescription: String? = null,

    @SerialName("source_rating_rank")
    val sourceRatingRank: Int? = null,

    @SerialName("source_value")
    val sourceValue: String? = null,

    @SerialName("source_group_name")
    val sourceGroupName: String? = null
)

@kotlinx.serialization.Serializable
data class ReportedSymptom(
    val id: String? = null,
    val name: String? = null
)
