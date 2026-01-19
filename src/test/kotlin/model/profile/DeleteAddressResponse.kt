package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteAddressResponse(
    val status: String,
    val message: String,

    val data: DeleteAddressData
)

@Serializable
data class DeleteAddressData(
    @SerialName("is_updated")
    val isUpdated: Boolean,

    @SerialName("is_primary_switched")
    val isPrimarySwitched: Boolean
)
