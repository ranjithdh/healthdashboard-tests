package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class DiOrder(
    val appointment_date: String?=null,
    val created_at: String?=null,
    val id: String?=null,
    val is_manual: Boolean?=null,
    val order_id: String?=null,
    val product_id: String?=null,
    val ref_order_id: String?=null,
    val source: String?=null,
    val status: String?=null,
    val updated_at: String?=null,
    val user_id: String?=null,
)