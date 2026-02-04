package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_CONTINUE
import utils.report.StepHelper.ENTER_ADDRESS
import utils.report.StepHelper.ENTER_CITY
import utils.report.StepHelper.ENTER_FLAT_HOUSE_NO
import utils.report.StepHelper.ENTER_PIN_CODE
import utils.report.StepHelper.FILL_ADDRESS_DETAILS_CONTINUE
import utils.report.StepHelper.FILL_ADDRESS_FORM
import utils.report.StepHelper.SELECT_STATE
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class AddressPage(page: Page) : BasePage(page) {

    override val pageUrl = "/onboard"

    private val addressInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your address"))
    private val cityInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("City"))
    private val stateInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("State"))
    private val pinCodeInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code"))
    private val continueButton = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))

    private val flatHouseNoOrBuildingInput = page.getByText("Flat, House no., Building,")

    fun enterFlatHouseNoOrBuilding(value: String): AddressPage {
        StepHelper.step(ENTER_FLAT_HOUSE_NO + value)
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
        StepHelper.step(ENTER_ADDRESS + address)
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
        StepHelper.step(ENTER_CITY + city)
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
        StepHelper.step(SELECT_STATE + state)
        logger.info { "selectState($state)" }
        stateInput.fill(state)
        return this
    }

    fun enterPinCode(pinCode: String): AddressPage {
        StepHelper.step(ENTER_PIN_CODE + pinCode)
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
        StepHelper.step(FILL_ADDRESS_FORM)
        logger.info { "fillAddress($flatHouseNoOrBuilding, $address, $city, $state, $pinCode)" }
        utils.SignupDataStore.update {
            this.flatHouseNoOrBuilding = flatHouseNoOrBuilding
            this.address = address
            this.city = city
            this.state = state
            this.pinCode = pinCode
        }
        enterFlatHouseNoOrBuilding(flatHouseNoOrBuilding)
        enterAddress(address)
        enterCity(city)
        selectState(state)
        enterPinCode(pinCode)
        return this
    }


    fun clickContinue(): AddressPage {
        StepHelper.step(CLICK_CONTINUE)
        logger.info { "clickContinue()" }
        continueButton.click()
        return this
    }

    fun fillAddressDetails(testUser: TestUser = TestConfig.TestUsers.NEW_USER): TimeSlotPage {
        StepHelper.step(FILL_ADDRESS_DETAILS_CONTINUE)
        logger.info { "fillAddressDetails()" }
        fillAddress(
            testUser.flatHouseNo,
            testUser.address,
            testUser.city,
            testUser.state,
            testUser.pinCode
        )
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
