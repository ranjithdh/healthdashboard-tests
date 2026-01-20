package model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceResponse(
    val status: String? = null,
    val message: String? = null,
    val data: ServiceData? = null
)

@Serializable
data class ServiceData(
    val products: List<ServiceProduct>? = null,
    val pagination: ServicePagination? = null
)

@Serializable
data class ServiceProduct(
    val id: String? = null,
    val product_id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: List<String>? = null,
    val vendor_id: String? = null,
    val vendor_product_id: String? = null,
    val price: String? = null,
    val mrp: String? = null,
    val sku: String? = null,
    val stock: Int? = null,
    val img_urls: List<String>? = null,
    val tags: List<String>? = null,
    val rating: String? = null,
    val n_rating: Int? = null,
    val is_active: Boolean? = null,
    val type: String? = null,
    val meta_data: ServiceMetaData? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val item_purchase_status: String? = null,
    val vendor: ServiceVendor? = null,
    val vendor_name: String? = null
)

@Serializable
data class ServiceMetaData(
    val duration: Int? = null,
    val name: String? = null,
    val experience: String? = null,
    val title: String? = null,
    val photo_sm: String? = null,
    val photo_bg: String? = null,
    val credentials: List<String>? = null,
    val bio: String? = null,
    val inclusions: List<String>? = null,
    val exclusions: List<String>? = null,
    val calendly_link: String? = null
)

@Serializable
data class ServicePagination(
    val total: Int? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
    val limit: Int? = null
)

@Serializable
data class ServiceVendor(
    val name: String? = null
)
