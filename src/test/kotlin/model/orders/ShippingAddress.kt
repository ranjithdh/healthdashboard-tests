package model.orders

data class ShippingAddress(
    val address: String,
    val address_house_no: String,
    val address_line_1: String,
    val address_line_2: String,
    val address_mobile: String,
    val address_name: String,
    val address_type: Any,
    val city: String,
    val country: String,
    val created_at: String,
    val id: String,
    val is_primary: Boolean,
    val pincode: String,
    val state: String,
    val updated_at: String,
    val user_id: String
)