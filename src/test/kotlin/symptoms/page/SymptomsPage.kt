package symptoms.page

import com.microsoft.playwright.Page
import config.BasePage
import config.TestConfig
import utils.logger.logger

class SymptomsPage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.SYMPTOMS_PAGE_URL

    fun waitForSymptomsPageConfirmation(): SymptomsPage {
        logger.info("Waiting for mobileView.home page confirmation...")
        page.waitForURL(TestConfig.Urls.SYMPTOMS_PAGE_URL)
        return this
    }

}