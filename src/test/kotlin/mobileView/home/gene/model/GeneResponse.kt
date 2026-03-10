package mobileView.home.gene.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GeneResponse(
    val status: String? = null,
    val message: String? = null,
    val data: GeneDataWrapper? = null
)

@Serializable
data class GeneDataWrapper(
    val gene: GeneData? = null
)

@Serializable
data class GeneData(
    val data: List<GeneItem>? = null
)

@Serializable
data class GeneItem(
    val id: String? = null,
    val value: Double? = null,
    val inference: String? = null,

    @SerialName("released_at")
    val releasedAt: String? = null,

    @SerialName("is_latest")
    val isLatest: Boolean? = null,

    @SerialName("rating_rank")
    val ratingRank: Int? = null,

    @SerialName("display_description")
    val displayDescription: String? = null,

    val metric: GeneMetric? = null
)

@Serializable
data class GeneMetric(
    val id: String? = null,
    val metric: String? = null,

    @SerialName("metric_id")
    val metricId: String? = null,

    @SerialName("display_name")
    val displayName: String? = null,

    val unit: String? = null,
    val description: String? = null,
    val category: String? = null,

    @SerialName("group_name")
    val groupName: String? = null
)