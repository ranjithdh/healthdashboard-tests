package model.orders

data class OrderStatusTimeline(
    val active: Boolean,
    val completed: Boolean,
    val expected_at: String,
    val icon: String,
    val status: String,
    val status_updated_at: Any
)