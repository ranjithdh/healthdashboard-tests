package mobileView.home.gut.model

import kotlinx.serialization.Serializable

@Serializable
data class GutResponse(
    val status: String? = null,
    val message: String? = null,
    val data: GutDataWrapper? = null
)

@Serializable
data class GutDataWrapper(
    val gut: GutData? = null
)

@Serializable
data class GutData(
    val data: List<GutMetricData>? = null
)

@Serializable
data class GutMetricData(
    val id: String? = null,
    val value: Double? = null,
    val inference: String? = null,
    val released_at: String? = null,
    val is_latest: Boolean? = null,
    val rating_rank: Int? = null,
    val display_description: String? = null,
    val metric: GutMetric? = null
)

@Serializable
data class GutMetric(
    val id: String? = null,
    val metric: String? = null,
    val metric_id: String? = null,
    val display_name: String? = null,
    val unit: String? = null,
    val description: String? = null,
    val category: String? = null,
    val group_name: String? = null
)