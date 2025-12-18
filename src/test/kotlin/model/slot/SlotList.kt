package model.slot

import kotlinx.serialization.Serializable

@Serializable
data class SlotList(
    val data: SlotData? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
data class SlotData(
    val slots: List<Slot>? = null
)

@Serializable
data class Slot(
    val booking_count: Int? = null,
    val end_time: String? = null,
    val is_available: Boolean? = null,
    val start_time: String? = null
)