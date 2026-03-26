package mobileView.home.gene.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneMetricResponse(
    val status: String,
    val message: String,
    val data: GeneMetricData
)

@Serializable
data class GeneMetricData(
    val metrics: List<GeneMetricItem>
)

@Serializable
data class GeneMetricItem(
    val summary: GeneMetricSummary,
    val metric: GeneMetricInfo,
    val details: List<GeneMetricDetail>,
    val correlations: List<GeneMetricCorrelation>
)

@Serializable
data class GeneMetricSummary(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("metric_id")
    val metricId: String,

    val value: String?,          // null in JSON
    val inference: String,

    @SerialName("released_at")
    val releasedAt: String,

    @SerialName("is_latest")
    val isLatest: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("rating_rank")
    val ratingRank: Int,

    @SerialName("display_description")
    val displayDescription: String
)

@Serializable
data class GeneMetricInfo(
    val id: String,

    @SerialName("metric_id")
    val metricId: String,

    @SerialName("reference_metric_id")
    val referenceMetricId: String?,

    val metric: String,

    @SerialName("reference_metric")
    val referenceMetric: String?,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("short_name")
    val shortName: String,

    val unit: String?,
    val description: String?,

    @SerialName("range_type")
    val rangeType: String?,

    val category: String,

    @SerialName("sub_category")
    val subCategory: String?,

    @SerialName("group_name")
    val groupName: String,

    @SerialName("sub_group_name")
    val subGroupName: String?,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class GeneMetricDetail(
    val id: String,

    @SerialName("metric_id")
    val metricId: String,

    val category: String,
    val title: String,
    val content: String,

    @SerialName("key_points")
    val keyPoints: String?,

    val subgroups: String?,
    val notes: String?,

    @SerialName("content_type")
    val contentType: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class GeneMetricCorrelation(
    val id: String,

    @SerialName("source_metric_id")
    val sourceMetricId: String,

    @SerialName("source_metric_name")
    val sourceMetricName: String,

    @SerialName("source_type")
    val sourceType: String,

    @SerialName("source_unit")
    val sourceUnit: String?,

    @SerialName("source_metric")
    val sourceMetric: String,

    @SerialName("target_metric_id")
    val targetMetricId: String,

    val description: String?,

    @SerialName("source_inference")
    val sourceInference: String,

    @SerialName("target_inference")
    val targetInference: String,

    @SerialName("short_description")
    val shortDescription: String?,

    @SerialName("source_rating_rank")
    val sourceRatingRank: Int,

    @SerialName("source_value")
    val sourceValue: String?,

    @SerialName("source_group_name")
    val sourceGroupName: String,

    val condition: String?
)