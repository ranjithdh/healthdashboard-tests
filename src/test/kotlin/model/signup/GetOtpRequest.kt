package model.signup

import kotlinx.serialization.Serializable

@Serializable
data class GetOtpRequest(
    val mobile: String,
    val country_code: String,
    val secret_key: String,
    val service: String
)
