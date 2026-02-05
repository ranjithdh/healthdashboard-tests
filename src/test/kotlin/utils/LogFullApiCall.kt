package utils

import utils.logger.logger
import utils.report.StepHelper

object LogFullApiCall {
    fun logFullApiCall(
        method: String,
        url: String,
        requestHeaders: Map<String, String>,
        requestBody: String?,
        response: com.microsoft.playwright.APIResponse
    ) {
        // -------- REQUEST --------
        logger.error { "➡️ API REQUEST METHOD: $method" }
        StepHelper.step("➡️ API REQUEST METHOD: $method")

        logger.error { "➡️ API REQUEST URL: $url" }
        StepHelper.step("➡️ API REQUEST URL: $url")

        logger.error { "➡️ API REQUEST HEADERS: $requestHeaders" }
        StepHelper.step("➡️ API REQUEST HEADERS: $requestHeaders")

        if (!requestBody.isNullOrBlank()) {
            logger.error { "➡️ API REQUEST BODY: $requestBody" }
            StepHelper.step("➡️ API REQUEST BODY: $requestBody")
        }

        // -------- RESPONSE --------
        logger.error { "⬅️ API RESPONSE STATUS: ${response.status()}" }
        StepHelper.step("⬅️ API RESPONSE STATUS: ${response.status()}")

        logger.error { "⬅️ API RESPONSE HEADERS: ${response.headers()}" }
        StepHelper.step("⬅️ API RESPONSE HEADERS: ${response.headers()}")

        try {
            val responseBody = response.text()
            logger.error { "⬅️ API RESPONSE BODY: $responseBody" }
            StepHelper.step("⬅️ API RESPONSE BODY: $responseBody")
        } catch (e: Exception) {
            logger.error { "⬅️ API RESPONSE BODY: <cannot read> ${e.message}" }
            StepHelper.step("⬅️ API RESPONSE BODY: <cannot read> ${e.message}")
        }
    }
}