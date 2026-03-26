package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersResponse(
    @SerialName("status")
    val status: String,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: UsersData
)

@Serializable
data class UsersData(
    @SerialName("users")
    val users: List<UserItem>
)

@Serializable
data class UserItem(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userUuid: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("mobile")
    val mobile: String? = null,
    @SerialName("lead_id")
    val leadId: String? = null
)
