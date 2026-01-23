package model.addontest

import kotlinx.serialization.Serializable

@Serializable
data class AddOnTests(
    val diagnostic_product_list: OnboardDiagnosticProductList?=null
)

@Serializable
data class OnboardDiagnosticProductList(
    var packages: List<Package>?=null,
    var test_profiles: List<TestProfile>?=null,
    var tests: List<Test>?=null
)

@Serializable
data class Package(
    val code: String?=null,
    val content: Content?=null,
    val created_at: String?=null,
    val description: String?=null,
    val fasting_duration_hr: String?=null,
    val id: String?=null,
    val img_url: String?=null,
    val is_fasting_required: Boolean?=null,
    val name: String?=null,
    val product: Product?=null,
    val report_generation_hr: String?=null,
    val sample_type: String?=null,
    val type: String?=null,
    val updated_at: String?=null
)


@Serializable
data class Content(
    val about: String?=null,
    val fasting_info: String?=null,
    val highlights: List<String>?=null,
    val long_description: String?=null,
    val short_description: String?=null,
    val what_measured_description: String?=null,
    val what_to_expect_description: String?=null,
    val who: List<String>?=null,
)


@Serializable
data class Product(
    val created_at: String?=null,
    val description: String?=null,
    val id: String?=null,
    val img_urls: List<String>?=null,
    val is_active: Boolean?=null,
    val mrp: String?=null,
    val n_rating: Int?=null,
    val name: String?=null,
    val price: String?=null,
    val product_id: String?=null,
    val rating: String?=null,
    val sku: String?=null,
    val stock: Int?=null,
    val type: String?=null,
    val updated_at: String?=null,
    val vendor_id: String?=null,
    val vendor_product_id: String?=null
)


@Serializable
data class Test(
    val code: String?=null,
    val content: Content?=null,
    val created_at: String?=null,
    val description: String?=null,
    val id: String?=null,
    val img_url: String?=null,
    val is_fasting_required: Boolean?=null,
    val name: String?=null,
    val product: Product?=null,
    val report_generation_hr: String?=null,
    val sample_type: String?=null,
    val tags: List<String>?=null,
    val type: String?=null,
    val updated_at: String?=null
)


@Serializable
data class TestProfile(
    val code: String?=null,
    val content: Content?=null,
    val created_at: String?=null,
    val description: String?=null,
    val id: String?=null,
    val img_url: String?=null,
    val is_fasting_required: Boolean?=null,
    val name: String?=null,
    val product: Product?=null,
    val report_generation_hr: String?=null,
    val sample_type: String?=null,
    val tags: List<String>?=null,
    val tests: List<Test>?=null,
    val type: String?=null,
    val updated_at: String?=null
)

