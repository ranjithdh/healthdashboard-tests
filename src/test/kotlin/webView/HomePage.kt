package webView

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import model.healthdata.HealthData
import utils.logger.logger

class HomePage(page: Page) : BasePage(page)  {

    override val pageUrl = TestConfig.Urls.HOME_PAGE_URL

    var healthData: HealthData? = null

    fun waitForHomePageConfirmation(): webView.HomePage {
        logger.info("Waiting for webView home page confirmation...")
        page.waitForURL(TestConfig.Urls.HOME_PAGE_URL)
        return this
    }

    fun clickHealthTab() {
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Health Data")).click()
    }

    fun getHealthDataResponse(): HealthData? {
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains(TestConfig.APIs.HEALTH_DATA) == true && response.status() == 200
            },
            {
                page.waitForURL(TestConfig.Urls.HOME_PAGE_URL)
            }
        )

        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "getHealthDataResponse API response body is empty" }
        }

        try {
            val responseObj = utils.json.json.decodeFromString<HealthData>(responseBody)

            if (responseObj.data != null) {
                healthData = responseObj
                return healthData
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response..${e.message}" }
            return null
        }

        return null
    }
}