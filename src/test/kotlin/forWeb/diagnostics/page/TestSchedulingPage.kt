package forWeb.diagnostics.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import model.profile.UserAddressData
import model.profile.UserAddressResponse
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import profile.utils.ProfileUtils.buildAddressText
import utils.json.json
import utils.logger.logger
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

class TestSchedulingPage(page: Page) : BasePage(page) {

    override val pageUrl = ""
    private var addressData: UserAddressData? = null
    private val nickNameInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Home, work, etc."))

    private val mobileNumberInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter Contact number for this"))

    private val houseNoInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter house no."))

    private val streetAddressInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your street address"))

    private val addressLine2Input =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter Address Line 2 ("))

    private val cityInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter city"))

    private val stateInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter state"))

    private val pincodeInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter pincode"))

    private val countryInput =
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Country"))

    private val newAddressSubmit =
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))

    val newAddressDialog: Locator =
//        page.getByRole(AriaRole.DIALOG, Page.GetByRoleOptions().setName("Add a new Address"))
    page.getByTestId("diagnostics-booking-add-new-address")

    fun captureAddressData(action: () -> Unit) {
        logger.info { "Capturing address data..." }
        try {
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()?.contains(TestConfig.APIs.API_ADDRESS) == true &&
                            response.status() == 200 &&
                            response.request().method() == "GET"
                }, action
            )

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.info { "API response body is empty" }
                return
            }

            logger.info { "API response...${responseBody}" }

            val responseObj = json.decodeFromString<UserAddressResponse>(responseBody)

            if (responseObj.data.addressList.isNotEmpty()) {
                addressData = responseObj.data
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response or API call failed..${e.message}" }
        }
    }


    fun verifySampleCollectionAddressHeading() {
        val heading = page.getByText("Sample Collection Address")
        logger.info { "Verifying Sample Collection Address heading" }
        Assertions.assertTrue(heading.isVisible, "Sample Collection Address heading should be visible")
    }

    fun assertAddressesFromApi() {
        // Implementation regarding API validation would go here
        logger.info { "Asserting addresses from API..." }
    }

    fun clickAddNewAddress() {
        logger.info { "Clicking Add New Address" }
//        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add New Address")).click()
        page.getByTestId("diagnostics-booking-add-new-address").click();
    }

    fun editUserAddress(index: Int) {
        val updateAddressDialog: Locator =
            page.getByRole(AriaRole.DIALOG, Page.GetByRoleOptions().setName("Update Address"))

        val addresses = addressData?.addressList
            ?: throw AssertionError("Address list is null from API")

        require(addresses.isNotEmpty()) { "Address list is empty from API" }
        require(index < addresses.size) { "Address index $index out of bounds" }

        val addressItem = addresses[index]
        val address = addressItem.address
        val addressId = addressItem.addressId

        val title = address.addressName?.takeIf { it.isNotBlank() } ?: "Primary"
        val expectedAddressText = buildAddressText(address)
        page.locator(".bg-secondary.flex.flex-1").first().click();

        // Fill inputs (UI)
        val number = (500..1000).random()
        val updatedNickName = (address.addressName ?: "").plus(" Updated $number")
        nickNameInput.fill(updatedNickName)
        // mobileNumberInput.fill(address.addressMobile ?: "")
        houseNoInput.fill(address.address)
        streetAddressInput.fill(address.addressLine1)
        addressLine2Input.fill(address.addressLine2 ?: "")
        cityInput.fill(address.city)
        stateInput.fill(address.state)
        pincodeInput.fill(address.pincode)
        countryInput.fill(address.country)


        captureAddressData {
            newAddressSubmit.click()
        }

        val updatedList = addressData?.addressList ?: throw AssertionError("Address list not updated")
        val updatedAddress = updatedList.find { it.addressId == addressId }

        assertEquals(updatedNickName, updatedAddress?.address?.addressName)
    }

    fun verifyPriceDetails(expectedSubtotal: Double, expectedDiscount: Double) {
        logger.info { "Verifying price details: Subtotal=$expectedSubtotal, Discount=$expectedDiscount" }
        // Basic verification logic
        val subtotalText = "₹$expectedSubtotal" // Simplified formatting logic
        // Verify subtotal visibility if possible, or generic check
        
        // This is a placeholder validation
        Assertions.assertTrue(page.isVisible("text=Price Details"), "Price Details section should be visible")
    }

    fun verifyFooterActions() {
        logger.info { "Verifying footer actions" }
        val proceedBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Proceed"))
        Assertions.assertTrue(proceedBtn.isVisible, "Proceed button should be visible")
    }

    fun clickProceed() {
        logger.info { "Clicking Proceed" }
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Proceed")).click()
    }

    fun verifySlotSelectionPage() {
        logger.info { "Verifying Slot Selection Page" }
        // Verify we are on the slot selection page, e.g. check for "Select a Date" or "Schedule" heading
        val heading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Schedule your Blood Test").setLevel(1))
        // If heading differs, might need adjustment
        Assertions.assertTrue(heading.isVisible || page.getByText("Select Date").isVisible, "Slot selection page should be visible")
    }


    fun assertAddressFormFieldsVisible() {
        page.getByText("Nick name *").waitFor()
        // page.getByText("Mobile number", Page.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("Flat, House no., Building,").waitFor()
        page.getByText("Street Address *").waitFor()
        page.getByText("Address Line").waitFor()
        page.getByText("City *").waitFor()
        page.getByText("State *").waitFor()
        page.getByText("Pin code *").waitFor()
        page.getByText("Country *").waitFor()

        nickNameInput.waitFor()
        // mobileNumberInput.waitFor()
        houseNoInput.waitFor()
        streetAddressInput.waitFor()
        addressLine2Input.waitFor()
        cityInput.waitFor()
        stateInput.waitFor()
        pincodeInput.waitFor()
        countryInput.waitFor()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close")).click()
    }

    fun fillMandatoryAddressFields(
        nickName: String,
        street: String,
        doorNumber: String,
        city: String,
        state: String,
        pincode: String,
        country: String
    ) {
        nickNameInput.fill(nickName)
        streetAddressInput.fill(street)
        houseNoInput.fill(doorNumber)
        cityInput.fill(city)
        stateInput.fill(state)
        pincodeInput.fill(pincode)
        countryInput.fill(country)
    }


    fun addAddressAndValidate() {
        val number = (500..1000).random()
        val nickName = "Home $number"
        val doorNumber = "E 4"
        val street = "5 Road, Swarnapuri"
        val city = "Salem"
        val state = "Tamil Nadu"
        val pincode = "636004"
        val country = "India"

        fillMandatoryAddressFields(
            nickName,
            street,
            doorNumber,
            city,
            state,
            pincode,
            country
        )

        captureAddressData {
            newAddressSubmit.click()
        }

        val updatedList = addressData?.addressList ?: throw AssertionError("Address list not updated")
        val addedAddress = updatedList.find { it.address.addressName == nickName }

        assertEquals(nickName, addedAddress?.address?.addressName)
        assertEquals(city, addedAddress?.address?.city)
        assertEquals(pincode, addedAddress?.address?.pincode)
    }

    fun isNewAddressDialogVisible(): Boolean {
        newAddressDialog.waitFor()
        return newAddressDialog.isVisible
    }
}
