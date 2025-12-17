package profile.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import config.TestConfig.ACCESS_TOKEN
import config.TestConfig.CLIENT_ID
import config.TestConfig.json
import model.AddAddressResponse
import model.UserAddressData
import model.UserAddressResponse
import profile.utils.ProfileUtils.buildAddressText
import utils.logger.logger
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfilePage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.PROFILE_PAGE_URL

    private var addressData: UserAddressData? = null

    val tonePreference: Locator = byText("Tone Preference")

    fun waitForConfirmation(): ProfilePage {
        tonePreference.waitFor()
        return this
    }


    fun captureAddressData(action: () -> Unit) {
        try {
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()?.contains(TestConfig.APIs.API_ADDRESS) == true && response.status() == 200
                }, action
            )

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.info { "API response body is empty" }
                return
            }

            logger.info { "API response...${responseBody}" }

            val responseObj = json.decodeFromString<UserAddressResponse>(responseBody)
            // logger.error { "responseObj...$responseObj" } // Reduced log level or removed redundant error log

            if (responseObj.data.addressList.isNotEmpty()) {
                addressData = responseObj.data
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response or API call failed..${e.message}" }
        }
    }


    /**------Address Flied------*/

    val saveAddressDropDown: Locator =
        page.locator("div").filter(Locator.FilterOptions().setHasText(Pattern.compile("^Saved Addresses$"))).first()

    val addNewAddress: Locator = page.getByText("Add a new address")

    val newAddressDialog: Locator =
        page.getByRole(AriaRole.DIALOG, Page.GetByRoleOptions().setName("Add a new Address"))


    fun isSaveAddressDropDownVisible(): Boolean {
        saveAddressDropDown.waitFor()
        return saveAddressDropDown.isVisible
    }

    fun clickAddressDropDown() {
        saveAddressDropDown.click()
    }

    fun assertAddressesFromApi() {
        val addresses = addressData?.addressList
            ?: throw AssertionError("Address list is null from API")

        addresses.forEach { item ->
            val address = item.address

            val title = address.addressName ?: "Primary"
            val expectedAddressText = buildAddressText(address)

            // Unique address card
            val addressCard = page
                .locator("div.border")
                .filter(
                    Locator.FilterOptions().setHas(
                        page.getByRole(
                            AriaRole.HEADING,
                            Page.GetByRoleOptions().setName(title)
                        )
                    )
                )
                .first()

            addressCard.waitFor()

            // ✅ Address text (handles duplicates)
            addressCard
                .locator("p")
                .filter(
                    Locator.FilterOptions().setHasText(expectedAddressText)
                )
                .first()
                .waitFor()

            // Mobile (optional)
            address.addressMobile?.let {
                addressCard
                    .locator("p")
                    .filter(
                        Locator.FilterOptions().setHasText("Mobile number: $it")
                    )
                    .first()
                    .waitFor()
            }

            // Edit & Remove
            addressCard.getByText("Edit").first().waitFor()
            addressCard.getByText("Remove").first().waitFor()
        }

        // Add new address CTA
        page.getByText("Add a new address").waitFor()
    }


    fun isAddNewAddressVisible(): Boolean {
        addNewAddress.waitFor()
        return addNewAddress.isVisible
    }

    fun clickAddNewAddress() {
        addNewAddress.click()
    }

    fun isNewAddressDialogVisible(): Boolean {
        newAddressDialog.waitFor()
        return newAddressDialog.isVisible
    }

    //New Address Dialog

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


    fun assertAddressFormFieldsVisible() {
        page.getByText("Nick name *").waitFor()
        page.getByText("Mobile number", Page.GetByTextOptions().setExact(true)).waitFor()
        page.getByText("Flat, House no., Building,").waitFor()
        page.getByText("Street Address *").waitFor()
        page.getByText("Address Line").waitFor()
        page.getByText("City *").waitFor()
        page.getByText("State *").waitFor()
        page.getByText("Pin code *").waitFor()
        page.getByText("Country *").waitFor()

        nickNameInput.waitFor()
        mobileNumberInput.waitFor()
        houseNoInput.waitFor()
        streetAddressInput.waitFor()
        addressLine2Input.waitFor()
        cityInput.waitFor()
        stateInput.waitFor()
        pincodeInput.waitFor()
        countryInput.waitFor()
    }

    fun fillMandatoryAddressFields(
        nickName: String,
        street: String,
        city: String,
        state: String,
        pincode: String,
        country: String
    ) {
        nickNameInput.fill(nickName)
        streetAddressInput.fill(street)
        cityInput.fill(city)
        stateInput.fill(state)
        pincodeInput.fill(pincode)
        countryInput.fill(country)
    }


    fun assertSubmitEnabledAfterMandatoryFields() {
        assertThat(newAddressSubmit).isEnabled()
    }


    fun addAddressAndValidate() {
        // 2️⃣ Prepare request payload
        val payload = mapOf(
            "address_name" to "Home",
            "address_line_1" to "5 Road, Swarnapuri",
            "state" to "Tamil Nadu",
            "city" to "Salem",
            "pincode" to "636004",
            "country" to "India",
            "address_type" to "communication"
        )

        // 3️⃣ Call API using browser request context
        val response = page.request().post(
            TestConfig.APIs.API_ADD_ADDRESS,
            RequestOptions.create()
                .setHeader("access_token", ACCESS_TOKEN)
                .setHeader("client_id", CLIENT_ID)
                .setHeader("user_timezone", "Asia/Calcutta")
                .setHeader("Content-Type", "application/json")
                .setData(payload)
        )

        // 4️⃣ HTTP-level validation
        assertTrue(response.ok(), "API failed with status ${response.status()}")

        // 5️⃣ Parse response JSON
        val responseBody = response.text()
        val parsed =
            json.decodeFromString<AddAddressResponse>(responseBody)

        // 6️⃣ Business validations
        assertEquals("success", parsed.status)

        val address = parsed.data.di_address
        assertEquals("Home", address.addressName)
        assertEquals("Salem", address.city)
        assertEquals("Tamil Nadu", address.state)
        assertEquals("636004", address.pincode)
        assertEquals("India", address.country)
        assertEquals("communication", address.addressType)
    }


}









