package profile.utils

import com.microsoft.playwright.Locator
import model.profile.Address
import profile.model.QuestionAnswer
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.LinkedHashMap

object ProfileUtils {


     val answersStored: MutableMap<String, QuestionAnswer> = LinkedHashMap()

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

    fun calculateBMIValues(heightCm: Float, weightKg: Float): Float {
        val heightMeters = heightCm / 100
        val bmi = weightKg / (heightMeters * heightMeters)
        return String.format("%.2f", bmi).toFloat()
    }


    fun bmiCategoryValues(bmi: Float): String {
        if (bmi < 18) return "Unusual BMI. Re-check entered values."
        if (bmi < 18.5) return "Underweight"
        if (bmi < 25) return "Normal"
        if (bmi < 30) return "Overweight"
        return "Obese"
    }


    fun formatFlotTwoDecimal(value: Float): String {
        val formatted = String.format("%.2f", value)
        return if (formatted.endsWith(".00")) {
            formatted.dropLast(3)
        } else {
            formatted
        }
    }

    fun isSelected(button: Locator): Boolean {
        val clazz = button.getAttribute("class") ?: return false
        return clazz.contains("border-[#6d6d79]") &&
                clazz.contains("bg-[#25252a]")
    }

    fun isButtonChecked(button: Locator): Boolean {
        return isSelected(button)
    }

    fun assertExclusiveSelected(
        exclusive: Locator,
        others: List<Locator>
    ) {
        check(isSelected(exclusive)) {
            "Expected exclusive option to be selected"
        }

        others.forEach {
            check(!isSelected(it)) {
                "Other options must be unselected when exclusive option is selected"
            }
        }
    }

    /*  fun assertConditionSelected(
          selected: Locator,
          notSure: Locator,
          none: Locator
      ) {
          check(isSelected(selected))
          check(!isSelected(notSure)) { "'I'm not sure' must be unselected" }
          check(!isSelected(none)) { "'None of the above' must be unselected" }
      }*/


}