package mobileView.home.gut.util

object GutUtility {
    fun toKebabCase(input: String): String {
        return input
            .trim()
            .lowercase()
           // .replace(Regex("[^a-z0-9\\s-()]"), "") // remove special chars except space & -
            .replace(Regex("[^a-z0-9\\s&,()-]"), "") // keep & and ,
            .replace(Regex("\\s+"), "-")        // replace spaces with -
    }

    fun gutSourceType(sourceType: String?): String {
        return when (sourceType) {
            "gene" -> "Genetic Trait"
            "gut" -> "Gut Marker"
            "blood" -> "Blood Biomarker"
            "wearable" -> "Device Marker"
            else -> ""
        }
    }

    fun String.normalizeQuotes(): String =
        this.replace("’", "'")
            .replace("‘", "'")
}

