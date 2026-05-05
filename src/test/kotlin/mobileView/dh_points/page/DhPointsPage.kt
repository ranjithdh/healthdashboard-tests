package mobileView.dh_points.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DhPointsPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.BASE_URL // Update this to the actual URL path for dh_points
    
    fun waitForConfirmation(): DhPointsPage {
        // Example check, replace with actual assertion if specific URL exists
        page.waitForURL { it.contains("dh-points") || it.contains("rewards") || it.contains(TestConfig.Urls.BASE_URL) }
        return this
    }

    /**
     * Triggers the blood-data trigger-pipeline API.
     * 
     * @param stgUserId The staging user ID. Default: 1783
     * @param prdUserId The production user ID. Default: 145
     */
    fun triggerBloodDataPipeline(stgUserId: String, prdUserId: Int) {
        val url = TestConfig.APIs.API_TRIGGER_PIPELINE + "?platform=web"
        val payload = buildJsonObject {
            put("stg_user_id", stgUserId.toIntOrNull() ?: 0)
            put("source_prd_user_id", prdUserId)
            put("mark_onboarding_diagnostics_complete", true)

        }.toString()

        val headers = mapOf(
            "accept" to "application/json, text/plain, */*",
            "content-type" to "application/json",
            "client_id" to TestConfig.CLIENT_ID,
            "user_timezone" to "Asia/Calcutta"
        )

      //  ApiLogger.logRequest(url, "POST", headers, payload)

        val startTime = System.currentTimeMillis()
        val requestOptions = RequestOptions.create()
        headers.forEach { (name, value) -> requestOptions.setHeader(name, value) }
        requestOptions.setData(payload)

        val response = page.request().post(url, requestOptions)
        val endTime = System.currentTimeMillis()
        val responseText = response.text()
        
      //  ApiLogger.logResponse(response.status(), responseText, endTime - startTime, url)

        if (response.status() != 200) {
            throw RuntimeException("Failed to trigger blood-data pipeline: ${response.status()} $responseText")
        }
    }
}
