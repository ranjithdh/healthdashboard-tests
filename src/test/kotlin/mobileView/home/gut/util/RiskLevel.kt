package mobileView.home.gut.util

sealed class RiskLevel(val value: String) {

    object Ideal : RiskLevel("Ideal")

    object NonIdeal : RiskLevel("Non-Ideal")

    object ModerateRisk : RiskLevel("Moderate Risk")
}