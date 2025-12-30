package model.signup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: VerifyOtpData
)

@Serializable
data class VerifyOtpData(
    @SerialName("is_verified")
    val isVerified: Boolean,

    @SerialName("is_paid")
    val isPaid: Boolean,

    @SerialName("is_eligible")
    val isEligible: Boolean,

    @SerialName("is_free_user")
    val isFreeUser: Boolean,

    @SerialName("ht_coupon")
    val htCoupon: String? = null,

    @SerialName("lead_id")
    val leadId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("user_uuid")
    val userUuid: String,

    @SerialName("profile_id")
    val profileId: String,

    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String
)
