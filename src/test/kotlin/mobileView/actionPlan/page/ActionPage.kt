package mobileView.actionPlan.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig

class ActionPage(page: Page) : BasePage(page) {
    private val actionPlanTitle: Locator =
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Action Plan"))

    fun waitForConfirmation(): ActionPage {
        page.waitForURL(TestConfig.Urls.RECOMMENDATIONS_URL)
        actionPlanTitle.waitFor()
        return this
    }

    override val pageUrl = TestConfig.Urls.RECOMMENDATIONS_URL

}