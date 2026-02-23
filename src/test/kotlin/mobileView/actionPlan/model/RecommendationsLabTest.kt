package mobileView.actionPlan.model

import kotlinx.serialization.Serializable
import model.orders.DiOrder

@Serializable
data class RecommendationLabTest(
    val status: String? = null,
    val message: String? = null,
    val data: RecommendationLabTestData? = null
)

@Serializable
data class RecommendationLabTestData(
    val diagnostic_product_list: RecommendationDiagnosticProductList? = null
)

@Serializable
data class RecommendationDiagnosticProductList(
    val packages: List<RecommendationLabTestPackage>? = null,
    val test_profiles: List<RecommendationLabTestPackage>? = null,
    val tests: List<RecommendationLabTestPackage>? = null
)

@Serializable
data class RecommendationLabTestPackage(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val tags: List<String>? = null,
    val content:  RecommendationLabTestContent? = null,
    val sample_type: String? = null,
    val is_fasting_required: Boolean? = null,
    val fasting_duration_hr: String? = null,
    val report_generation_hr: String? = null,
    val type: String? = null,
    val product:  RecommendationLabTestProduct? = null,
    val di_order:  DiOrder? = null,
    val di_kit:  RecommendationDiKit? = null,
)



@Serializable
data class  RecommendationLabTestContent(
    val about: String? = null,
    val short_description: String? = null,
    val long_description: String? = null,
    val preparation: String? = null,
    val highlights: List<String>? = null,
    val what_measured_description: String? = null,
    val what: List<String>? = null,
    val what_measured: Map<String, List<String>>? = null,
    val who: List<String>? = null,
    val who_v2: Map<String, List<String>>? = null,
    val what_to_expect_description: String? = null,
    val what_to_expect: Map<String, List<String>>? = null,
    val why: List<String>? = null,
    val why_with_dh: List<String>? = null,
    val why_test: List<String>? = null,
    val how: List<String>? = null,
    val biomarkers_description: String? = null,
    val biomarkers_tested: Map<String, List<String>>? = null,
    val fasting_info: String? = null,
    val when_to_take: String? = null
)

@Serializable
data class  RecommendationLabTestProduct(
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
    val vendor:  RecommendationLabTestVendor? = null
)

@Serializable
data class  RecommendationLabTestVendor(
    val id: String? = null,
    val vendor_id: String? = null,
    val name: String? = null,
    val storefront_access_token: String? = null,
    val admin_api_access_token: String? = null
)

@Serializable
data class  RecommendationDiKit(
    val id: String? = null,
    val user_id: String? = null,
    val order_id: String? = null,
    val order_tracking_link: String? = null,
    val address_id: String? = null,
    val product_id: String? = null,
    val product: String? = null,
    val sample_type: String? = null,
    val return_id: String? = null,
    val return_tracking_link: String? = null,
    val order_status: String? = null,
    val barcode: String? = null,
    val return_status: String? = null,
    val return_reason: String? = null,
    val returned_at: String? = null,
    val ordered_at: String? = null,
    val delivered_at: String? = null,
    val user_verification_id: String? = null,
    val tracking_id: String? = null,
    val source: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)


