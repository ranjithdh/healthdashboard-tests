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
        logApiData(method, url, requestHeaders, requestBody, response.status(), response.headers(), { response.text() })
    }

    fun logFullApiCall(response: com.microsoft.playwright.Response) {
        val request = response.request()
        logApiData(
            request.method(),
            request.url(),
            request.headers(),
            request.postData(),
            response.status(),
            response.headers(),
            { response.text() }
        )
    }

    private fun logApiData(
        method: String,
        url: String,
        requestHeaders: Map<String, String>,
        requestBody: String?,
        status: Int,
        responseHeaders: Map<String, String>,
        responseBodyProvider: () -> String?
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
        logger.error { "⬅️ API RESPONSE STATUS: $status" }
        StepHelper.step("⬅️ API RESPONSE STATUS: $status")

        logger.error { "⬅️ API RESPONSE HEADERS: $responseHeaders" }
        StepHelper.step("⬅️ API RESPONSE HEADERS: $responseHeaders")

        try {
            val responseBody = responseBodyProvider()
            logger.error { "⬅️ API RESPONSE BODY: $responseBody" }
            StepHelper.step("⬅️ API RESPONSE BODY: $responseBody")
        } catch (e: Exception) {
            logger.error { "⬅️ API RESPONSE BODY: <cannot read> ${e.message}" }
            StepHelper.step("⬅️ API RESPONSE BODY: <cannot read> ${e.message}")
        }
    }
}