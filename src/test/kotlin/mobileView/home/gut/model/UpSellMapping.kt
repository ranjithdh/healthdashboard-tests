package mobileView.home.gut.model

import kotlinx.serialization.Serializable

@Serializable
data class UpSellMapping(
    val gene_metric_id: String,
    val gene_pgr: String,
    val gut_metric_id: String,
    val gut: String,
    val gene_upsell: String,
    val gut_upsell: String
)



