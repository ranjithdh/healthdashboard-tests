package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class AddressPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    private val addressInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your address"))
    private val cityInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("City"))
    private val stateInput = byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("State"))
    private val pinCodeInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code"))
    private val continueButton = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))

    private val flatHouseNoOrBuildingInput = page.getByText("Flat, House no., Building,")

    fun enterFlatHouseNoOrBuilding(value: String): AddressPage {
        logger.info { "enterFlatHouseNoOrBuilding($value)" }
        flatHouseNoOrBuildingInput.fill(value)
        return this
    }

    fun clearFlatHouseNoOrBuilding(): AddressPage {
        logger.info { "clearFlatHouseNoOrBuilding()" }
        flatHouseNoOrBuildingInput.clear()
        return this
    }

    fun enterAddress(address: String): AddressPage {
        logger.info { "enterAddress($address)" }
        addressInput.fill(address)
        return this
    }

    fun clearAddress(): AddressPage {
        logger.info { "clearAddress()" }
        addressInput.clear()
        return this
    }

    fun enterCity(city: String): AddressPage {
        logger.info { "enterCity($city)" }
        cityInput.fill(city)
        return this
    }

    fun clearCity(): AddressPage {
        logger.info { "clearCity()" }
        cityInput.clear()
        return this
    }

    fun selectState(state: String): AddressPage {
        logger.info { "selectState($state)" }
        stateInput.click()
        byRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(state)).click()
        return this
    }

    fun enterPinCode(pinCode: String): AddressPage {
        logger.info { "enterPinCode($pinCode)" }
        pinCodeInput.fill(pinCode)
        return this
    }

    fun clearPinCode(): AddressPage {
        logger.info { "clearPinCode()" }
        pinCodeInput.clear()
        return this
    }

    fun fillAddress(
        flatHouseNoOrBuilding: String,
        address: String,
        city: String,
        state: String,
        pinCode: String
    ): AddressPage {
        logger.info { "fillAddress($flatHouseNoOrBuilding, $address, $city, $state, $pinCode)" }
        enterFlatHouseNoOrBuilding(flatHouseNoOrBuilding)
        enterAddress(address)
        enterCity(city)
        selectState(state)
        enterPinCode(pinCode)
        return this
    }


    fun clickContinue(): AddressPage {
        logger.info { "clickContinue()" }
        continueButton.click()
        return this
    }

    fun fillAndContinue(
        flatHouseNoOrBuilding: String,
        address: String,
        city: String,
        state: String,
        pinCode: String
    ): TimeSlotPage {
        logger.info { "fillAndContinue()" }
        fillAddress(flatHouseNoOrBuilding, address, city, state, pinCode)
        clickContinue()
        val timeSlotPage = TimeSlotPage(page)
        timeSlotPage.waitForConfirmation()
        return timeSlotPage
    }

    fun waitForConfirmation(): AddressPage {
        flatHouseNoOrBuildingInput.waitFor()
        return this
    }

    fun isAddressVisible(): Boolean {
        return addressInput.isVisible
    }

    fun isFlatHouseNoOrBuildingVisible(): Boolean {
        return flatHouseNoOrBuildingInput.isVisible
    }

    fun isCityVisible(): Boolean {
        return cityInput.isVisible
    }

    fun isStateVisible(): Boolean {
        return stateInput.isVisible
    }

    fun isPinCodeVisible(): Boolean {
        return pinCodeInput.isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return continueButton.isEnabled
    }
}
