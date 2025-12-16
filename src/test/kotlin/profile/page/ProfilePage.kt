package profile.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState
import config.BasePage
import config.TestConfig
import model.PiiData

class ProfilePage(page: Page) : BasePage(page) {


    override val pageUrl = TestConfig.Urls.PROFILE_PAGE_URL

    val healthMetricEdit: Locator =
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Health Metrics Edit"))

    private var piiData: PiiData? = PiiData()


    fun waitForConfirmation(): ProfilePage {
        page.waitForLoadState(LoadState.NETWORKIDLE)
        return this
    }




    fun isHealthMetricEditVisible(): Boolean {
        healthMetricEdit.waitFor()
        return healthMetricEdit.isVisible
    }


}









