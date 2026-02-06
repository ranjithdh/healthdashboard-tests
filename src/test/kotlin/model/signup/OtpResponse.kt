package model.signup

import kotlinx.serialization.Serializable

@Serializable
data class OtpResponse(
    val status: String,
    val message: String,
    val data: OtpData
)

@Serializable
data class OtpData(
    val otp: String? = null
)
