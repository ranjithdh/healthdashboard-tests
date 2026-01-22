package utils

import model.healthdata.Biomarker
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Utility class to parse biomarker data from CSV export file.
 */
object BiomarkerCsvParser {
    
    /**
     * Parse a CSV file and return a list of Biomarker objects.
     * 
     * Expected CSV format:
     * "Biomarker Name","Status","Value","Unit","Ideal Range","Last Updated","System Name"
     */
    fun parse(filePath: String): List<Biomarker> {
        logger.info { "Parsing biomarker CSV from: $filePath" }
        
        val file = File(filePath)
        require(file.exists()) { "CSV file not found: $filePath" }
        
        val lines = file.readLines()
        require(lines.isNotEmpty()) { "CSV file is empty" }
        
        // Skip header row
        val dataLines = lines.drop(1)
        
        val biomarkers = dataLines.mapNotNull { line ->
            try {
                parseLine(line)
            } catch (e: Exception) {
                logger.warn { "Failed to parse line: $line - ${e.message}" }
                null
            }
        }
        
        logger.info { "Parsed ${biomarkers.size} biomarkers from CSV" }
        return biomarkers
    }
    
    /**
     * Parse a single CSV line into a Biomarker object.
     * Handles quoted fields with commas inside.
     */
    private fun parseLine(line: String): Biomarker? {
        if (line.isBlank()) return null
        
        val fields = parseQuotedCsv(line)
        
        if (fields.size < 7) {
            logger.warn { "Invalid line format (expected 7 fields, got ${fields.size}): $line" }
            return null
        }
        
        return Biomarker(
            name = fields[0].trim(),
            status = fields[1].trim(),
            value = fields[2].trim(),
            unit = fields[3].trim(),
            idealRange = fields[4].trim(),
            lastUpdated = fields[5].trim(),
            systemName = fields[6].trim()
        )
    }
    
    /**
     * Parse a CSV line with proper handling of quoted fields.
     */
    private fun parseQuotedCsv(line: String): List<String> {
        val fields = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields.add(currentField.toString())
                    currentField.clear()
                }
                else -> currentField.append(char)
            }
        }
        // Add the last field
        fields.add(currentField.toString())
        
        return fields
    }
    
    /**
     * Parse CSV and group biomarkers by system name.
     */
    fun parseGroupedBySystem(filePath: String): Map<String, List<Biomarker>> {
        return parse(filePath).groupBy { it.systemName }
    }
    
    /**
     * Get all unique system names from the CSV.
     */
    fun getSystemNames(filePath: String): List<String> {
        return parse(filePath)
            .map { it.systemName }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()
    }
    
    /**
     * Get biomarkers filtered by status.
     */
    fun getBiomarkersByStatus(filePath: String, status: String): List<Biomarker> {
        return parse(filePath).filter { it.status.equals(status, ignoreCase = true) }
    }
    
    /**
     * Get biomarkers that need attention (not Normal/Optimal).
     */
    fun getBiomarkersNeedingAttention(filePath: String): List<Biomarker> {
        return parse(filePath).filter { it.needsAttention() }
    }
}
