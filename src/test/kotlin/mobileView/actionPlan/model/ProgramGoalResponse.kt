package mobileView.actionPlan.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgramGoalResponse(
    val status: String? = null,
    val message: String? = null,
    val data: ProgramGoalData? = null
)

@Serializable
data class ProgramGoalData(
    val program: Program? = null,
    val ha_profile: HaProfile? = null,
    val preference: Preference? = null,
    val program_start_date: String? = null
)


@Serializable
data class Program (
    val is_questionnaire_taken: Boolean? = null
)



@Serializable
data class HaProfile(
    val smoke: String? = null,
    val alcohol: String? = null,
    val pre_condition: String? = null,
    val is_pregnant: Boolean? = null,
    val activity_level: String? = null,
    val n_smoke: String? = null,
    val n_alcohol: String? = null,
    val waist_circumference: String? = null
)

@Serializable
data class Preference(
    val weekday_sleep_routine_wakeup_time: String? = null,
    val weekday_sleep_routine_bed_time: String? = null,
    val weekend_sleep_routine_wakeup_time: String? = null,
    val weekend_sleep_routine_bed_time: String? = null,
    val nutrition_recommendation_status: String? = null
)
