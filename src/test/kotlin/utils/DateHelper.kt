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
            return utcTimeStamp.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        }else{
            LocalDateTime.now()
        }
    }

}
