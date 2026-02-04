package model.healthdata

import kotlinx.serialization.Serializable


@Serializable
data class Range(
    val age_variation: String?=null,
    val created_at: String?=null,
    val display_description: String?=null,
    val display_name: String?=null,
    val display_rating: String?=null,
    val id: String?=null,
    val metric: String?=null,
    val metric_id: String?=null,
    val range: String?=null,
    val rating_rank: Int?=null,
    val sex_variation: String?=null,
    val time_variation: String?=null,
    val unit: String?=null,
    val updated_at: String?=null
)