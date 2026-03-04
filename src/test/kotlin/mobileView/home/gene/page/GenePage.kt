package mobileView.home.gene.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.home.gut.page.GutPage

class GenePage(page: Page): BasePage(page)  {
    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL




}