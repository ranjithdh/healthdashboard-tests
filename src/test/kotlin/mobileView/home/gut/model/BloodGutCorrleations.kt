package mobileView.home.gut.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BloodGutCorrleations(
    @SerialName("row_number")
    val rowNumber: Int,

    @SerialName("gut metric_ids")
    val gutMetricIds: String,

    @SerialName("Gut Metric")
    val gutMetric: String,

    @SerialName("blood metric_ids")
    val bloodMetricIds: String?,

    @SerialName("Blood Marker")
    val bloodMarker: String,

    @SerialName("Gut Value")
    val gutValue: String,

    @SerialName("Blood Level")
    val bloodLevel: String,

    @SerialName("Description")
    val Description: String
)