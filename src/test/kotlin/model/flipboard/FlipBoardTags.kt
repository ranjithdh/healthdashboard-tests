package model.flipboard

import kotlinx.serialization.Serializable

@Serializable
data class FlipBoardTags(
    val tags: List<String>?=null,
    val total: Int?=null
)