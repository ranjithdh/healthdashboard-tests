package model

import kotlinx.serialization.Serializable

@Serializable
data class HomeData(
    val age: String?=null,
    val biological_age: String?=null,
    val diagnostics: List<Diagnostic>?=null,
    val gender: String?=null,
    val next_steps: NextSteps?=null,
    val services: List<Service>?=null,
    val status: String?=null,
    val message: String?=null,
)

@Serializable
data class HomeDataResponse(
    val status: String? = null,
    val message: String? = null,
    val data: HomeData? = null
)

@Serializable
data class CreditPoints(
    val points: Int?=null,
    val status: String?=null
)

@Serializable
data class Diagnostic(
    val img_url: String?=null,
    val last_updated_at: String?=null,
    val mp_order_id: String?=null,
    val name: String?=null,
    val order_id: String?=null,
    val product_id: String?=null,
    val source: String?=null,
    val status: String?=null,
    val type: String?=null,
    val blood_test_appointment_date: String?=null
)

@Serializable
data class FreeConsultation(
    val product_id: String?=null,
    val status: String?=null
)

@Serializable
data class NextSteps(
    val credit_points: CreditPoints?=null,
    val free_consultation: FreeConsultation?=null,
    val personalized_action: PersonalizedAction?=null,
    val video_tutorial: VideoTutorial?=null,
    val diagnostics: List<Diagnostic>?=null
)

@Serializable
data class PersonalizedAction(
    val status: String?=null
)

@Serializable
data class Service(
    val img_url: String?=null,
    val last_updated_at: String?=null,
    val mp_order_id: String?=null,
    val name: String?=null,
    val product_id: String?=null,
    val source: String?=null,
    val status: String?=null,
    val type: String?=null
)

@Serializable
data class VideoTutorial(
    val status: String?=null,
    val url: String?=null,
    val video_type: String?=null
)