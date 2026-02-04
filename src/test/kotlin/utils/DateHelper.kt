package utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateHelper {

    const val SERVER_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    fun utcToLocalDateTime(utcTime: String?): LocalDateTime {
        return if (utcTime != null) {
            val serverFormatter = DateTimeFormatter.ofPattern(SERVER_FORMAT)
            val utcDate = LocalDateTime.parse(utcTime, serverFormatter)
            val utcTimeStamp = ZonedDateTime.of(utcDate, ZoneId.of("UTC"))
            utcTimeStamp.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        } else {
            LocalDateTime.now()
        }
    }

    fun localDateTimeToUtc(localDateTime: LocalDateTime?): String {
        val formatter = DateTimeFormatter.ofPattern(SERVER_FORMAT)
        val zonedLocal = (localDateTime ?: LocalDateTime.now()).atZone(ZoneId.systemDefault())
        return zonedLocal.withZoneSameInstant(ZoneId.of("UTC")).format(formatter)
    }

    fun getTomorrowDate(): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd")
        return dateFormatter.format(LocalDateTime.now().plusDays(1)) ?: dateFormatter.format(LocalDateTime.now())
    }

    fun formatDateWithOrdinal(date: java.time.LocalDate): String {
        val day = date.dayOfMonth
        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
        val formatter = DateTimeFormatter.ofPattern("EEEE, d'$suffix' MMMM", java.util.Locale.ENGLISH)
        return date.format(formatter)
    }
}
