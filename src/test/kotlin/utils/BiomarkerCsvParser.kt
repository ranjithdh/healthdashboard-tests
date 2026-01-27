package utils

import model.healthdata.Biomarker
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

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
        fields.add(currentField.toString())
        
        return fields
    }

}
