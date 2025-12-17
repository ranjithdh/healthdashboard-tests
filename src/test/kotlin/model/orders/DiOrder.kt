package model.orders

data class DiOrder(
    val address_id: Any,
    val appointment_date: String,
    val barcode: Any,
    val cancellation_reason: Any,
    val cancelled_at: Any,
    val created_at: String,
    val id: String,
    val is_manual: Boolean,
    val meta: Meta,
    val meta_data: MetaData,
    val order_id: String,
    val product_id: String,
    val ref_order_id: String,
    val report_generated_at: Any,
    val rescheduled_at: Any,
    val sample_collection_review: Any,
    val source: String,
    val status: String,
    val tracking_id: Any,
    val tracking_link: Any,
    val updated_at: String,
    val user_id: String,
    val whatsapp_msg_sent_at: Any
)