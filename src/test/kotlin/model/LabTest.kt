package model

import kotlinx.serialization.Serializable

@Serializable
data class LabTestResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LabTestData? = null
)

@Serializable
data class LabTestData(
    val diagnostic_product_list: DiagnosticProductList? = null
)

@Serializable
data class DiagnosticProductList(
    val packages: List<LabTestPackage>? = null,
    val test_profiles: List<LabTestProfile>? = null,
    val tests: List<LabTestItem>? = null
)

@Serializable
data class LabTestPackage(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val tags: List<String>? = null,
    val content: LabTestContent? = null,
    val sample_type: String? = null,
    val is_fasting_required: Boolean? = null,
    val fasting_duration_hr: String? = null,
    val report_generation_hr: String? = null,
    val type: String? = null,
    val product: LabTestProduct? = null,
    val tests: List<LabTestItem>? = null
)

@Serializable
data class LabTestProfile(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val description: String? = null,
    val img_url: String? = null,
    val tags: List<String>? = null,
    val content: LabTestContent? = null,
    val sample_type: String? = null,
    val is_fasting_required: Boolean? = null,
    val fasting_duration_hr: String? = null,
    val report_generation_hr: String? = null,
    val type: String? = null,
    val product: LabTestProduct? = null,
    val tests: List<LabTestItem>? = null
)

@Serializable
data class LabTestItem(
    val id: String? = null,
    val name: String? = null,
    val code: String? = null,
    val description: String? = null,
    val sample_type: String? = null,
    val tags: List<String>? = null,
    val content: LabTestContent? = null,
    val is_fasting_required: Boolean? = null,
    val fasting_duration_hr: String? = null,
    val report_generation_hr: String? = null,
    val type: String? = null,
    val product: LabTestProduct? = null
)

@Serializable
data class LabTestContent(
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
    val fasting_info: String? = null
)

@Serializable
data class LabTestProduct(
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
    val vendor: LabTestVendor? = null
)

@Serializable
data class LabTestVendor(
    val id: String? = null,
    val vendor_id: String? = null,
    val name: String? = null,
    val storefront_access_token: String? = null,
    val admin_api_access_token: String? = null
)

