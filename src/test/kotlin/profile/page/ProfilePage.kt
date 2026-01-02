package profile.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestConfig.json
import model.profile.*
import profile.utils.ProfileUtils.bmiCategoryValues
import profile.utils.ProfileUtils.buildAddressText
import profile.utils.ProfileUtils.calculateBMIValues
import profile.utils.ProfileUtils.formatDobToDdMmYyyy
import profile.utils.ProfileUtils.formatDobWithAge
import profile.utils.ProfileUtils.formatFlotTwoDecimal
import utils.logger.logger
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ProfilePage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.PROFILE_PAGE_URL

    private var addressData: UserAddressData? = null
    private var currentPreference: String? = null
    private var piiData: PiiData? = null

    val tonePreference: Locator = byText("Tone Preference")

    val tonePreferenceKeyList = listOf("doctor", "friend", "bio_hacker")

    private val answersStored: MutableMap<String?, Any?> = HashMap<String?, Any?>()

    val previousButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Previous"))
    val nextButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Next"))


    init {
        monitorTraffic()
    }

    fun waitForConfirmation(): ProfilePage {
        tonePreference.waitFor()
        return this
    }


    private fun monitorTraffic() {
        //update address
        val updateProfileRequest = { request: com.microsoft.playwright.Request ->
            if (request.url().contains(TestConfig.APIs.API_UPDATE_PROFILE)) {
                logger.info { "API Request: ${request.method()} ${request.url()}" }
                request.postData()?.let {
                    logger.info { "API Request Payload: $it" }
                }
            }
        }

        val updateProfileResponse = { response: Response ->
            if (response.url().contains(TestConfig.APIs.API_UPDATE_PROFILE)) {
                logger.info { "API Response: ${response.status()} ${response.url()}" }
                try {
                    logger.info { "API Response Body: ${response.text()}" }
                } catch (e: Exception) {
                    logger.warn { "Could not read response body: ${e.message}" }
                }
            }
        }

        //preference update
        val preferenceProfileRequest = { request: com.microsoft.playwright.Request ->
            if (request.url().contains(TestConfig.APIs.API_TONE_PREFERENCE)) {
                logger.info { "API Request: ${request.method()} ${request.url()}" }
                request.postData()?.let {
                    logger.info { "API Request Payload: $it" }
                }
            }
        }

        val preferenceProfileResponse = { response: Response ->
            if (response.url().contains(TestConfig.APIs.API_TONE_PREFERENCE)) {
                logger.info { "API Response: ${response.status()} ${response.url()}" }
                try {
                    logger.info { "API Response Body: ${response.text()}" }
                    if (response.status() == 200) {

                    }
                } catch (e: Exception) {
                    logger.warn { "Could not read response body: ${e.message}" }
                }
            }
        }

        page.onRequest(updateProfileRequest)
        page.onResponse(updateProfileResponse)
        page.onRequest(preferenceProfileRequest)
        page.onResponse(preferenceProfileResponse)
        try {
        } finally {
            page.offRequest(updateProfileRequest)
            page.offResponse(updateProfileResponse)
            page.offRequest(preferenceProfileRequest)
            page.offResponse(preferenceProfileResponse)
        }
    }


    fun captureAddressData(action: () -> Unit) {
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


    fun capturePreferenceUpdate(selectedOption: String, action: () -> Unit): Boolean {
        try {
            val response = page.waitForResponse(
                { response: Response? ->
                    response?.url()?.contains(TestConfig.APIs.API_PREFERENCE_UPDATE) == true &&
                            response.status() == 200 &&
                            response.request().method() == "PUT"
                }, action
            )

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return false
            }

            logger.info { "API response...${responseBody}" }

            val responseObj = json.decodeFromString<PreferenceUpdateResponse>(responseBody)

            if (responseObj.status == "success" && responseObj.data.isUpdated) {
                logger.info { "Preference updated successfully to: $selectedOption" }
                return true
            }

            return false
        } catch (e: Exception) {
            logger.error { "Failed to parse API response or API call failed..${e.message}" }
            return false
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

            val title = address.addressName?.takeIf { it.isNotBlank() } ?: "Primary"
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
            if (!address.addressMobile.isNullOrBlank()) {
                addressCard
                    .locator("p")
                    .filter(
                        Locator.FilterOptions().setHasText("Mobile number: ${address.addressMobile}")
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


    fun addAddressAndValidate() {
        val number = (0..100).random()
        val nickName = "Home $number"
        val street = "5 Road, Swarnapuri"
        val city = "Salem"
        val state = "Tamil Nadu"
        val pincode = "636004"
        val country = "India"

        fillMandatoryAddressFields(
            nickName,
            street,
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


    fun removeUserAddress() {

        val addresses = addressData?.addressList
            ?: throw AssertionError("Address list is null from API")

        require(addresses.isNotEmpty()) {
            "Address list is empty from API"
        }


        val addressItem = addresses.first()
        val address = addressItem.address
        val addressId = addressItem.addressId


        val title = address.addressName?.takeIf { it.isNotBlank() } ?: "Primary"
        val expectedAddressText = buildAddressText(address)

        /* -------------------------------
           1️⃣ Locate Address Card
           ------------------------------- */
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

        /* -------------------------------
           2️⃣ Validate Address Content
           ------------------------------- */
        addressCard
            .locator("p")
            .filter(Locator.FilterOptions().setHasText(expectedAddressText))
            .first()
            .waitFor()

        /* -------------------------------
           3️⃣ Click Remove Button
           ------------------------------- */
        addressCard.getByText("Remove").first().click()

        /* -------------------------------
           4️⃣ Confirm Dialog
           ------------------------------- */
        val dialog = page.getByRole(AriaRole.DIALOG)
        dialog.waitFor()

        /* -------------------------------
           5️⃣ DELETE API CALL (Intercepted)
           ------------------------------- */
        captureAddressData {
            dialog.getByRole(
                AriaRole.BUTTON,
                Locator.GetByRoleOptions().setName("Yes, delete")
            ).click()
        }

        /* -------------------------------
           6️⃣ Verify Removal
           ------------------------------- */
        val updatedList = addressData?.addressList ?: emptyList()
        val isRemoved = updatedList.none { it.addressId == addressId }

        assertTrue(isRemoved, "Address with ID $addressId was not removed from the list")
    }

    fun editUserAddress() {

        val updateAddressDialog: Locator =
            page.getByRole(AriaRole.DIALOG, Page.GetByRoleOptions().setName("Update Address"))

        val addresses = addressData?.addressList
            ?: throw AssertionError("Address list is null from API")

        require(addresses.isNotEmpty()) { "Address list is empty from API" }

        val addressItem = addresses.first()
        val address = addressItem.address
        val addressId = addressItem.addressId

        val title = address.addressName?.takeIf { it.isNotBlank() } ?: "Primary"
        val expectedAddressText = buildAddressText(address)

        /* -------------------------------
           1️⃣ Locate Address Card
           ------------------------------- */
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

        /* -------------------------------
           2️⃣ Validate Address Content
           ------------------------------- */
        addressCard
            .locator("p")
            .filter(Locator.FilterOptions().setHasText(expectedAddressText))
            .first()
            .waitFor()

        /* -------------------------------
           3️⃣ Click Edit Button
           ------------------------------- */
        addressCard.getByText("Edit").first().click()
        updateAddressDialog.waitFor()

        // Fill inputs (UI)
        val number = (0..100).random()
        val updatedNickName = (address.addressName ?: "").plus(" Updated $number")
        nickNameInput.fill(updatedNickName)
        mobileNumberInput.fill(address.addressMobile ?: "")
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

    /**--------Communication Preference------------*/

    fun fetchCurrentPreference() {
        try {
            logger.info { "Fetching current preference from API..." }

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.API_PREFERENCE,
                com.microsoft.playwright.options.RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", "Asia/Calcutta")
            )

            if (response.status() != 200) {
                logger.error { "API returned status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            logger.info { "API response...${responseBody}" }

            val responseObj = json.decodeFromString<UserPreferenceResponse>(responseBody)

            if (responseObj.status == "success") {
                currentPreference = responseObj.data.preference.communicationPreference
                logger.info { "Current preference from API: $currentPreference" }
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch current preference: ${e.message}" }
        }
    }


    fun selectCommunicationOption() {

        // Fetch current preference from API
        fetchCurrentPreference()

        val options = listOf("Doctor", "Friend", "Biohacker")

        // Get current preference from API instead of hardcoded value
        val alreadySelectedPreference = currentPreference ?: tonePreferenceKeyList[0]

        val selectedOption = when (alreadySelectedPreference) {
            tonePreferenceKeyList[0] -> "Doctor"
            tonePreferenceKeyList[1] -> "Friend"
            tonePreferenceKeyList[2] -> "Biohacker"
            else -> "Doctor" // Default fallback
        }

        // Ensure all cards are rendered
        options.forEach {
            communicationCard(it).waitFor()
        }


        assertTrue(
            isCheckIconVisible(selectedOption),
            "$selectedOption should show check icon"
        )

        val currentIndex = tonePreferenceKeyList.indexOf(alreadySelectedPreference)
        val nextIndex = (currentIndex + 1) % tonePreferenceKeyList.size
        val newSelectedPreference = tonePreferenceKeyList[nextIndex]

        val newSelectedOption = when (newSelectedPreference) {
            tonePreferenceKeyList[0] -> "Doctor"
            tonePreferenceKeyList[1] -> "Friend"
            else -> "Biohacker"
        }

        // Capture the preference update API call
        val isSuccess = capturePreferenceUpdate(newSelectedOption) {
            communicationCard(newSelectedOption).click()
        }

        // Verify the API call was successful
        assertTrue(isSuccess, "Preference update API call failed")

        // Fetch current preference from API
        fetchCurrentPreference()

        // Verify the selected option from the backend
        assertEquals(currentPreference, newSelectedPreference)

        // Verify the selected option is now checked
        assertTrue(
            isCheckIconVisible(newSelectedOption),
            "$newSelectedOption should show check icon after selection"
        )

        logger.info { "Successfully selected and verified preference: $newSelectedOption" }
    }


    fun communicationCard(title: String): Locator {
        return page.locator("div.cursor-pointer.border").filter(
            Locator.FilterOptions().setHas(
                page.getByRole(
                    AriaRole.HEADING,
                    Page.GetByRoleOptions().setName(title)
                )
            )
        )
    }

    fun isCheckIconVisible(title: String): Boolean {
        val card = communicationCard(title)
        card.waitFor()
        return card.locator("svg").first().isVisible
    }


    /**------------Account Information----------------*/
    fun fetchAccountInformation() {
        try {
            logger.info { "Fetching current preference from API..." }

            val apiContext = page.context().request()
            val response = apiContext.get(
                TestConfig.APIs.API_ACCOUNT_INFORMATION,
                com.microsoft.playwright.options.RequestOptions.create()
                    .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                    .setHeader("client_id", TestConfig.CLIENT_ID)
                    .setHeader("user_timezone", "Asia/Calcutta")
            )

            if (response.status() != 200) {
                logger.error { "API returned status: ${response.status()}" }
                return
            }

            val responseBody = response.text()
            if (responseBody.isNullOrBlank()) {
                logger.error { "API response body is empty" }
                return
            }

            logger.info { "API response...${responseBody}" }

            val responseObj = json.decodeFromString<PiiUserResponse>(responseBody)

            if (responseObj.status == "success") {
                piiData = responseObj.data.piiData
                logger.info { "Current account Information from API: $piiData" }
            }
        } catch (e: Exception) {
            logger.error { "Failed to fetch current preference: ${e.message}" }
        }
    }

    //validation
    fun accountInformationValidation() {
        fetchAccountInformation()
        waitForViewProfileLoaded()

        val mobileNumber = "+${piiData?.countryCode} ${piiData?.mobile}"
        val dob = formatDobWithAge(piiData?.dob)

        assertViewProfileDetails(
            name = piiData?.name ?: "",
            email = piiData?.email ?: "",
            dob = dob,
            countryCode = mobileNumber
        )

    }


    private fun valueByLabel(label: String): Locator {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName(label)
        )
            .locator("xpath=following-sibling::*")
            .first()
    }

    fun waitForViewProfileLoaded() {
        valueByLabel("Name").waitFor()
        valueByLabel("Email").waitFor()
        valueByLabel("Date of Birth").waitFor()
        valueByLabel("Mobile Number").waitFor()
    }

    fun assertViewProfileDetails(
        name: String,
        email: String,
        dob: String,
        countryCode: String
    ) {

        logger.info {
            "${valueByLabel("Name").innerText()} : $name, ${valueByLabel("Email").innerText()} : $email, ${
                valueByLabel(
                    "Date of Birth"
                ).innerText()
            } : $dob, ${
                valueByLabel(
                    "Mobile Number"
                ).innerText()
            } : $countryCode"
        }

        assertTrue(valueByLabel("Name").innerText().equals(name))
        assertTrue(valueByLabel("Email").innerText().equals(email))
        assertTrue(valueByLabel("Date of Birth").innerText().equals(dob))
        assertTrue(valueByLabel("Mobile Number").innerText().equals(countryCode))


    }


    //edit
    fun accountInformationEdit() {
        fetchAccountInformation()
        waitForViewProfileLoaded()

        val editProfile = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Edit Profile"))
        val saveChanges = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save Changes"))

        editProfile.waitFor()
        editProfile.click()
        waitForEditProfileLoaded()


        val mobileNumber = "+${piiData?.countryCode} ${piiData?.mobile}"
        val editDob = formatDobToDdMmYyyy(piiData?.dob)
        val viewDob = formatDobWithAge(piiData?.dob)


        assertEditProfileDetails(
            name = piiData?.name ?: "",
            email = piiData?.email ?: "",
            dob = editDob,
            countryCode = mobileNumber
        )

        val randomNumber = (1..100).random()

        val updateName = editableInputByLabel("Name").inputValue().plus(" $randomNumber")

        editableInputByLabel("Name").fill(updateName)

        saveChanges.click()

        waitForViewProfileLoaded()


        assertViewProfileDetails(
            name = updateName,
            email = piiData?.email ?: "",
            dob = viewDob,
            countryCode = mobileNumber
        )

    }

    fun accountInformationEditClose() {
        fetchAccountInformation()
        waitForViewProfileLoaded()

        val editProfile = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Edit Profile"))
        val close = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close"))

        editProfile.waitFor()
        editProfile.click()
        waitForEditProfileLoaded()


        val mobileNumber = "+${piiData?.countryCode} ${piiData?.mobile}"
        val editDob = formatDobToDdMmYyyy(piiData?.dob)
        val viewDob = formatDobWithAge(piiData?.dob)


        assertEditProfileDetails(
            name = piiData?.name ?: "",
            email = piiData?.email ?: "",
            dob = editDob,
            countryCode = mobileNumber
        )

        close.click()

        waitForViewProfileLoaded()

        assertViewProfileDetails(
            name = piiData?.name ?: "",
            email = piiData?.email ?: "",
            dob = viewDob,
            countryCode = mobileNumber
        )

    }

    private fun fieldContainer(label: String): Locator {
        return page.locator("h5", Page.LocatorOptions().setHasText(label))
            .locator("xpath=ancestor::div[.//h5][1]")
    }

    fun editableInputByLabel(label: String): Locator {
        return fieldContainer(label)
            .locator("input")
            .first()
    }


    fun waitForEditProfileLoaded() {
        editableInputByLabel("Name").waitFor()
        editableInputByLabel("Email").waitFor()
        editableInputByLabel("Date of Birth").waitFor()

        // Mobile Number is read-only
        readOnlyValueByLabel("Mobile Number").waitFor()
    }

    fun readOnlyValueByLabel(label: String): Locator {
        return fieldContainer(label)
            .locator("p")
    }


    fun assertEditProfileDetails(
        name: String,
        email: String,
        dob: String,
        countryCode: String
    ) {
        logger.info {
            "${editableInputByLabel("Name").inputValue()} : $name, ${editableInputByLabel("Email").inputValue()} : $email, ${
                editableInputByLabel(
                    "Date of Birth"
                ).inputValue()
            } : $dob, ${
                readOnlyValueByLabel(
                    "Mobile Number"
                ).innerText().trim()
            } : $countryCode"
        }

        assertTrue(editableInputByLabel("Name").inputValue().equals(name))
        assertTrue(editableInputByLabel("Email").inputValue().equals(email))
        assertTrue(editableInputByLabel("Date of Birth").inputValue().equals(dob))
        assertEquals(readOnlyValueByLabel("Mobile Number").innerText().trim(), countryCode)
    }


    /**------------Health Metrics----------------*/
    fun assertHealthMetrics() {
        fetchAccountInformation()

        val healthMetricsEdit = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Health Metrics Edit"))

        val weight = formatFlotTwoDecimal(piiData?.weight ?: 0f)
        val height = formatFlotTwoDecimal(piiData?.height ?: 0f)

        healthMetricsEdit.waitFor()

        val bmi = calculateBMIValues(height.toFloat(), weight.toFloat())
        val bmiStatus = bmiCategoryValues(bmi)

        page.getByText("Height (cm):").waitFor()
        page.getByText("Weight (kg):").waitFor()

        page.getByText(weight).waitFor()
        page.getByText(height).waitFor()
        page.getByText(bmiStatus).waitFor()
        page.getByText("${bmi}BMI").waitFor()
    }

    fun assertEditHealthMetrics() {
        fetchAccountInformation()

        val newHeight = (60..302).random().toString()
        val newWeight = (10..150).random().toString()

        val healthMetricsEdit = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Health Metrics Edit"))
        val edit =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Health Metrics Edit")).locator("span")
        healthMetricsEdit.waitFor()

        edit.click()

        val weight = formatFlotTwoDecimal(piiData?.weight ?: 0f)
        val height = formatFlotTwoDecimal(piiData?.height ?: 0f)


        val editHeight = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter height in cm"))
        val editWeight = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter weight in kg"))

        assertEquals(editHeight.inputValue(), height)
        assertEquals(editWeight.inputValue(), weight)


        editHeight.fill("10")
        page.getByText("Height must be within range").waitFor()

        editWeight.fill("3")
        page.getByText("Weight must be within range").waitFor()


        editHeight.fill(newHeight)
        editWeight.fill(newWeight)

        val saveButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save Changes"))
        assertTrue(saveButton.isEnabled)
        saveButton.click()

        fetchAccountInformation()

        val updateWeight = formatFlotTwoDecimal(piiData?.weight ?: 0f)
        val updateHeight = formatFlotTwoDecimal(piiData?.height ?: 0f)

        assertEquals(newHeight, updateHeight)
        assertEquals(newWeight, updateWeight)

    }


    /**------------Questioner----------------*/
    fun assertQuestionerInitialCheck() {
        val questionHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View/Edit Questionnaire"))
        val editQuestionerButton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View/Edit Responses"))
        val questionDialog = page.locator(".bg-zinc-900").first()

        questionHeading.waitFor()
        editQuestionerButton.waitFor()

        editQuestionerButton.click()

        questionDialog.waitFor()

        question_1()
        question_3()
        question_4()
        question_5()
        question_6()
        question_7()
        question_8()
        question_9()
        question_10()
        question_11()
    }


    fun question_1() { //What is your food preference?
        logger.error { "Questioner 1" }
        val question =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your food preference?"))

        val vegetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegetarian Primarily plant-"))
        val nonVegetarian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Non-Vegetarian Consumes meat"))
        val vegan = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegan Exclusively plant-based"))
        val eggetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Eggetarian Primarily plant-"))

        question.waitFor()
        //options
        vegetarian.waitFor()
        nonVegetarian.waitFor()
        vegan.waitFor()
        eggetarian.waitFor()


        assertFalse(previousButton.isEnabled)
        vegetarian.click()
        answersStored["food_preference"] = "vegetarian"
    }

    fun question_3() { //What is your cuisine preference?
        logger.error { "Questioner 3" }
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your cuisine"))


        val northIndian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("North Indian"))
        val southIndian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("South Indian"))
        val jain =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Jain"))
        val mediterranean =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Mediterranean"))
        val continental =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continental"))
        val chinese =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Chinese"))
        val arabian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Arabian"))
        val asian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Asian"))
        val japanese =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Japanese"))

        val cuisineOptions = listOf(
            title,
            northIndian,
            southIndian,
            jain,
            mediterranean,
            continental,
            chinese,
            arabian,
            asian,
            japanese
        )

        cuisineOptions.forEach { it.waitFor() }


        northIndian.click()
        southIndian.click()
        jain.click()


        answersStored["cuisine_preference"] =
            arrayOf(
                "North Indian", "South Indian", "Jain"
            )

        nextButton.click()

    }

    fun question_4() { //Which of the following best describes your daily eating habits?
        logger.error { "Questioner 4" }
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        val homeCooked =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Primarily Home Cooked Meals")
            )

        val occasionalSnacker =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Occasional Snacker")
            )

        val oftenDiningOut =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Often dining out")
            )

        val frequentJunk =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Frequent junk/processed food")
            )

        val skipsMeals =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Skips meals")
            )

        val lateNightEating =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Late-night eating")
            )

        val intermittentFasting =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Intermittent fasting / time-")
            )

        val lifestyleOptions = listOf(
            title,
            homeCooked,
            occasionalSnacker,
            oftenDiningOut,
            frequentJunk,
            skipsMeals,
            lateNightEating,
            intermittentFasting
        )

        // ✅ Single wait point
        lifestyleOptions.forEach { it.waitFor() }

        homeCooked.click()
        answersStored["daily_eating_habit"] = "Primarily Home Cooked Meals"


        /*  Optional: random selection (single click)
         lifestyleOptions
             .drop(1) // exclude title
             .random()
             .click()*/


    }

    fun question_5() { //What is your past experience with diets?
        logger.error { "Questioner 5" }
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("What is your past experience"))

        val triedAndWorks =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tried and found what works")
            )

        val triedVarious =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tried various diets, unsure")
            )

        val triedAll =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tried them all, hard to")
            )

        val none =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("None")
            )

        val previous =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Previous")
            )

        val experienceOptions = listOf(
            title,
            triedAndWorks,
            triedVarious,
            triedAll,
            none,
            previous
        )

        // ✅ wait once
        experienceOptions.forEach { it.waitFor() }

        none.click()

        answersStored["diet_experience"] = "None"


        /*   click each option → go back using Previous
            listOf(triedAndWorks, triedVarious, triedAll, none).forEach { option ->
                option.click()
                previous.waitFor()
                previous.click()
            }*/
    }

    fun question_6() { //How familiar are you with tracking calories or macronutrients and micronutrients?
        logger.error { "Questioner 6" }
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How familiar are you with"))

        val veryFamiliar =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Very familiar, successful")
            )

        val trackedABit =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tracked a bit, unsure of my")
            )

        val neverTracked =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Never tracked, need guidance")
            )

        val options = listOf(
            title,
            veryFamiliar,
            trackedABit,
            neverTracked
        )

        // ✅ wait once for everything
        options.forEach { it.waitFor() }

        neverTracked.click()

        answersStored["nutrition_tracking_experience"] = "Never tracked, need guidance"

        /*      // 🎯 choose one (random)
              options
                  .drop(1) // exclude title
                  .random()
                  .click()*/
    }

    fun question_7() { //Do you have any food allergies?
        logger.error { "Questioner 7" }
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you have any food"))

        val milk =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Milk or dairy"))
        val peanuts =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Peanuts"))
        val treeNuts =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Tree nuts"))
        val soy =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Soy"))
        val gluten =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Gluten (Wheat)"))
        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        val others =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))

        val othersTextBox =
            page.getByRole(
                AriaRole.TEXTBOX,
                Page.GetByRoleOptions().setName("Please specify...")
            )

        val options = listOf(
            title,
            milk,
            peanuts,
            treeNuts,
            soy,
            gluten,
            none,
            others
        )

        // ✅ wait once
        options.forEach { it.waitFor() }

        // 🎯 select multiple (example: random 1–2 allergies)
        val selectable = listOf(milk, peanuts)

        selectable.forEach { it.click() }

        answersStored["allergy"] = arrayOf("Milk or dairy", "Eggs")
        nextButton.click()
    }

    fun question_8() { //Do you have any food intolerances?

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you have any food"))

        val lactose =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Lactose"))

        val caffeine =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Caffeine"))

        val gluten =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Gluten"))

        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))

        val subtitle = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("(Select all that apply —"))

        val options = listOf(
            title,
            lactose,
            caffeine,
            gluten,
            none,
            subtitle
        )

        // ✅ wait once
        options.forEach { it.waitFor() }

        lactose.click()
        caffeine.click()

        answersStored["intolerance"] = arrayOf("Lactose", "Caffeine")

        nextButton.click()
    }

    fun question_9() { //How much caffeine do you typically consume in a day - including coffee, tea, energy drinks, or other caffeinated products?

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How much caffeine do you"))

        val noneOrRarely =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("None or Rarely")
            )

        val twoServings =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-2 servings")
            )

        val fourServings =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-4 servings")
            )

        val moreServings =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("or more servings")
            )

        val options = listOf(
            title,
            noneOrRarely,
            twoServings,
            fourServings,
            moreServings
        )


        options.forEach { it.waitFor() }

        answersStored["caffeine_consumption"] = "None or Rarely"

        noneOrRarely.click()

    }

    fun question_10() { //How active are you in a typical week?

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How active are you in a"))

        val sedentary =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Sedentary: <3 hrs/week")
            )

        val lightlyActive =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Lightly Active: 3–5 hrs/week")
            )

        val moderatelyActive =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Moderately Active: 5–7 hrs/")
            )

        val veryActive =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Very Active: >7 hrs/week")
            )

        val hardlyExercise =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Hardly Exercise")
            )

        val options = listOf(
            title,
            sedentary,
            lightlyActive,
            moderatelyActive,
            veryActive,
            hardlyExercise
        )

        // ✅ wait once
        options.forEach { it.waitFor() }

        answersStored["typical_day"] = "hardly_exercise"
        hardlyExercise.click()

        /*     // 🎯 select ONE option (radio behavior)
             options
                 .drop(1) // exclude title
                 .random()
                 .click()*/
    }


    fun question_11() { //What type of exercise do you usually do?



    }

    private fun question_2() { //Which of the following do you consume?

    }


    fun question_12() {
        // When do you usually work out or prefer to work out?
    }

    fun question_13() {
        // Equipments available
    }

    fun question_14() {
        // How would you describe your sleep?
    }

    fun question_15() {
        // What time do you usually go to bed on weekdays?
    }

    fun question_16() {
        // What time do you usually wake up on weekdays?
    }

    fun question_17() {
        // What time do you usually go to bed on weekends?
    }

    fun question_18() {
        // What time do you usually wakeup on weekends?
    }

    fun question_19() {
        // Would you like to set your ideal bedtime or wakeup time?
    }

    fun question_20() {
        // Set your ideal Bedtime
    }

    fun question_21() {
        // Set your ideal Waketime
    }

    fun question_22() {
        // How satisfied are you with your sleep?
    }

    fun question_23() {
        // Do you wake up refreshed?
    }

    fun question_24() {
        // What is the duration of your sun exposure on a day-to-day basis?
    }

    fun question_25() {
        // During which part of the day are you usually exposed to direct sunlight?
    }

    fun question_26() {
        // How often do you look for external motivation to stick to your wellness routine?
    }

    fun question_27() {
        // In the past month, how often have you felt stressed, sad, or low?
    }

    fun question_28() {
        // How well do you deal with stress?
    }

    fun question_29() {
        // How often do you eat in response to emotions rather than physical hunger?
    }

    fun question_30() {
        // What type of snacks do you usually indulge in?
    }

    fun question_31() {
        // What's your current menstrual status?
    }

    fun question_32() {
        // Are you pregnant?
    }

    fun question_33() {
        // How many cigarettes do you typically smoke in a day?
    }

    fun question_34() {
        // How many alcoholic drinks do you consume per week?
    }

    fun question_35() {
        // Please select any additional dietary supplements you take
    }

    fun question_36() {
        // Do you have a family history of any medical conditions?
    }

    fun question_37() {
        // Do you currently have or have ever been diagnosed with any medical conditions?
    }

    fun question_38() {
        // Which of the following best describes your GI condition?
    }

    fun question_39() {
        // Which of the following best describes your skin condition?
    }

    fun question_40() {
        // Which of the following best describes your bone or joint condition?
    }

    fun question_41() {
        // Which of the following best describes your neurological condition?
    }

    fun question_42() {
        // How would you best describe your Diabetes status?
    }

    fun question_43() {
        // Which of the following best describes your thyroid condition?
    }

    fun question_44() {
        // Which of the following best describes your liver condition?
    }

    fun question_45() {
        // Which of the following best describes your kidney condition?
    }

    fun question_46() {
        // Which of the following best describes your heart condition?
    }

    fun question_47() {
        // Which of the following best describes your respiratory condition?
    }

    fun question_48() {
        // Which of the following best describes your auto-immune condition?
    }

    fun question_49() {
        // What is your current cancer status?
    }

    fun question_50() {
        // Please mention the type of cancer
    }

    fun question_51() {
        // Are you currently taking any of the following types of medicines?
    }

    fun question_52() {
        // What is your waist circumference at its narrowest point?
    }


}









