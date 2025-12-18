package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val amount: Int?=null,
    val di_order_id: String?=null,
    val id: String?=null,
    val order_item_id: String?=null,
    val product: Product?=null,
    val product_id: String?=null,
    val quantity: Int?=null,
    val vendor_order_status: String?=null
)