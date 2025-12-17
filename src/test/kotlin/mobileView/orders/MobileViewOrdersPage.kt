package mobileView.orders

import com.microsoft.playwright.Page
import config.BasePage
import mu.KotlinLogging



class AddressPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

}
