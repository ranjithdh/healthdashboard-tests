package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PiDataObject(
    @SerialName("pii_data")
    val piiData: PiiData? = null
)

@Serializable
data class PiiData(
    @SerialName("_id")
    val id: String? = null,

    @SerialName("lead_id")
    val leadId: String? = null,

    @SerialName("communication_address")
    val communicationAddress: CommunicationAddress? = null,

    @SerialName("country_code")
    val countryCode: String? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("dob")
    val dob: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("gender")
    val gender: String? = null,

    @SerialName("height")
    val height: Double? = null,

    @SerialName("is_customer")
    val isCustomer: Boolean? = null,

    @SerialName("lead_source")
    val leadSource: String? = null,

    @SerialName("mobile")
    val mobile: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("weight")
    val weight: Double? = null,

    @SerialName("customer_id")
    val customerId: String? = null,

    @SerialName("payment_amount_inr")
    val paymentAmountInr: String? = null,

    @SerialName("payment_date")
    val paymentDate: String? = null,

    @SerialName("payment_fee")
    val paymentFee: Double? = null,

    @SerialName("payment_id")
    val paymentId: String? = null
)

@Serializable
data class CommunicationAddress(
    @SerialName("address")
    val address: String? = null,

    @SerialName("pincode")
    val pincode: String? = null,

    @SerialName("address_line_1")
    val addressLine1: String? = null,

    @SerialName("city")
    val city: String? = null,

    @SerialName("state")
    val state: String? = null,

    @SerialName("country")
    val country: String? = null,

    @SerialName("_id")
    val id: String? = null
)