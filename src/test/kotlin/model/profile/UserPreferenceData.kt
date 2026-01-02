package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferenceResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: PreferenceData
)

@Serializable
data class PreferenceData(
    @SerialName("preference")
    val preference: Preference
)

@Serializable
data class Preference(
    @SerialName("communication_preference")
    val communicationPreference: String
)
