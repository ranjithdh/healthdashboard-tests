package model.orders

data class Product(
    val id: String,
    val name: String,
    val type: String,
    val vendor_product_id: String
)