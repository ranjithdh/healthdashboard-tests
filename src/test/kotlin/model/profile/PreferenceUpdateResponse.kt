package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreferenceUpdateResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: PreferenceUpdateData
)

@Serializable
data class PreferenceUpdateData(
    @SerialName("is_updated")
    val isUpdated: Boolean
)
