package model.healthdata


data class Biomarker(
    val name: String,
    val status: String,
    val value: String,
    val unit: String,
    val idealRange: String,
    val lastUpdated: String,
    val systemName: String
) {
    fun formattedValue(): String = if (unit.isNotBlank()) "$value $unit" else value
}
