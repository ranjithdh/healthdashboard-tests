package profile.utils

import model.profile.Address
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

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



    fun formatDobWithAge(
        utcIso: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        if (utcIso.isNullOrBlank()){
            return ""
        }

        // 1️⃣ Parse UTC ISO
        val instant = Instant.parse(utcIso)

        // 2️⃣ Convert to local timezone
        val localDate = instant.atZone(zoneId).toLocalDate()

        // 3️⃣ Format date
        val dateFormatter =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
        val formattedDate = localDate.format(dateFormatter)

        // 4️⃣ Calculate age
        val age = Period.between(localDate, LocalDate.now(zoneId)).years

        return "$formattedDate ($age Years)"
    }

}