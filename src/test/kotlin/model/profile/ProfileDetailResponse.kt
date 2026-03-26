package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDetailResponse(
    val status: String? = null,
    val message: String? = null,
    val data: ProfileDetailData? = null
)

@Serializable
data class ProfileDetailData(
    @SerialName("ht_user_verification")
    val htUserVerification: HtUserVerification? = null,
    @SerialName("pii_data")
    val piiData: PiiDetailData? = null
)

@Serializable
data class HtUserVerification(
    val id: String? = null,
    val name: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    @SerialName("lead_id")
    val leadId: String? = null
)

@Serializable
data class PiiDetailData(
    @SerialName("_id")
    val id: String? = null,
    @SerialName("lead_id")
    val leadId: String? = null,
    @SerialName("communication_address")
    val communicationAddress: ProfileCommunicationAddress? = null,
    val dob: String? = null,
    val gender: String? = null,
    val name: String? = null,
    val height: Int? = null,
    val weight: Int? = null
)

@Serializable
data class ProfileCommunicationAddress(
    val address: String? = null,
    val pincode: String? = null,
    @SerialName("address_line_1")
    val addressLine1: String? = null,
    @SerialName("address_line_2")
    val addressLine2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    @SerialName("address_house_no")
    val addressHouseNo: String? = null,
    @SerialName("address_name")
    val addressName: String? = null
)
