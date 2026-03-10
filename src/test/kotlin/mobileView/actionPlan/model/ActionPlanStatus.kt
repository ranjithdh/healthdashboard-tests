package mobileView.actionPlan.model

enum class ActionPlanStatus {
    TEST_IN_PROGRESS,
    NOT_SCHEDULED,
    SCHEDULED,
    NOT_PERSONALIZED,
    DEFAULT
}

val messages = mapOf(
    ActionPlanStatus.TEST_IN_PROGRESS to
            "Action plan will be generated after your consultation",

    ActionPlanStatus.NOT_SCHEDULED to
            "Action plan will be generated after your consultation",

    ActionPlanStatus.SCHEDULED to
            "Action plan will be generated after your consultation",

    ActionPlanStatus.NOT_PERSONALIZED to
            "Generating your action plan...",

    ActionPlanStatus.DEFAULT to
            "Action plan will be generated after your consultation"
)

val subText = mapOf(
    ActionPlanStatus.TEST_IN_PROGRESS to
            "Currently your test results are being prepared, after that you can schedule a consultation and get your action plan.",

    ActionPlanStatus.NOT_SCHEDULED to
            "Your test results are ready, book a consultation to generate your personalised action plan.",

    ActionPlanStatus.SCHEDULED to
            "Your consultation is scheduled for ",

    ActionPlanStatus.NOT_PERSONALIZED to
            "Our experts are adding some finishing touches. This might take a while.",

    ActionPlanStatus.DEFAULT to
            "Our experts are adding some finishing touches. This might take a while"
)

val CONSULTATION_CALENDLY_INCLUSIONS = listOf(
    "Quick overview of dashboard and results",
    "Symptoms check-in and mapping results",
    "State of existing medical conditions (if any reported)",
    "Highlight any urgent or clinically significant flags",
    "Suggest further medical diagnosis if any required",
    "Action Plan - Activity, Nutrition Guidelines, Sleep, Stress, Re-Test Schedule, Supplement Plan"
)


