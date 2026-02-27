package mobileView.home.gut.model

import com.google.gson.annotations.SerializedName

data class BloodGutCorrleations(
    @SerializedName("row_number")
    val rowNumber: Int,

    @SerializedName("gut metric_ids")
    val gutMetricIds: String,

    @SerializedName("Gut Metric")
    val gutMetric: String,

    @SerializedName("blood metric_ids")
    val bloodMetricIds: String,

    @SerializedName("Blood Marker")
    val bloodMarker: String,

    @SerializedName("Gut Value")
    val gutValue: String,

    @SerializedName("Blood Level")
    val bloodLevel: String,

    @SerializedName("Description")
    val description: String
)