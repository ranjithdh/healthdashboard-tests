package model

import kotlinx.serialization.Serializable


@Serializable
data class AddAddressResponse(
    val status: String,
    val message: String,
    val data: Data
)

@Serializable
data class Data(
    val di_address: Address
)