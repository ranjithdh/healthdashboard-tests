package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val blood_data_report: List<BloodDataReport>?=null,
    val order_items: List<OrderItem>?=null,
    val appointment_date:String?=null,
)