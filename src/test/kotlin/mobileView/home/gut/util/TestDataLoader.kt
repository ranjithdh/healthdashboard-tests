package mobileView.home.gut.util

import kotlinx.serialization.json.Json
import mobileView.home.gut.model.UpSellMapping

object TestDataLoader {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadGeneGutMappings(): List<UpSellMapping> {
        val text = this::class.java
            .getResource("/file/gene_gut_upsell_mapping.json")
            ?.readText()
            ?: error("JSON file not found")

        return json.decodeFromString(text)
    }
}