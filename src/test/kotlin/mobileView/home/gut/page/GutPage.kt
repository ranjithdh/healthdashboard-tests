package mobileView.home.gut.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.actionPlan.model.RecommendationData
import mobileView.actionPlan.page.ActionPlanPage
import mobileView.home.gut.model.GutDataWrapper
import mobileView.home.gut.model.GutResponse
import mobileView.home.gut.util.TestDataLoader
import model.profile.PreferenceUpdateResponse
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.logApiResponse

class GutPage(page: Page) : BasePage(page) {

    private val mappings = TestDataLoader.loadGeneGutMappings()

    private val searchView = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search in Gut"))

    override val pageUrl = TestConfig.Urls.HEALTH_DATA_URL

    private var gutDataWrapper: GutDataWrapper? = null

    fun clickGut(): GutPage {
        page.getByTestId("health-data-tab-gut").click()
        return this
    }

    fun waitForConfirmation(): GutPage {
        searchView.waitFor()
        return this
    }

    init {
        monitorTraffic()
    }

    private fun monitorTraffic() {
        captureGutData()
    }

    fun captureGutData() {
        if (gutDataWrapper != null) {
            try {
                val response = page.waitForResponse(
                    { response: Response? ->
                        response?.url()?.contains(TestConfig.APIs.API_GUT) == true &&
                                response.status() == 200 &&
                                response.request().method() == "GET"
                    }, { }
                )

                if (response.status() != 200) {
                    logger.error { "API returned error status: ${response.status()}" }
                    return
                }

                val responseBody = response.text()
                if (responseBody.isNullOrBlank()) {
                    logger.error { "API response body is empty" }
                    return
                }

                val responseObj = json.decodeFromString<GutResponse>(responseBody)

                if (responseObj.status == "success") {
                    gutDataWrapper = responseObj.data
                }
            } catch (e: Exception) {
                logger.error { "Failed to parse API response or API call failed..${e.message}" }
            }
        }
    }

}