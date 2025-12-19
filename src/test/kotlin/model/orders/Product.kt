package model.orders

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String?=null,
    val name: String?=null,
    val type: String?=null,
    val vendor_product_id: String?=null
)