package mobileView.home.gene.model

import kotlinx.serialization.Serializable

@Serializable
data class BloodGeneMapping(
    val bloodMetricId: String? = null,
    val bloodMarker: String? = null,
    val geneMetricId: String? = null,
    val gene: String? = null,
    val revisedRating: String? = null,
    val bloodLevel: String? = null,
    val description: String? = null
)