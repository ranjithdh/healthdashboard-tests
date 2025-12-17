package model.orders

data class Order(
    val blood_data_report: List<BloodDataReport>,
    val order_items: List<OrderItem>,
)