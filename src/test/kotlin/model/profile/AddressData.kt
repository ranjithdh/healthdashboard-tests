package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAddressResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: UserAddressData
)
@Serializable
data class UserAddressData(
    @SerialName("address_list")
    val addressList: List<UserAddressItem>
)
@Serializable
data class UserAddressItem(
    @SerialName("address_id")
    val addressId: String,

    @SerialName("address")
    val address: Address
)
@Serializable
data class Address(
    @SerialName("address")
    val address: String,

    @SerialName("pincode")
    val pincode: String,

    @SerialName("address_line_1")
    val addressLine1: String,

    @SerialName("address_line_2")
    val addressLine2: String?="",

    @SerialName("city")
    val city: String,

    @SerialName("state")
    val state: String,

    @SerialName("country")
    val country: String,

    @SerialName("address_type")
    val addressType: String? = "",

    @SerialName("address_house_no")
    val addressHouseNo: String? = "",

    @SerialName("address_mobile")
    val addressMobile: String? = "",

    @SerialName("address_name")
    val addressName: String? = "",

    @SerialName("is_primary")
    val isPrimary: Boolean
)
