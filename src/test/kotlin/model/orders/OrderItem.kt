package model.orders

data class OrderItem(
    val amount: Int,
    val cancel_reason: Any,
    val cancelled_at: Any,
    val consultations: List<Consultation>,
    val di_order_id: String,
    val id: String,
    val order_item_id: String,
    val order_refund_timeline: List<Any>,
    val order_status_timeline: List<OrderStatusTimeline>,
    val product: Product,
    val product_id: String,
    val quantity: Int,
    val vendor_order_status: String
)