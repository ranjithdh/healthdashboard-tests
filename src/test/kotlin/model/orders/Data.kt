package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrdersData(
    val orders: List<Order>?=null
)