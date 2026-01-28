package model.healthdata

/**
 * Data class representing a biomarker from the Health Data page.
 * Maps to the CSV export format with fields for name, status, value, unit, range, and system.
 */
data class Biomarker(
    val name: String,
    val status: String,
    val value: String,
    val unit: String,
    val idealRange: String,
    val lastUpdated: String,
    val systemName: String
) {
    /**
     * Returns the formatted value with unit (e.g., "43.3 %")
     */
    fun formattedValue(): String = if (unit.isNotBlank()) "$value $unit" else value
    
    /**
     * Checks if this biomarker has a "normal" status
     */
    fun isNormal(): Boolean = status.equals("Normal", ignoreCase = true) || 
                               status.equals("Optimal", ignoreCase = true)
    
    /**
     * Checks if this biomarker needs attention (not normal)
     */
    fun needsAttention(): Boolean = !isNormal()
    
    companion object {
        /**
         * All possible status values
         */
        val STATUS_VALUES = listOf(
            "Normal", "Optimal", "High", "Low", 
            "Borderline High", "Borderline Low", 
            "Mildly High", "Mildly Elevated", "Monitor"
        )
        
        /**
         * All health system categories
         */
        val SYSTEMS = listOf(
            "Blood Health",
            "Cancer Markers", 
            "Heart Health",
            "Hormone Health",
            "Immune Health",
            "Inflammation",
            "Kidney Health",
            "Liver Health",
            "Metabolic Health",
            "Nutrients, Vitamins, and Minerals",
            "Thyroid Health"
        )
    }
}
