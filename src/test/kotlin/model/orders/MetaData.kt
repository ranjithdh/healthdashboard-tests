package model.orders

data class MetaData(
    val fasting: String,
    val orderId: String,
    val order_no: String,
    val products: String,
    val ref_order_id: String,
    val status: String
)