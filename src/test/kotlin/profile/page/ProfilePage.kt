package profile.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState
import config.BasePage
import config.TestConfig

class ProfilePage(page: Page) : BasePage(page) {


    override val pageUrl = TestConfig.Urls.PROFILE_PAGE_URL

    val healthMetricEdit = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Health Metrics Edit"))


    fun waitForConfirmation(): ProfilePage {
        page.waitForLoadState(LoadState.NETWORKIDLE)
        return this
    }

    //health metrics
    fun isHealthMetricEditVisible(): Boolean {
        return healthMetricEdit.isVisible
    }


}









