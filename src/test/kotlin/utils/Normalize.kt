package utils

object Normalize {


    fun refactorTimeZone(timeZone: String): String {
        return if (timeZone == "Etc/UTC"){
            "UTC"
        }else{
            timeZone
        }
    }
}