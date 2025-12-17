package model.orders

data class Consultation(
    val completed_at: Any,
    val consultation_id: String,
    val id: String,
    val is_rescheduled: Boolean,
    val order_item_id: String,
    val scheduled_at: Any,
    val source_order_item_id: String,
    val started_at: Any,
    val status: String,
    val user_id: String
)