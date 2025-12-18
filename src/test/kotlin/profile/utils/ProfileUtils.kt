package profile.utils

import model.profile.Address
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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
        if (utcIso.isNullOrBlank()) {
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


    fun formatDobToDdMmYyyy(
        utcIso: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        if (utcIso.isNullOrBlank()) return ""

        // 1️⃣ Parse UTC ISO
        val instant = Instant.parse(utcIso)

        // 2️⃣ Convert to local timezone
        val localDate = instant.atZone(zoneId).toLocalDate()

        // 3️⃣ Format as dd/MM/yyyy
        val formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

        return localDate.format(formatter)
    }

    fun calculateBMIValues(heightCm: Double, weightKg: Double): String {
        val heightMeters = heightCm / 100
        val bmi = weightKg / (heightMeters * heightMeters)
        return String.format("%.2f", bmi)
    }


    fun bmiCategoryValues(bmi: Double): String {
        if (bmi < 18) return "Unusual BMI. Re-check entered values."
        if (bmi < 18.5) return "Underweight"
        if (bmi < 25) return "Normal"
        if (bmi < 30) return "Overweight"
        return "Obese"
    }


}