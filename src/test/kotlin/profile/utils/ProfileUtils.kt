package profile.utils

import model.profile.Address

object ProfileUtils {

     fun buildAddressText(address: Address): String {
        return listOf(
            address.addressHouseNo,
            address.addressLine1,
            address.addressLine2,
            address.city,
            address.state,
            address.pincode,
            address.country
        )
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
    }

}