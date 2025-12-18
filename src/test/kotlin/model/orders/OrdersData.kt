package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class Orders(
    val data: OrdersData?=null,
    val message: String?=null,
    val status: String?=null
)