package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class AddressPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"
    override val pageLoadedSelector =  byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Set up your account")).textContent() ?: ""

    fun enterAddress(address: String): AddressPage {
        logger.info { "enterAddress($address)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your address")).fill(address)
        return this
    }

    fun enterCity(city: String): AddressPage {
        logger.info { "enterCity($city)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("City")).fill(city)
        return this
    }

    fun selectState(state: String): AddressPage {
        logger.info { "selectState($state)" }
        byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("State")).click()
        byRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(state)).click()
        return this
    }

    fun enterPinCode(pinCode: String): AddressPage {
        logger.info { "enterPinCode($pinCode)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code")).fill(pinCode)
        return this
    }

    fun fillAddress(
        address: String,
        city: String,
        state: String,
        pinCode: String
    ): AddressPage {
        logger.info { "fillAddress($address, $city, $state, $pinCode)" }
        enterAddress(address)
        enterCity(city)
        selectState(state)
        enterPinCode(pinCode)
        return this
    }


    fun clickContinue(): AddressPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    fun fillAndContinue(
        address: String,
        city: String,
        state: String,
        pinCode: String
    ): TimeSlotPage {
        logger.info { "fillAndContinue()" }
        fillAddress(address, city, state, pinCode)
        clickContinue()
        val timeSlotPage = TimeSlotPage(page)
        timeSlotPage.waitForConfirmation()
        return timeSlotPage
    }

    fun waitForConfirmation(): AddressPage {
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your address")).waitFor()
        return this
    }

    fun isAddressVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your address")).isVisible
    }

    fun isCityVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("City")).isVisible
    }

    fun isStateVisible(): Boolean {
        return byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("State")).isVisible
    }

    fun isPinCodeVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code")).isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isEnabled
    }
}
