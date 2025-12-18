package model.orders

data class Orders(
    val data: OrdersData,
    val message: String,
    val status: String
)