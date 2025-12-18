package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class BloodDataReport(
    val di_order: DiOrder,
    val id: String,
    val name: String,
    val order_id: String,
    val status: String,
)