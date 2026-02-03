package model.healthdata

import kotlinx.serialization.Serializable


@Serializable
data class HealthData(
    val data: Data?=null,
    val message: String?=null,
    val status: String?=null
)


@Serializable
data class Data(
    val blood: Blood?=null,
    val gene: Gene?=null,
    val gut: Gut?=null
)


@Serializable
data class Blood(
    val data: List<BiomarkerInfo>
)


@Serializable
data class Gene(
    val data: List<BiomarkerInfo>
)


@Serializable
data class Gut(
    val data: List<BiomarkerInfo>
)


@Serializable
data class BiomarkerInfo(
    val causes: List<Cause>?=null,
    val created_at: String?=null,
    val display_description: String?=null,
    val display_name: String?=null,
    val display_rating: String?=null,
    val group_name: String?=null,
    val id: String?=null,
    val identifier: String?=null,
    val is_latest: Boolean?=null,
    val metric_id: String?=null,
    val range: String?=null,
    val ranges: List<Range>?=null,
    val released_at: String?=null,
    val trend_arrow: String?=null,
    val unit: String?=null,
    val updated_at: String?=null,
    val user_id: String?=null,
    val value: Double?=null
)

@Serializable
data class Cause(
    val id: String?=null,
    val name: String?=null,
    val type: String?=null
)