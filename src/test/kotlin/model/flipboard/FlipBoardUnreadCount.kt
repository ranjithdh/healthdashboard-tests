package model.flipboard

import kotlinx.serialization.Serializable

@Serializable
data class FlipBoardUnreadCount(
    val unread_count: Int?=null,
    val user_id: Int?=null
)