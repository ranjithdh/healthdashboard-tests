package mobileView

import utils.DateHelper
import utils.logger.logger
import java.time.format.DateTimeFormatter
import java.util.Locale

object LabTestDateHelper {

    fun getPhlebotomistAssignedDate(appointmentDate: String?): String {
        val localDate = DateHelper.utcToLocalDateTime(appointmentDate)

        val assignedDate = localDate.minusDays(1).withHour(21).withMinute(0).withSecond(0)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM, hh:mm a", Locale.ENGLISH)
        val formattedDateTime = dateTimeFormatter.format(assignedDate)

        logger.info {
            "formattedDateTime.....$formattedDateTime"
        }

        return formattedDateTime
    }

    fun getSampleCollectionDate(appointmentDate: String?): String {
        val localDate = DateHelper.utcToLocalDateTime(appointmentDate)

        val endTime = localDate.plusMinutes(30)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM, hh:mm", Locale.ENGLISH)
        val endTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

        val formattedDateTime = dateTimeFormatter.format(localDate)
        val formattedEndTime = endTimeFormatter.format(endTime)

        logger.info {
            "getLabProcessingDate.....${formattedDateTime.plus("-").plus(formattedEndTime)}"
        }

        return formattedDateTime.plus(" - ").plus(formattedEndTime)

    }

    fun getDashBoardReadyToViewDate(appointmentDate: String?): String {
        val localDate = DateHelper.utcToLocalDateTime(appointmentDate)

        val assignedDate = localDate.plusDays(2)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM, hh:mm a", Locale.ENGLISH)
        val formattedDateTime = dateTimeFormatter.format(assignedDate)

        logger.info {
            "getDashBoardReadyToViewDate.....${formattedDateTime}"
        }
        return formattedDateTime
    }
}