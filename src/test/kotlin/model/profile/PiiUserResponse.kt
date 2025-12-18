package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PiiUserResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: PiiUserData
)
@Serializable
data class PiiUserData(
    @SerialName("pii_data")
    val piiData: PiiData
)

@Serializable
data class PiiData(
    @SerialName("_id")
    val id: String,

    @SerialName("lead_id")
    val leadId: String,

    @SerialName("country_code")
    val countryCode: String,

    @SerialName("email")
    val email: String,

    @SerialName("mobile")
    val mobile: String,

    @SerialName("name")
    val name: String,

    @SerialName("dob")
    val dob: String,   // ISO string, convert to LocalDate if needed

    @SerialName("gender")
    val gender: String,

    @SerialName("height")
    val height: Int,

    @SerialName("weight")
    val weight: Int,

    @SerialName("customer_id")
    val customerId: String
)
