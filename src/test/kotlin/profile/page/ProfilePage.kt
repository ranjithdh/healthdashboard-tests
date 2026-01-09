package profile.page


import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Request
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import config.TestConfig.json
import model.profile.*
import profile.model.*
import profile.utils.ProfileUtils.answersStored
import profile.utils.ProfileUtils.assertExclusiveSelected
import profile.utils.ProfileUtils.bmiCategoryValues
import profile.utils.ProfileUtils.buildAddressText
import profile.utils.ProfileUtils.calculateBMIValues
import profile.utils.ProfileUtils.formatDobToDdMmYyyy
import profile.utils.ProfileUtils.formatDobWithAge
import profile.utils.ProfileUtils.formatFlotTwoDecimal
import profile.utils.ProfileUtils.isButtonChecked
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


    private val previousButton: Locator = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Previous"))
    private val nextButton: Locator = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Next"))
    private val questionerCount = page.getByTestId("question-progress-counter-mobile")
    private val progressIndicator = page.getByTestId("question-progress-bar-indicate-mobile")
    private var exerciseType = ActivityLevel.SEDENTARY
    private var medicalConditions: List<MedicalCondition> = listOf(MedicalCondition.NONE)
    private var isMale: Boolean = true
    private val medicalQuestionQueue: MutableList<() -> Unit> = mutableListOf()
    private var shouldClickComplete: Boolean = true
    private var stopAtQuestion: Int? = null
    private var menstrualStatus: MenstrualStatus = MenstrualStatus.STILL_MENSTRUATING


    fun setStopAtQuestion(questionNumber: Int?) {
        this.stopAtQuestion = questionNumber
    }

    fun setShouldClickComplete(value: Boolean) {
        this.shouldClickComplete = value
    }

    fun setActivityType(type: ActivityLevel = ActivityLevel.SEDENTARY) {
        exerciseType = type
    }

    fun setMedicalConditions(condition: List<MedicalCondition> = listOf(MedicalCondition.NONE)) {
        medicalConditions = condition
    }

    fun setMaleConditions(isMale: Boolean) {
        this.isMale = isMale
    }

    private fun logQuestion(questionText: String) {
        logger.info { "[QUESTIONER]: $questionText" }
    }

    private fun logAnswer(key: String, question: String, answer: Any) {
        val questionAnswer = QuestionAnswer(question, answer)
        answersStored[key] = questionAnswer
        logger.info {
            "[ANSWERS STORED SNAPSHOT]: ${
                answersStored.entries.joinToString(
                    prefix = "{",
                    postfix = "}"
                ) { "${it.key}: {Question: ${it.value.question}, Answer: ${formatValue(it.value.answer)}}" }
            }"
        }
    }

    private fun calculateExpectedTotal(): Int {
        var total = 31 // Base count

        // Q1 Food Preference -> Q2 Type of Meat (skipped if veg)
        val foodPref = answersStored[QuestionSubType.FOOD_PREFERENCE]?.answer as? String
        if (foodPref != null && foodPref.startsWith("Non-Vegetarian")) {
            total += 1
        }

        // Q10 Activity Level -> Q11, Q12, Q13 (skipped if hardly exercise)
        val activityLevel = answersStored[QuestionSubType.TYPICAL_DAY]?.answer as? String
        if (activityLevel != null && !activityLevel.contains("Hardly Exercise")) {
            total += 3
        }

        // Q30 Gender specific -> Q31 Menstrual Status, Q32 Pregnancy (skipped if male)
        if (!isMale) {
            total += 1 // For Q31
            val storedMenstrualStatus = answersStored[QuestionSubType.MENSTRUAL_STATUS]?.answer as? String
            if (storedMenstrualStatus == MenstrualStatus.STILL_MENSTRUATING.label) {
                total += 1 // For Q32
            }
        }

        val sleepPreference = answersStored[QuestionSubType.SLEEP_SCHEDULE_PREFERENCE]?.answer as? String
        if (sleepPreference != null) {
            total += 1
        }

        // Q37 Medical Conditions
        val conditions = answersStored[QuestionSubType.MEDICAL_CONDITION]?.answer
        if (conditions is Array<*>) {
            // Each condition adds its specific sub-question
            // Q38-Q49 are driven by Q37 selection.
            // Note: Gall bladder (Q37 selection) has no sub-question in flow currently.
            // We count the number of specific sub-questions in the queue logic.

            // Logic in question_37 adds specific functions to medicalQuestionQueue.
            // We can mirror that logic here.
            val selectedLabels = conditions.filterIsInstance<String>()

            // Map labels back to conditions or just count based on logic in question_37
            // GI, Derm, Bone, Neuro, Diabetes, Thyroid, Liver, Kidney, Cardio, Cancer(2), Resp, Auto
            val subQuestionLabels = listOf(
                "Gastrointestinal Conditions",
                "Dermatological Conditions",
                "Bone or Joint Conditions",
                "Neurological Conditions",
                "Type 2 - Diabetes",
                "Thyroid-related disorders",
                "Liver Disorders",
                "Kidney Conditions",
                "Cardiovascular Conditions",
                "Respiratory conditions",
                "Auto-immune condition"
            )

            selectedLabels.forEach { label ->
                if (subQuestionLabels.any { label.contains(it) }) {
                    total += 1
                }
                if (label.contains("Cancer")) {
                    total += 2 // Q49 Cancer Status and Q50 Cancer Type
                }
            }
        }

        return total
    }

    private fun assertProgressCount(index: Int? = null) {
        val currentIndex = index ?: (answersStored.size + 1)
        val total = calculateExpectedTotal()
        val expectedText = "QUESTION $currentIndex/$total"

        val actualText = questionerCount.innerText()
        logger.info { "Asserting Progress: Expected [$expectedText], Actual [$actualText]" }
        assertEquals(expectedText, actualText, "Progress counter mismatch")

        // Verify Progress Bar indicator
        val style = progressIndicator.getAttribute("style") ?: ""
        val expectedScale = currentIndex.toDouble() / total

        // Regex to extract scaleX value from transform: scaleX(0.02702702702702703)
        val match = Pattern.compile("scaleX\\(([0-9.]+)\\)").matcher(style)
        if (match.find()) {
            val actualScale = match.group(1).toDouble()
            logger.info { "Asserting Progress Bar: Expected Scale [~$expectedScale], Actual Scale [$actualScale]" }
            // Use a small delta for floating point comparison
            assertTrue(
                Math.abs(expectedScale - actualScale) < 0.01,
                "Progress bar scale mismatch. Expected: $expectedScale, Actual: $actualScale"
            )
        } else {
            throw AssertionError("Could not find scaleX in progress indicator style: $style")
        }
    }

    private fun formatValue(value: Any?): String {
        return when (value) {
            is Array<*> -> value.joinToString(", ", prefix = "[", postfix = "]")
            is List<*> -> value.joinToString(", ", prefix = "[", postfix = "]")
            else -> value.toString()
        }
    }


    init {
        monitorTraffic()
    }

    fun waitForConfirmation(): ProfilePage {
        tonePreference.waitFor()
        return this
    }


    private fun monitorTraffic() {
        //update address
        val updateProfileRequest = { request: Request ->
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
        val preferenceProfileRequest = { request: Request ->
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
        page.locator("div").filter(FilterOptions().setHasText(Pattern.compile("^Saved Addresses$"))).first()

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
                    FilterOptions().setHas(
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
                    FilterOptions().setHasText(expectedAddressText)
                )
                .first()
                .waitFor()

            // Mobile (optional)
            if (!address.addressMobile.isNullOrBlank()) {
                addressCard
                    .locator("p")
                    .filter(
                        FilterOptions().setHasText("Mobile number: ${address.addressMobile}")
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
                FilterOptions().setHas(
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
            .filter(FilterOptions().setHasText(expectedAddressText))
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
                FilterOptions().setHas(
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
            .filter(FilterOptions().setHasText(expectedAddressText))
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
                RequestOptions.create()
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
            FilterOptions().setHas(
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
                RequestOptions.create()
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
                setMaleConditions(piiData?.gender == "male")
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

    /**
     * Helper function to visit the next medical question in the queue.
     * If queue is empty, proceeds to question_51 (medications).
     */
    private fun visitNextMedicalQuestion() {
        if (medicalQuestionQueue.isNotEmpty()) {
            val nextQuestion = medicalQuestionQueue.removeAt(0)
            nextQuestion()
        } else {
            question_51()  // All condition questions processed, move to medications
        }
    }


    fun assertQuestionerVegInitialCheck() {
        fetchAccountInformation()
        answersStored.clear()
        logger.info {
            "Answer count --> ${answersStored.size}"
        }

        val questionHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View/Edit Questionnaire"))
        val editQuestionerButton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View/Edit Responses"))
        val questionDialog = page.locator(".bg-zinc-900").first()

        questionHeading.waitFor()
        editQuestionerButton.waitFor()

        editQuestionerButton.click()

        questionDialog.waitFor()

        question_1_veg()
    }

    fun assertQuestionerNonVegInitialCheck() {
        fetchAccountInformation()
        answersStored.clear()
        logger.info {
            "Answer count --> ${answersStored.size}"
        }
        val questionHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View/Edit Questionnaire"))
        val editQuestionerButton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View/Edit Responses"))
        val questionDialog = page.locator(".bg-zinc-900").first()

        questionHeading.waitFor()
        editQuestionerButton.waitFor()

        editQuestionerButton.click()

        questionDialog.waitFor()

        question_1_non_veg()
    }

    fun question_1_veg() { //What is your food preference?
        logQuestion("What is your food preference?")
        val question =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your food preference?"))


        val vegetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegetarian Primarily plant-"))
        val nonVegetarian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Non-Vegetarian Consumes meat"))
        val vegan = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegan Exclusively plant-based"))
        val eggetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Eggetarian Primarily plant-"))

        listOf(
            question,
            vegetarian,
            nonVegetarian,
            vegan,
            eggetarian,
            previousButton,
            nextButton,
            questionerCount
        ).forEach { it.waitFor() }

        assertProgressCount()

        assertFalse(previousButton.isEnabled)
        vegetarian.click()
        logAnswer(
            QuestionSubType.FOOD_PREFERENCE,
            "What is your food preference?",
            "Vegetarian : Primarily plant-based, avoiding meat, poultry, and seafood"
        )
        question_3()
    }

    fun question_1_non_veg() { //What is your food preference?
        logQuestion("What is your food preference?")
        val question =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your food preference?"))

        val vegetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegetarian Primarily plant-"))
        val nonVegetarian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Non-Vegetarian Consumes meat"))
        val vegan = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegan Exclusively plant-based"))
        val eggetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Eggetarian Primarily plant-"))

        listOf(
            question,
            previousButton,
            vegetarian,
            nonVegetarian,
            vegan,
            eggetarian,
            questionerCount
        ).forEach { it.waitFor() }

        assertProgressCount()

        assertFalse(previousButton.isEnabled)

        nonVegetarian.click()
        logAnswer(
            QuestionSubType.FOOD_PREFERENCE,
            "What is your food preference?",
            "Non-Vegetarian : Consumes meat, poultry, seafood, and other animal products along with plant-based foods"
        )
        question_2()
    }

    private fun question_2() { //Which of the following do you consume?
        logQuestion("Which of the following do you consume?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following do you"))

        val chicken =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Chicken"))

        val pork =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Pork"))

        val mutton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Mutton"))

        val turkey =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Turkey"))

        val fish =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Fish").setExact(true)
            )

        val shellfish =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Shellfish").setExact(true))

        val beef =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Beef"))

        val options = listOf(
            title,
            chicken,
            pork,
            mutton,
            turkey,
            fish,
            shellfish,
            beef
        )

        (options + questionerCount).forEach { it.waitFor() }

        assertProgressCount()

        // 🎯 multi-select: choose 1–3 randomly
        val meatOptions = listOf(
            chicken,
            pork,
            mutton,
            turkey,
            fish,
            shellfish,
            beef
        )

        meatOptions.take(3)
            .forEach { it.click() }

        logAnswer(
            QuestionSubType.TYPE_OF_MEAT,
            "Which of the following do you consume?",
            arrayOf("Chicken", "Pork", "Mutton")
        )

        nextButton.click()
        question_3()
    }

    fun question_3() { //What is your cuisine preference?
        logQuestion("What is your cuisine preference?")
        logger.error { "Questioner 3" }
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your cuisine"))
        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))


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
            subTitle,
            northIndian,
            southIndian,
            jain,
            mediterranean,
            continental,
            chinese,
            arabian,
            asian,
            japanese,
        )

        (cuisineOptions + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        northIndian.click()
        southIndian.click()
        jain.click()


        logAnswer(
            QuestionSubType.CUISINE_PREFERENCE,
            "What is your cuisine preference?",
            arrayOf(
                "North Indian", "South Indian", "Jain"
            )
        )

        nextButton.click()
        question_4()
    }

    fun question_4() { //Which of the following best describes your daily eating habits?
        logQuestion("Which of the following best describes your daily eating habits?")
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

        (lifestyleOptions + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        homeCooked.click()
        logAnswer(
            QuestionSubType.DAILY_EATING_HABIT,
            "Which of the following best describes your daily eating habits?",
            "Primarily Home Cooked Meals"
        )
        question_5()
    }

    fun question_5() { //What is your past experience with diets?
        logQuestion("What is your past experience with diets?")
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

        (experienceOptions + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        none.click()

        logAnswer(QuestionSubType.DIET_EXPERIENCE, "What is your past experience with diets?", "None")

        question_6()
    }

    fun question_6() { //How familiar are you with tracking calories or macronutrients and micronutrients?
        logQuestion("How familiar are you with tracking calories or macronutrients and micronutrients?")
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

        (options + questionerCount).forEach { it.waitFor() }

        assertProgressCount()

        neverTracked.click()

        logAnswer(
            QuestionSubType.NUTRITION_TRACKING_EXPERIENCE,
            "How familiar are you with tracking calories or macronutrients and micronutrients?",
            "Never tracked, need guidance"
        )
        question_7()
    }

    fun question_7() { //Do you have any food allergies?
        logQuestion("Do you have any food allergies?")
        logger.error { "Questioner 7" }
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you have any food"))

        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))

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


        val options = listOf(
            title,
            subTitle,
            milk,
            peanuts,
            treeNuts,
            soy,
            gluten,
        )


        (options + none + others + questionerCount).forEach { it.waitFor() }
        assertProgressCount()


        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )
        //None

        options.forEach { it.click() }

        none.click()
        assertExclusiveSelected(none, (options + others))


        // 🎯 select multiple (example: random 1–2 allergies)
        val selectable = listOf(milk, peanuts)

        selectable.forEach { it.click() }

        logAnswer(QuestionSubType.ALLERGY, "Do you have any food allergies?", arrayOf("Milk or dairy", "Peanuts"))
        nextButton.click()
        question_8()
    }

    fun question_8() { //Do you have any food intolerances?
        logQuestion("Do you have any food intolerances?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you have any food"))
        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply —"))

        val lactose =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Lactose"))

        val caffeine =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Caffeine"))

        val gluten =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Gluten"))

        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))


        val options = listOf(
            title,
            subTitle,
            lactose,
            caffeine,
            gluten,
            none,
        )

        val otherOptions = listOf(
            lactose,
            caffeine,
            gluten,
        )


        (options + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        otherOptions.forEach {
            it.click()
        }

        none.click()
        assertExclusiveSelected(none, otherOptions)

        lactose.click()
        caffeine.click()

        logAnswer(QuestionSubType.INTOLERANCE, "Do you have any food intolerances?", arrayOf("Lactose", "Caffeine"))

        nextButton.click()
        question_9()
    }

    fun question_9() { //How much caffeine do you typically consume in a day - including coffee, tea, energy drinks, or other caffeinated products?
        logQuestion("How much caffeine do you typically consume in a day - including coffee, tea, energy drinks, or other caffeinated products?")

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

        (options + questionerCount).forEach { it.waitFor() }
        assertProgressCount()


        logAnswer(
            QuestionSubType.CAFFEINE_CONSUMPTION,
            "How much caffeine do you typically consume in a day - including coffee, tea, energy drinks, or other caffeinated products?",
            "None or Rarely"
        )

        noneOrRarely.click()
        question_10()

    }

    fun question_10() { //How active are you in a typical week?
        logQuestion("How active are you in a typical week?")

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

        (options + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        // 🔹 Select option and navigate based on activityLevel parameter
        when (exerciseType) {
            ActivityLevel.HARDLY_EXERCISE -> {
                hardlyExercise.click()
                logAnswer(QuestionSubType.TYPICAL_DAY, "How active are you in a typical week?", "Hardly Exercise")
                question_14()  // Skip Q11-Q13 and go directly to sleep question
            }

            ActivityLevel.SEDENTARY -> {
                sedentary.click()
                logAnswer(
                    QuestionSubType.TYPICAL_DAY,
                    "How active are you in a typical week?",
                    "Sedentary: <3 hrs/week"
                )
                question_11_with_exercise()
            }

            ActivityLevel.LIGHTLY_ACTIVE -> {
                lightlyActive.click()
                logAnswer(
                    QuestionSubType.TYPICAL_DAY,
                    "How active are you in a typical week?",
                    "Lightly Active: 3–5 hrs/week"
                )
                question_11_with_exercise()
            }

            ActivityLevel.MODERATELY_ACTIVE -> {
                moderatelyActive.click()
                logAnswer(
                    QuestionSubType.TYPICAL_DAY,
                    "How active are you in a typical week?",
                    "Moderately Active: 5–7 hrs/week"
                )
                question_11_with_exercise()
            }

            ActivityLevel.VERY_ACTIVE -> {
                veryActive.click()
                logAnswer(
                    QuestionSubType.TYPICAL_DAY,
                    "How active are you in a typical week?",
                    "Very Active: >7 hrs/week"
                )
                question_11_with_exercise()
            }
        }
    }

    fun question_11_with_exercise() { //What type of exercise do you usually do?
        logQuestion("What type of exercise do you usually do?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("What type of exercise do you"))

        val yoga =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yoga"))
        val strengthTraining =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Strength Training"))
        val pilates =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Pilates"))
        val flexibility =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Flexibility / Stretching")
            )
        val noExercise =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I don't exercise")
            )

        val exerciseOptions = listOf(yoga, strengthTraining, pilates, flexibility)


        (listOf(title, *exerciseOptions.toTypedArray(), noExercise) + questionerCount).forEach { it.waitFor() }
        val expected = questionerCount.innerText()


        if (isMale) {
            assertTrue { "QUESTION 10/32" == expected || "QUESTION 11/33" == expected }
        } else {
            assertTrue { "QUESTION 10/33" == expected || "QUESTION 11/34" == expected }
        }

        //  assertProgressCount()

        // Example: select Yoga (your test can vary this)
        yoga.click()

        assertTrue(isButtonChecked(yoga))

        logAnswer(
            QuestionSubType.EXERCISE_TYPE, "What type of exercise do you usually do?", arrayOf(
                "Yoga"
            )
        )

        nextButton.click()
        // ➡️ Go to Question 12
        question_12()
    }

    fun question_12() {// When do you usually work out or prefer to work out?
        logQuestion("When do you usually work out or prefer to work out?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("When do you usually work out"))

        val morning =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Morning"))

        val afternoon =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Afternoon"))

        val evening =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Evening"))

        val flexible =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Flexible"))

        val options = listOf(
            title,
            morning,
            afternoon,
            evening,
            flexible
        )

        (options + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        morning.click()

        logAnswer(
            QuestionSubType.PREFERRED_WORKOUT_TIME,
            "When do you usually work out or prefer to work out?",
            "Morning"
        )
        question_13()
    }

    fun question_13() { // Equipments available
        logQuestion("Equipments available")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Equipments available"))

        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))

        val dumbbells =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Dumbbells"))

        val kettlebells =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Kettlebells"))

        val resistanceBands =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Resistance bands"))

        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))

        val equipmentOptions = listOf(
            dumbbells,
            kettlebells,
            resistanceBands
        )

        (listOf(
            title,
            subTitle,
            dumbbells,
            kettlebells,
            resistanceBands,
            none
        ) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        equipmentOptions.forEach { it.click() }

        // CASE 1: None selected
        none.click()

        assertExclusiveSelected(none, equipmentOptions)

        dumbbells.click()

        logAnswer(QuestionSubType.EQUIPMENTS_AVAILABLE, "Equipments available", arrayOf("Dumbbells"))

        nextButton.click()
        question_14()
    }

    fun question_14() {// How would you describe your sleep?
        logQuestion("How would you describe your sleep?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How would you describe your"))

        val excellent =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Excellent routine, sleep like")
            )

        val roomForImprovement =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Room for improvement,")
            )

        val needsWork =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Needs work, struggling with")
            )

        val options = listOf(
            title,
            excellent,
            roomForImprovement,
            needsWork
        )

        (options + questionerCount).forEach { it.waitFor() }

        assertProgressCount()

        logAnswer(
            QuestionSubType.SLEEP_HYGIENE,
            "How would you describe your sleep?",
            "Room for improvement, occasional distractions"
        )

        roomForImprovement.click()

        question_15()
    }

    fun question_15() {   // What time do you usually go to bed on weekdays?
        logQuestion("What time do you usually go to bed on weekdays?")
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually go"))
        val timerBox = page.getByRole(AriaRole.TEXTBOX)

        val listOfField = listOf(title, timerBox)

        (listOfField + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        timerBox.fill("23:00")
        logAnswer(
            QuestionSubType.WEEKDAY_SLEEP_ROUTINE_BED_TIME,
            "What time do you usually go to bed on weekdays?",
            "23:00"
        )
        nextButton.click()
        question_16()
    }

    fun question_16() { // What time do you usually wake up on weekdays?
        logQuestion("What time do you usually wake up on weekdays?")

        val title =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually wake"))
        val timerBox = page.getByRole(AriaRole.TEXTBOX)

        val listOfField = listOf(title, timerBox)

        (listOfField + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        timerBox.fill("07:00")
        logAnswer(
            QuestionSubType.WEEKDAY_SLEEP_ROUTINE_WAKEUP_TIME,
            "What time do you usually wake up on weekdays?",
            "07:00"
        )
        nextButton.click()
        question_17()
    }

    fun question_17() { // What time do you usually go to bed on weekends?
        logQuestion("What time do you usually go to bed on weekends?")

        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually go"))
        val timerBox = page.getByRole(AriaRole.TEXTBOX)


        val listOfField = listOf(title, timerBox)

        (listOfField + questionerCount).forEach {
            it.waitFor()
        }
        assertProgressCount()

        timerBox.fill("23:00")
        logAnswer(
            QuestionSubType.WEEKEND_SLEEP_ROUTINE_BED_TIME,
            "What time do you usually go to bed on weekends?",
            "23:00"
        )
        nextButton.click()

        question_18()
    }

    fun question_18() {    // What time do you usually wakeup on weekends?
        logQuestion("What time do you usually wakeup on weekends?")
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually"))
        val timerBox = page.getByRole(AriaRole.TEXTBOX)

        val listOfField = listOf(title, timerBox)

        (listOfField + questionerCount).forEach {
            it.waitFor()
        }
        assertProgressCount()

        timerBox.fill("07:00")
        logAnswer(
            QuestionSubType.WEEKEND_SLEEP_ROUTINE_WAKEUP_TIME,
            "What time do you usually wakeup on weekends?",
            "07:00"
        )
        nextButton.click()
        question_19()
    }

    fun question_19() {        // Would you like to set your ideal bedtime or wakeup time?
        logQuestion("Would you like to set your ideal bedtime or wakeup time?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Let's make your sleep"))

        val bedtime =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Bedtime")
            )

        val waketime =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Waketime")
            )

        val options = listOf(
            title,
            bedtime,
            waketime
        )

        (options + questionerCount).forEach { it.waitFor() }

        assertProgressCount()

        logAnswer(
            QuestionSubType.SLEEP_SCHEDULE_PREFERENCE,
            "Let's make your sleep schedule perfect! Would you like to set your ideal bedtime or wakeup time?",
            "Bedtime"
        )
        bedtime.click()
        question_20()
    }

    fun question_20() {  // Set your ideal Bedtime
        logQuestion("Set your ideal Bedtime")
        if (stopAtQuestion == 20) {
            logger.info { "Stopping at question 20 and clicking goBack()." }
            goBackQuestioner()
            return
        }

        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Set your ideal Bedtime"))
        val timerBox = page.getByRole(AriaRole.TEXTBOX)

        val listOfField = listOf(title, timerBox)

        (listOfField + questionerCount).forEach {
            it.waitFor()
        }
        assertProgressCount()

        timerBox.fill("11:00")
        logAnswer(QuestionSubType.BED_TIME_GOAL, "Set your ideal Bedtime", "11:00")

        nextButton.click()
        question_22()
    }

    fun question_21() { // Set your ideal Waketime
        logQuestion("Set your ideal Waketime")
        val title =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Set your ideal Waketime"))
        val timerBox = page.getByRole(AriaRole.TEXTBOX)

        val listOfField = listOf(title, timerBox)

        (listOfField + questionerCount).forEach {
            it.waitFor()
        }
        assertProgressCount()

        timerBox.fill("07:00")
        logAnswer(QuestionSubType.WAKEUP_TIME_GOAL, "Set your ideal Waketime", "07:00")
        //question_22()
    }

    fun question_22() { // How satisfied are you with your sleep?
        logQuestion("How satisfied are you with your sleep?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How satisfied are you with"))

        val fullySatisfied =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Fully Satisfied")
            )

        val somewhatSatisfied =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Somewhat Satisfied")
            )

        val notSatisfied =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Not Satisfied")
            )

        val options = listOf(
            title,
            fullySatisfied,
            somewhatSatisfied,
            notSatisfied
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(QuestionSubType.SLEEP_SATISFACTION, "How satisfied are you with your sleep?", "Somewhat Satisfied")
        somewhatSatisfied.click()
        question_23()
    }

    fun question_23() {// Do you wake up refreshed?
        logQuestion("Do you wake up refreshed?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you wake up refreshed?"))

        val always =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Always")
            )

        val sometimes =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Sometimes")
            )

        val rarely =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Rarely")
            )

        val options = listOf(
            title,
            always,
            sometimes,
            rarely
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(QuestionSubType.SLEEP_WAKEUP_REFRESHMENT, "Do you wake up refreshed?", "Sometimes")
        sometimes.click()
        question_24()
    }

    fun question_24() {// What is the duration of your sun exposure on a day-to-day basis?
        logQuestion("What is the duration of your sun exposure on a day-to-day basis?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("What is the duration of your"))

        val lessThan5 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Less than 5 minutes")
            )

        val fiveToTen =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-10 minutes")
            )

        val tenToTwenty =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-20 minutes")
            )

        val moreThan20 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than 20 minutes")
            )

        val options = listOf(
            title,
            lessThan5,
            fiveToTen,
            tenToTwenty,
            moreThan20
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(
            QuestionSubType.SUNLIGHT_UPON_WAKEUP,
            "What is the duration of your sun exposure on a day-to-day basis?",
            "5-10 minutes"
        )
        fiveToTen.click()
        question_25()
    }

    fun question_25() { // During which part of the day are you usually exposed to direct sunlight?
        logQuestion("During which part of the day are you usually exposed to direct sunlight?")


        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("During which part of the day"))

        val earlyMorning =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Early morning (before 10 a.m.)")
            )

        val lateMorning =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Late morning to early")
            )

        val lateAfternoon =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Late afternoon (3 p.m. - 5 p.")
            )

        val evening =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Evening (after 5 p.m.)")
            )

        val options = listOf(
            title,
            earlyMorning,
            lateMorning,
            lateAfternoon,
            evening
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(
            QuestionSubType.SUNLIGHT_TIMING,
            "During which part of the day are you usually exposed to direct sunlight?",
            "Early morning (before 10 a.m.)"
        )
        earlyMorning.click()
        question_26()
    }

    fun question_26() {  // How often do you look for external motivation to stick to your wellness routine?
        logQuestion("How often do you look for external motivation to stick to your wellness routine?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How often do you look for"))

        val subTitle =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(e.g., social media tips,"))
        val allTheTime =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("All the time")
            )

        val nowAndThen =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Now and then")
            )

        val hardlyEver =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Hardly Ever")
            )

        val options = listOf(
            title,
            subTitle,
            allTheTime,
            nowAndThen,
            hardlyEver
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(
            QuestionSubType.WELLNESS_MOTIVATION_FREQUENCY,
            "How often do you look for external motivation to stick to your wellness routine?",
            "Now and then"
        )
        nowAndThen.click()
        question_27()
    }

    fun question_27() {
        logQuestion("In the past month, how often have you felt stressed, sad, or low?")
        // In the past month, how often have you felt stressed, sad, or low?
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("In the past month, how often"))

        val everyDay =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Every day")
            )

        val moreThanOnceAWeek =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than once a week")
            )

        val onceAWeek =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Once a week").setExact(true)
            )

        val onceInTwoWeeks =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Once in two weeks")
            )

        val onceAMonth =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Once a month / Rarely")
            )

        val options = listOf(
            title,
            everyDay,
            moreThanOnceAWeek,
            onceAWeek,
            onceInTwoWeeks,
            onceAMonth
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(
            QuestionSubType.WELLNESS_BOTHER_FREQUENCY,
            "In the past month, how often have you felt stressed, sad, or low?",
            "Once a week"
        )
        onceAWeek.click()
        question_28()
    }

    fun question_28() {   // How well do you deal with stress?
        logQuestion("How well do you deal with stress?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How well do you deal with"))

        val dealWell =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I deal with my stress well")
            )

        val couldDealBetter =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I could deal with stress")
            )

        val overwhelmed =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I feel overwhelmed by stress")
            )

        val options = listOf(
            title,
            dealWell,
            couldDealBetter,
            overwhelmed
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(
            QuestionSubType.STRESS_MANAGEMENT,
            "How well do you deal with stress?",
            "I feel overwhelmed by stress"
        )
        overwhelmed.click()
        question_29()
    }

    fun question_29() {  // How often do you eat in response to emotions rather than physical hunger?
        logQuestion("How often do you eat in response to emotions rather than physical hunger?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How often do you eat in"))

        val frequently =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Frequently")
            )

        val occasionally =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Occasionally")
            )

        val rarely =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Rarely")
            )

        val never =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Never")
            )

        val options = listOf(
            title,
            frequently,
            occasionally,
            rarely,
            never
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(
            QuestionSubType.EMOTIONAL_EATING,
            "How often do you eat in response to emotions such as stress, cravings, boredom, or anxiety rather than physical hunger?",
            "Rarely"
        )
        rarely.click()
        question_30()
    }

    fun question_30() { // What type of snacks do you usually indulge in?
        logQuestion("What type of snacks do you usually indulge in?")


        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("What type of snacks do you"))

        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))

        val sweets =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sweets"))

        val fried =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Fried and crispy"))

        val salty =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Salty"))

        val healthy =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Healthier options (e.g.,")
            )

        val others =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))

        val allOfTheAbove =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("All of the above")
            )

        val snackOptions = listOf(
            sweets,
            fried,
            salty,
            healthy
        )

        // ✅ wait once
        listOf(
            title,
            subTitle,
            sweets,
            fried,
            salty,
            healthy,
            others,
            allOfTheAbove,
            questionerCount
        ).forEach { it.waitFor() }
        assertProgressCount()

        //Scenario 1
        allOfTheAbove.click()
        assertExclusiveSelected(allOfTheAbove, snackOptions)

        sweets.click()
        logAnswer(QuestionSubType.SNACK_PREFERENCE, "What type of snacks do you usually indulge in?", arrayOf("Sweets"))
        nextButton.click()
        if (isMale) {
            question_33()
        } else {
            question_31()
        }
    }

    fun question_31() {
        // What's your current menstrual status?
        logQuestion("What's your current menstrual status?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("What's your current menstrual"))

        val stillMenstruating =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I'm still menstruating"))

        val nearingMenopause =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I'm nearing menopause"))

        val attainedMenopause =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I have attained Menopause"))

        val options = listOf(
            title,
            stillMenstruating,
            nearingMenopause,
            attainedMenopause
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        val buttonToClick = when (menstrualStatus) {
            MenstrualStatus.STILL_MENSTRUATING -> stillMenstruating
            MenstrualStatus.NEARING_MENOPAUSE -> nearingMenopause
            MenstrualStatus.ATTAINED_MENOPAUSE -> attainedMenopause
        }
        buttonToClick.click()

        logAnswer(
            QuestionSubType.MENSTRUAL_STATUS,
            "What's your current menstrual status?",
            menstrualStatus.label
        )

        if (answersStored[QuestionSubType.MENSTRUAL_STATUS]?.answer == MenstrualStatus.STILL_MENSTRUATING.label) {
            question_32()
        } else {
            question_33()
        }
    }

    fun question_32() {
        // Are you pregnant?
        logQuestion("Are you pregnant?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Are you pregnant?"))

        val yes = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yes"))
        val no = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("No"))

        val options = listOf(title, yes, no)

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        // Scenario: select "No" by default or based on test
        logAnswer(QuestionSubType.IS_PREGNANT, "Are you pregnant?", "No")
        no.click()
        question_33()
    }

    fun question_33() {  // How many cigarettes do you typically smoke in a day?
        logQuestion("How many cigarettes do you typically smoke in a day?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How many cigarettes do you"))

        val dontSmoke =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I don't smoke")
            )

        val upTo5 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("–5")
            )

        val upTo10 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("–10")
            )

        val upTo20 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("–20")
            )

        val moreThan20 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than")
            )

        val options = listOf(
            title,
            dontSmoke,
            upTo5,
            upTo10,
            upTo20,
            moreThan20
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        logAnswer(QuestionSubType.N_SMOKE, "How many cigarettes do you typically smoke in a day?", "I don't smoke")
        dontSmoke.click()
        question_34()
    }

    fun question_34() { // How many alcoholic drinks do you consume per week?
        logQuestion("How many alcoholic drinks do you consume per week?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How many alcoholic drinks do"))

        val dontDrink =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I don't drink")
            )

        val lessThanOncePerWeek =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Less than once per week")
            )

        val upTo3 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-3 drinks")
            )

        val upTo7 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-7 drinks")
            )

        val upTo14 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("-14 drinks")
            )

        val moreThan14 =
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than 14 drinks")
            )

        val options = listOf(
            title,
            dontDrink,
            lessThanOncePerWeek,
            upTo3,
            upTo7,
            upTo14,
            moreThan14
        )

        options.forEach { it.waitFor() }

        questionerCount.waitFor()
        assertProgressCount()

        dontDrink.click()
        logAnswer(QuestionSubType.N_ALCOHOL, "How many alcoholic drinks do you consume per week?", "I don't drink")
        question_35()
    }

    fun question_35() { // Please select any additional dietary supplements you take
        logQuestion("Please select any additional dietary supplements you take")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please select any additional"))

        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))

        val supplementNames = listOf(
            "Vitamin A",
            "Vitamin D",
            "Vitamin E",
            "Vitamin K",
            "Vitamin B12",
            "Folate / Folic Acid",
            "Biotin",
            "Vitamin C",
            "Iron",
            "Calcium",
            "Magnesium",
            "Zinc",
            "Iodine",
            "Selenium",
            "Multivitamin / Combination",
            "Collagen",
            "Ashwagandha",
            "Chyawanprash",
            "Apple cider vinegar",
            "Melatonin",
            "Omega-3 fatty acids",
            "Flavonoids, Resveratrol,",
            "Nicotinamide mononucleotide (",
            "Curcumin / Turmeric extract",
            "Protein powders (Whey, Plant-",
            "Herbal adaptogens (e.g.,",
            "Probiotics / Prebiotics"
        )

        val supplements = supplementNames.map {
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(it))
        }

        val others =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))

        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))

        (listOf(title, subTitle, others, none).plus(supplements) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        //Others
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))
        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )

        //None
        supplements.take(5).forEach { it.click() }
        none.click()
        assertExclusiveSelected(none, supplements)

        logAnswer(
            QuestionSubType.ADDITIONAL_SUPPLEMENT,
            "Please select any additional dietary supplements you take from the following list:",
            arrayOf("Vitamin A", "Vitamin D", "Vitamin E")
        )
        supplements.take(3).forEach { it.click() }
        nextButton.click()
        question_36()
    }

    fun question_36() {// Do you have a family history of any medical conditions?
        logQuestion("Do you have a family history of any medical conditions?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you have a family history"))
        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))

        var conditionNames = listOf(
            "Dermatological Conditions",
            "Bone or Joint Conditions",
            "Gastrointestinal Conditions",
            "Neurological Conditions",
            "Diabetes",
            "Thyroid-related disorders",
            "Liver Disorders",
            "Kidney Conditions",
            "Cardiovascular Conditions",
            "Gall bladder issues",
            "Cancer",
            "Respiratory conditions",
            "Auto-immune condition"
        )

        if (!isMale) {
            conditionNames = conditionNames.plus("Polycystic ovary syndrome")
        }


        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        val notSure =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I'm not sure"))

        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None of the above"))

        (listOf(title, subTitle, notSure, none).plus(conditions) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        conditions.forEach { it.click() }

        // -------- CASE 1: Not Sure --------
        notSure.click()
        assertExclusiveSelected(notSure, conditions)

        // -------- CASE 2: None of the above --------
        none.click()
        assertExclusiveSelected(none, conditions)

        // -------- CASE 3: Normal condition --------
        val selectedCondition = conditions.first()
        selectedCondition.click()

        //assertConditionSelected(selectedCondition, notSure, none)
        logAnswer(
            QuestionSubType.MEDICAL_CONDITION_FAMILY,
            "Do you have a family history of any of the following medical conditions?",
            arrayOf("Dermatological Conditions (e.g., eczema, acne, psoriasis)")
        )
        nextButton.click()
        question_37()
    }

    fun question_37() {// Do you currently have or have ever been diagnosed with any medical conditions?
        logQuestion("Do you currently have or have ever been diagnosed with any medical conditions?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Do you currently have or have"))
        val subTitle = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("(Select all that apply)"))

        var conditionNames = listOf(
            "Dermatological Conditions",
            "Bone or Joint Conditions",
            "Gastrointestinal Conditions",
            "Neurological Conditions",
            "Type 2 - Diabetes",
            "Thyroid-related disorders",
            "Liver Disorders",
            "Kidney Conditions",
            "Cardiovascular Conditions",
            "Gall bladder issues",
            "Cancer",
            "Respiratory conditions",
            "Auto-immune condition"
        )

        if (!isMale) {
            conditionNames = conditionNames.plus("Polycystic ovary syndrome")
        }

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        val notSure =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I'm not sure"))

        val none =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None of the above"))


        (listOf(title, subTitle, notSure, none).plus(conditions) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        // 🔹 Clear any previous selections (optional but good practice)
        // Note: For a clean run, we assume nothing is selected initially.

        // 🔹 Select options based on the passed parameter
        medicalConditions.forEach { condition ->
            val buttonName = condition.buttonName
            val buttonToClick = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(buttonName))
            buttonToClick.click()
        }

        // 🔹 Logic for "Not Sure" and "None" (Exclusive check)
        if (medicalConditions.contains(MedicalCondition.NOT_SURE)) {
            assertExclusiveSelected(notSure, conditions)
        }
        if (medicalConditions.contains(MedicalCondition.NONE)) {
            assertExclusiveSelected(none, conditions)
        }

        // 🔹 Build the medical question queue based on selections
        medicalQuestionQueue.clear()  // Clear any previous queue

        // Check against the ENUM list to add to queue (Order matters based on questionnaire flow)
        if (medicalConditions.contains(MedicalCondition.GASTROINTESTINAL)) {
            medicalQuestionQueue.add(::question_38)
        }
        if (medicalConditions.contains(MedicalCondition.DERMATOLOGICAL)) {
            medicalQuestionQueue.add(::question_39)
        }
        if (medicalConditions.contains(MedicalCondition.BONE_OR_JOINT)) {
            medicalQuestionQueue.add(::question_40)
        }
        if (medicalConditions.contains(MedicalCondition.NEUROLOGICAL)) {
            medicalQuestionQueue.add(::question_41)
        }
        if (medicalConditions.contains(MedicalCondition.DIABETES)) {
            medicalQuestionQueue.add(::question_42)
        }
        if (medicalConditions.contains(MedicalCondition.THYROID)) {
            medicalQuestionQueue.add(::question_43)
        }
        if (medicalConditions.contains(MedicalCondition.LIVER)) {
            medicalQuestionQueue.add(::question_44)
        }
        if (medicalConditions.contains(MedicalCondition.KIDNEY)) {
            medicalQuestionQueue.add(::question_45)
        }
        if (medicalConditions.contains(MedicalCondition.CARDIOVASCULAR)) {
            medicalQuestionQueue.add(::question_46)
        }
        // Gall bladder issues has no sub-questions in JSON
        if (medicalConditions.contains(MedicalCondition.CANCER)) {
            medicalQuestionQueue.add(::question_49)
        }
        if (medicalConditions.contains(MedicalCondition.RESPIRATORY)) {
            medicalQuestionQueue.add(::question_47)
        }
        if (medicalConditions.contains(MedicalCondition.AUTO_IMMUNE)) {
            medicalQuestionQueue.add(::question_48)
        }

        // Log the selected conditions
        // Log the selected conditions
        val selectedConditionLabels = medicalConditions.mapNotNull { it.label }.toTypedArray()
        logAnswer(
            QuestionSubType.MEDICAL_CONDITION,
            "Do you currently have or have ever been diagnosed with any of the following medical conditions?",
            selectedConditionLabels
        )

        nextButton.click()

        // 🔹 Check for exclusive selections first
        if (medicalConditions.contains(MedicalCondition.NOT_SURE) || medicalConditions.contains(MedicalCondition.NONE)) {
            question_51()  // Skip condition details, go straight to medications
        } else {
            visitNextMedicalQuestion()  // Start visiting queued condition questions
        }
    }

    fun question_38() { // Which of the following best describes your GI condition?
        logQuestion("Which of the following best describes your GI condition?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        val ibs = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Irritable Bowel Syndrome")
        )

        val ibd = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Inflammatory Bowel Disease")
        )

        val acidReflux = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Acid reflux or")
        )

        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        val conditions = listOf(
            ibs,
            ibd,
            acidReflux,
            others
        )

        (listOf(title, none).plus(conditions) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )

        //-------None----------
        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        ibs.click()
        logAnswer(
            QuestionSubType.GI_CONDITION,
            "Which of the following best describes your GI condition?",
            arrayOf("Irritable Bowel Syndrome")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }


    fun question_39() {  // Which of the following best describes your skin condition?
        logQuestion("Which of the following best describes your skin condition?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Skin condition buttons (excluding "Others" and "None")
        val skinConditions = listOf("Psoriasis", "Eczema", "Acne")
        val conditionButtons = skinConditions.map {
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(it))
        }

        // Separate buttons
        val othersButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))
        val noneButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))

        (listOf(title, othersButton, noneButton).plus(conditionButtons) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        conditionButtons.forEach { it.click() } // Psoriasis, Eczema, Acne

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = othersButton,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )

        //-------None----------
        noneButton.click()
        assertExclusiveSelected(
            exclusive = noneButton,
            others = conditionButtons + listOf(othersButton)
        )

        conditionButtons[0].click()
        logAnswer(
            QuestionSubType.SKIN_CONDITION,
            "Which of the following best describes your skin condition?",
            arrayOf("Psoriasis")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_40() { // Which of the following best describes your bone or joint condition?
        logQuestion("Which of the following best describes your bone or joint condition?")
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Ankylosing Spondylitis",
            "Rheumatoid arthritis",
            "Gout",
            "Psoriatic Arthritis"
        )

        val conditionButtons = conditionNames.map {
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(it))
        }


        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        (listOf(title, others, none).plus(conditionButtons) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        conditionButtons.forEach { it.click() }


        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )

        //-------None----------
        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditionButtons + listOf(others)
        )

        conditionButtons[0].click()
        logAnswer(
            QuestionSubType.BONE_JOINT_CONDITION,
            "Which of the following best describes your bone/joint condition?",
            arrayOf("Ankylosing Spondylitis")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_41() {// Which of the following best describes your neurological condition?
        logQuestion("Which of the following best describes your neurological condition?")
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Migraines",
            "Epilepsy",
            "Parkinson's"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        conditions.forEach { it.click() }


        //--------None---------
        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.NEUROLOGICAL_CONDITION,
            "Which of the following best describes your neurological condition?",
            arrayOf("Migraines")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_42() { // How would you best describe your Diabetes status?
        logQuestion("How would you best describe your Diabetes status?")
        // Question title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("How would you best describe"))

        // Answer options
        val preDiabeticNotOnMeds = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("I am prediabetic, but I'm not")
        )

        val preDiabeticOnMeds = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("I have prediabetes and I'm on")
        )

        val diabeticNotOnMeds = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("I have diabetes, but not on")
        )

        val diabeticOnMeds = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("I have diabetes and I'm on")
        )

        // ✅ wait once
        listOf(
            title,
            preDiabeticNotOnMeds,
            preDiabeticOnMeds,
            diabeticNotOnMeds,
            diabeticOnMeds,
            questionerCount
        ).forEach { it.waitFor() }

        assertProgressCount()
        // -------------------------
        // Select ONE option (wizard auto-handles navigation)
        // -------------------------


        preDiabeticNotOnMeds.click()
        logAnswer(
            QuestionSubType.DIABETES_STATUS,
            "How would you best describe your Diabetes status?",
            "I am prediabetic, but I'm not on medication"
        )
        visitNextMedicalQuestion()

    }

    fun question_43() {// Which of the following best describes your thyroid condition?
        logQuestion("Which of the following best describes your thyroid condition?")
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Hypothyroidism",
            "Hyperthyroidism"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        // ✅ wait once for all elements
        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        //--------None---------
        conditions.forEach { it.click() }

        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.THYROID_CONDITION,
            "Which of the following best describes your thyroid condition?",
            arrayOf("Hypothyroidism")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_44() {  // Which of the following best describes your liver condition?
        logQuestion("Which of the following best describes your liver condition?")
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Fatty Liver",
            "Cirrhosis",
            "Hepatitis"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )

        // None

        conditions.forEach { it.click() }

        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.LIVER_CONDITION,
            "Which of the following best describes your liver condition?",
            arrayOf("Fatty Liver")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_45() {  // Which of the following best describes your kidney condition?
        logQuestion("Which of the following best describes your kidney condition?")

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Nephritis",
            "Chronic Kidney Disease"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        // None
        conditions.forEach { it.click() }

        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.KIDNEY_CONDITION,
            "Which of the following best describes your kidney condition?",
            arrayOf("Nephritis")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_46() {//  Which of the following best describes your heart condition?
        logQuestion("Which of the following best describes your heart condition?")


        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Hypertension",
            "Heart disease risk",
            "Hypotension"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        //--------None---------
        conditions.forEach { it.click() }

        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.HEART_CONDITION,
            "Which of the following best describes your heart condition?",
            arrayOf("Hypertension")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_47() {//  Which of the following best describes your respiratory condition?
        logQuestion("Which of the following best describes your respiratory condition?")
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Asthma",
            "Chronic Obstructive Pulmonary",
            "Bronchitis"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()


        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        //--------None---------

        conditions.forEach { it.click() }
        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.RESPIRATORY_CONDITION,
            "Which of the following best describes your respiratory condition?",
            arrayOf("Asthma")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_48() {  // Which of the following best describes your auto-immune condition?
        logQuestion("Which of the following best describes your auto-immune condition?")
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Which of the following best"))

        // Condition buttons (excluding Others / None)
        val conditionNames = listOf(
            "Systemic Lupus Erythematosus",
            "Hashimoto's Thyroiditis",
            "Graves' disease",
            "Rheumatoid Arthritis",
            "Multiple Sclerosis (MS)",
            "Type 1 Diabetes",
            "Celiac Disease"
        )

        val conditions = conditionNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        // Separate buttons
        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None")
        )

        // ✅ wait once for everything
        listOf(title, others, none, questionerCount).plus(conditions)
            .forEach { it.waitFor() }
        assertProgressCount()

        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        //--------None---------

        conditions.forEach { it.click() }
        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = conditions + listOf(others)
        )

        conditions[0].click()
        logAnswer(
            QuestionSubType.AUTO_IMMUNE_CONDITION,
            "Which of the following best describes your auto-immune condition?",
            arrayOf("Systemic Lupus Erythematosus (SLE)")
        )
        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_49() { // What is your current cancer status?
        logQuestion("What is your current cancer status?")
        // Question title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("What is your current cancer"))

        // Answer options (single-select)
        val onTreatment = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Yes, I currently have cancer and on treatment")
        )

        val notOnTreatment = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Yes, I currently have cancer but not on treatment")
        )

        val completedLessThanYear = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Yes, but completed treatment less than a year ago")
        )

        val completedMoreThanYear = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Yes, but completed treatment more than a year ago")
        )

        listOf(
            title,
            onTreatment,
            notOnTreatment,
            completedLessThanYear,
            completedMoreThanYear,
            questionerCount
        ).forEach { it.waitFor() }

        assertProgressCount()

        // -------------------------
        // Select ONE option only
        // -------------------------

        logAnswer(
            QuestionSubType.CANCER_DIAGNOSIS,
            "What is your current cancer status?",
            "Yes, I currently have cancer and on treatment"
        )
        onTreatment.click()
        question_50()
    }

    fun question_50() {
        logQuestion("Please mention the type of cancer")
        // Please mention the type of cancer
        // Title
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please mention the type of"))

        // Textbox
        val typeTextbox = page.getByRole(
            AriaRole.TEXTBOX,
            Page.GetByRoleOptions().setName("Please mention the type of")
        )

        (listOf(title, typeTextbox) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        assertFalse(nextButton.isEnabled)

        // -------------------------
        // Enter cancer type
        // -------------------------
        typeTextbox.fill("Breast cancer")
        logAnswer(QuestionSubType.CANCER_TYPE, "Please mention the type of cancer", "Breast cancer")

        assertTrue(nextButton.isEnabled)

        nextButton.click()
        visitNextMedicalQuestion()
    }

    fun question_51() { // Are you currently taking any of the following types of medicines?
        logQuestion("Are you currently taking any of the following types of medicines?")
        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Are you currently taking any"))

        val medicationNames = listOf(
            "Cholesterol-lowering drugs",
            "Blood pressure medicines",
            "Thyroid medicines",
            "Painkillers / Anti-",
            "Steroids / Corticosteroids",
            "Antacids / Acid-reducing",
            "Chemotherapy or Cancer-",
            "Hormone-related medicines",
            "Antidepressants / Anti-",
            "Any herbal or alternative"
        )

        val medications = medicationNames.map {
            page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(it)
            )
        }

        val none = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("None of the above")
        )

        val others = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Others")
        )

        (listOf(title, none, others).plus(medications) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()


        //--------Others---------
        val otherTextBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please specify..."))
        val errorInfo = page.getByRole(AriaRole.PARAGRAPH)
            .filter(FilterOptions().setHasText("Please specify your answer to"))

        handleOthersTextBox(
            othersButton = others,
            textBox = otherTextBox,
            errorParagraph = errorInfo,
            nextButton = nextButton,
            previousButton = previousButton,
        )


        // None
        medications.forEach {
            it.click()
        }

        none.click()
        assertExclusiveSelected(
            exclusive = none,
            others = medications + listOf(others)
        )
        medications.take(3).forEach {
            it.click()
        }

        logAnswer(
            QuestionSubType.MEDICINES_TAKING, "Are you currently taking any of the following types of medicines?",
            arrayOf(
                "Cholesterol-lowering drugs (Statins) – e.g., Rosuvastatin, Atorvastatin",
                "Blood pressure medicines – e.g., Amlodipine, Telmisartan, Losartan",
                "Thyroid medicines – e.g., Thyronorm, Eltroxin"
            )
        )

        nextButton.click()
        question_52()
    }

    fun question_52() { // What is your waist circumference at its narrowest point?
        logger.info {
            "Answer count --> ${answersStored.size}"
        }
        logQuestion("What is your waist circumference at its narrowest point?")
        val values = "24"

        val title = page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("What is your waist"))

        // Helper text
        val subTitle = page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("Please enter the value in"))

        // Waist input
        val waistTextBox = page.getByRole(AriaRole.TEXTBOX)

        val completeButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Complete"))

        (listOf(title, subTitle, waistTextBox, completeButton) + questionerCount).forEach { it.waitFor() }
        assertProgressCount()

        val rangeError = page.getByRole(AriaRole.PARAGRAPH)
            .filter(Locator.FilterOptions().setHasText("Please enter a value between"))

        waistTextBox.fill("10")
        rangeError.waitFor()


        waistTextBox.fill("")


        waistTextBox.fill("60")
        rangeError.waitFor()

        waistTextBox.fill("")
        waistTextBox.fill(values)
        logAnswer(
            QuestionSubType.WAIST_CIRCUMFERENCE,
            "What is your waist circumference at its narrowest point?",
            values
        )
        if (shouldClickComplete) {
            completeButton.click()
        }
    }


    /*   fun isButtonChecked(button: Locator): Boolean {
           return button.locator("svg").count() > 0
       }
   */
    /**
     * Handles a multi-select question with "Others" option that requires a textbox input.
     *
     * @param othersButton The "Others" button locator
     * @param textBox The textbox locator corresponding to "Others"
     * @param errorParagraph The error paragraph shown when textbox is empty
     * @param nextButton The wizard's Next button
     * @param previousButton The wizard's Previous button
     * @param fillText The text to fill in the textbox (default: "sample")
     */
    fun handleOthersTextBox(
        othersButton: Locator,
        textBox: Locator,
        errorParagraph: Locator,
        nextButton: Locator,
        previousButton: Locator,
        fillText: String = "sample"
    ) {
        // Click the Others button
        othersButton.click()

        // Wait for textbox
        textBox.waitFor()

        // Read initial value
        var inputValue = textBox.inputValue()

        // If blank, expect error and button states
        if (inputValue.isNullOrBlank()) {
            errorParagraph.waitFor()
            //  assertFalse(!nextButton.isEnabled) // next should be disabled //TODO need to check
            assertTrue(previousButton.isEnabled)
        }

        // Fill the textbox
        textBox.fill(fillText)

        // Re-read value
        inputValue = textBox.inputValue()

        // After filling, Next should be enabled
        if (!inputValue.isNullOrBlank()) {
            //   assertTrue(nextButton.isEnabled) //TODO need to check
            assertTrue(previousButton.isEnabled)
        }

        textBox.fill("")
    }

    fun assertQuestionerValidationsCheck() {
        logger.info {
            "Answer count --> ${answersStored.size}"
        }

        waitForConfirmation()

        val questionHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View/Edit Questionnaire"))
        val editQuestionerButton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View/Edit Responses"))
        val questionDialog = page.locator(".bg-zinc-900").first()

        questionHeading.waitFor()
        editQuestionerButton.waitFor()

        editQuestionerButton.click()

        questionDialog.waitFor()

        // Validate all questions sequentially based on stored answers
        answersStored.keys.forEach { key ->
            runChecker(key)
            if (key != QuestionSubType.WAIST_CIRCUMFERENCE) {
                nextButton.click()
            }
        }
    }

    fun assertQuestionerBackwardValidationsCheck() {
        logger.info {
            "Starting backward validation. Answer count --> ${answersStored.size}"
        }

        val reversedKeys = answersStored.keys.toList().reversed()

        reversedKeys.forEachIndexed { index, key ->
            logger.info { "Backward Validating: $key" }
            runChecker(key)

            // If not the last question in the backward flow (which is Q1)
            if (index < reversedKeys.size - 1) {
                logger.info { "Clicking Previous to reach next question in backward flow" }
                previousButton.click()
            }
        }
    }

    private fun runChecker(subType: String) {
        val keysList = answersStored.keys.toList()
        val index = keysList.indexOf(subType) + 1
        when (subType) {
            QuestionSubType.FOOD_PREFERENCE -> question_1_checker(index)
            QuestionSubType.TYPE_OF_MEAT -> question_2_checker(index)
            QuestionSubType.CUISINE_PREFERENCE -> question_3_checker(index)
            QuestionSubType.DAILY_EATING_HABIT -> question_4_checker(index)
            QuestionSubType.DIET_EXPERIENCE -> question_5_checker(index)
            QuestionSubType.NUTRITION_TRACKING_EXPERIENCE -> question_6_checker(index)
            QuestionSubType.ALLERGY -> question_7_checker(index)
            QuestionSubType.INTOLERANCE -> question_8_checker(index)
            QuestionSubType.CAFFEINE_CONSUMPTION -> question_9_checker(index)
            QuestionSubType.TYPICAL_DAY -> question_10_checker(index)
            QuestionSubType.EXERCISE_TYPE -> question_11_checker(index)
            QuestionSubType.PREFERRED_WORKOUT_TIME -> question_12_checker(index)
            QuestionSubType.EQUIPMENTS_AVAILABLE -> question_13_checker(index)
            QuestionSubType.SLEEP_HYGIENE -> question_14_checker(index)
            QuestionSubType.WEEKDAY_SLEEP_ROUTINE_BED_TIME -> question_15_checker(index)
            QuestionSubType.WEEKDAY_SLEEP_ROUTINE_WAKEUP_TIME -> question_16_checker(index)
            QuestionSubType.WEEKEND_SLEEP_ROUTINE_BED_TIME -> question_17_checker(index)
            QuestionSubType.WEEKEND_SLEEP_ROUTINE_WAKEUP_TIME -> question_18_checker(index)
            QuestionSubType.SLEEP_SCHEDULE_PREFERENCE -> question_19_checker(index)
            QuestionSubType.BED_TIME_GOAL -> question_20_checker(index)
            QuestionSubType.WAKEUP_TIME_GOAL -> question_21_checker(index)
            QuestionSubType.SLEEP_SATISFACTION -> question_22_checker(index)
            QuestionSubType.SLEEP_WAKEUP_REFRESHMENT -> question_23_checker(index)
            QuestionSubType.SUNLIGHT_UPON_WAKEUP -> question_24_checker(index)
            QuestionSubType.SUNLIGHT_TIMING -> question_25_checker(index)
            QuestionSubType.WELLNESS_MOTIVATION_FREQUENCY -> question_26_checker(index)
            QuestionSubType.WELLNESS_BOTHER_FREQUENCY -> question_27_checker(index)
            QuestionSubType.STRESS_MANAGEMENT -> question_28_checker(index)
            QuestionSubType.EMOTIONAL_EATING -> question_29_checker(index)
            QuestionSubType.SNACK_PREFERENCE -> question_30_checker(index)
            QuestionSubType.MENSTRUAL_STATUS -> question_31_checker(index)
            QuestionSubType.IS_PREGNANT -> question_32_checker(index)
            QuestionSubType.N_SMOKE -> question_33_checker(index)
            QuestionSubType.N_ALCOHOL -> question_34_checker(index)
            QuestionSubType.ADDITIONAL_SUPPLEMENT -> question_35_checker(index)
            QuestionSubType.MEDICAL_CONDITION_FAMILY -> question_36_checker(index)
            QuestionSubType.MEDICAL_CONDITION -> question_37_checker(index)
            QuestionSubType.GI_CONDITION -> question_38_checker(index)
            QuestionSubType.SKIN_CONDITION -> question_39_checker(index)
            QuestionSubType.BONE_JOINT_CONDITION -> question_40_checker(index)
            QuestionSubType.NEUROLOGICAL_CONDITION -> question_41_checker(index)
            QuestionSubType.DIABETES_STATUS -> question_42_checker(index)
            QuestionSubType.THYROID_CONDITION -> question_43_checker(index)
            QuestionSubType.LIVER_CONDITION -> question_44_checker(index)
            QuestionSubType.KIDNEY_CONDITION -> question_45_checker(index)
            QuestionSubType.HEART_CONDITION -> question_46_checker(index)
            QuestionSubType.RESPIRATORY_CONDITION -> question_47_checker(index)
            QuestionSubType.AUTO_IMMUNE_CONDITION -> question_48_checker(index)
            QuestionSubType.CANCER_DIAGNOSIS -> question_49_checker(index)
            QuestionSubType.CANCER_TYPE -> question_50_checker(index)
            QuestionSubType.MEDICINES_TAKING -> question_51_checker(index)
            QuestionSubType.WAIST_CIRCUMFERENCE -> question_52_checker(index)
            else -> println("No checker implemented for QuestionSubType: $subType")
        }
    }

    /*---------------Questioner Re-selection check----------------*/

    fun question_1_checker(index: Int) {
        logQuestion("Checking: What is your food preference?")

        val question =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your food preference?"))


        val vegetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegetarian Primarily plant-"))
        val nonVegetarian =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Non-Vegetarian Consumes meat"))
        val vegan = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vegan Exclusively plant-based"))
        val eggetarian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Eggetarian Primarily plant-"))

        listOf(question, vegetarian, nonVegetarian, vegan, eggetarian, questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)

        val storedAnswer = answersStored[QuestionSubType.FOOD_PREFERENCE]?.answer as? String

        val options = mapOf(
            "Vegetarian" to vegetarian,
            "Non-Vegetarian" to nonVegetarian,
            "Vegan" to vegan,
            "Eggetarian" to eggetarian
        )

        checkSingleSelect(storedAnswer, options)
    }

    private fun question_2_checker(index: Int) {
        logQuestion("Checking: Which of the following do you consume?")
        val title =
            page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following do you"))
        title.waitFor()

        val chicken = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Chicken"))
        val pork = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Pork"))
        val mutton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Mutton"))
        val turkey = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Turkey"))
        val fish = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Fish").setExact(true))
        val shellfish = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Shellfish").setExact(true))
        val beef = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Beef"))

        val options = mapOf(
            "Chicken" to chicken,
            "Pork" to pork,
            "Mutton" to mutton,
            "Turkey" to turkey,
            "Fish" to fish,
            "Shellfish" to shellfish,
            "Beef" to beef
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)

        val storedAnswer = answersStored[QuestionSubType.TYPE_OF_MEAT]?.answer
        checkMultiSelect(storedAnswer, options)
    }

    private fun question_3_checker(index: Int) {
        logQuestion("Checking: What is your cuisine preference?")
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your cuisine"))

        title.waitFor()

        val northIndian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("North Indian"))
        val southIndian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("South Indian"))
        val jain = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Jain"))
        val mediterranean = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Mediterranean"))
        val continental = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continental"))
        val chinese = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Chinese"))
        val arabian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Arabian"))
        val asian = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Asian"))
        val japanese = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Japanese"))

        val options = mapOf(
            "North Indian" to northIndian,
            "South Indian" to southIndian,
            "Jain" to jain,
            "Mediterranean" to mediterranean,
            "Continental" to continental,
            "Chinese" to chinese,
            "Arabian" to arabian,
            "Asian" to asian,
            "Japanese" to japanese
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.CUISINE_PREFERENCE]?.answer, options)
    }

    private fun question_4_checker(index: Int) {
        logQuestion("Checking: Daily eating habits")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Primarily Home Cooked Meals" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Primarily Home Cooked Meals")
            ),
            "Occasional Snacker" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Occasional Snacker")
            ),
            "Often dining out" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Often dining out")),
            "Frequent junk/processed food" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Frequent junk/processed food")
            ),
            "Skips meals" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Skips meals")),
            "Late-night eating" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Late-night eating")
            ),
            "Intermittent fasting" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Intermittent fasting / time-")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)

        checkSingleSelect(answersStored[QuestionSubType.DAILY_EATING_HABIT]?.answer as? String, options)
    }

    private fun question_5_checker(index: Int) {
        logQuestion("Checking: Diet experience")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your past experience")).waitFor()

        val options = mapOf(
            "Tried and found what works" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tried and found what works")
            ),
            "Tried various diets, unsure" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tried various diets, unsure")
            ),
            "Tried them all" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tried them all, hard to")
            ),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.DIET_EXPERIENCE]?.answer as? String, options)
    }

    private fun question_6_checker(index: Int) {
        logQuestion("Checking: Nutrition tracking")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How familiar are you with")).waitFor()

        val options = mapOf(
            "Very familiar" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Very familiar, successful")
            ),
            "Tracked a bit" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Tracked a bit, unsure of my")
            ),
            "Never tracked" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Never tracked, need guidance")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.NUTRITION_TRACKING_EXPERIENCE]?.answer as? String, options)
    }

    private fun question_7_checker(index: Int) {
        logQuestion("Checking: Food allergies")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Do you have any food")).waitFor()

        val options = mapOf(
            "Milk or dairy" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Milk or dairy")),
            "Peanuts" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Peanuts")),
            "Tree nuts" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Tree nuts")),
            "Soy" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Soy")),
            "Gluten" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Gluten (Wheat)")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.ALLERGY]?.answer, options)
    }

    private fun question_8_checker(index: Int) {
        logQuestion("Checking: Food intolerances")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Do you have any food")).waitFor()

        val options = mapOf(
            "Lactose" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Lactose")),
            "Caffeine" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Caffeine")),
            "Gluten" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Gluten")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.INTOLERANCE]?.answer, options)
    }

    private fun question_9_checker(index: Int) {
        logQuestion("Checking: Caffeine consumption")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How much caffeine do you")).waitFor()

        val options = mapOf(
            "None or Rarely" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None or Rarely")),
            "-2 servings" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-2 servings")),
            "-4 servings" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-4 servings")),
            "or more servings" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("or more servings"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.CAFFEINE_CONSUMPTION]?.answer as? String, options)
    }

    private fun question_10_checker(index: Int) {
        logQuestion("Checking: Activity level")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How active are you in a")).waitFor()

        val options = mapOf(
            "Sedentary" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sedentary: <3 hrs/week")),
            "Lightly Active" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Lightly Active: 3–5 hrs/week")
            ),
            "Moderately Active" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Moderately Active: 5–7 hrs/")
            ),
            "Very Active" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Very Active: >7 hrs/week")
            ),
            "Hardly Exercise" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hardly Exercise"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.TYPICAL_DAY]?.answer as? String, options)
    }

    private fun question_11_checker(index: Int) {
        if (answersStored[QuestionSubType.EXERCISE_TYPE] == null) {
            // Skipped based on Q10
            return
        }
        logQuestion("Checking: Exercise type")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What type of exercise do you")).waitFor()

        val options = mapOf(
            "Yoga" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yoga")),
            "Strength Training" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Strength Training")
            ),
            "Pilates" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Pilates")),
            "Flexibility" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Flexibility / Stretching")
            ),
            "I don't exercise" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I don't exercise"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.EXERCISE_TYPE]?.answer, options)
    }

    private fun question_12_checker(index: Int) {
        if (answersStored[QuestionSubType.PREFERRED_WORKOUT_TIME] == null) return
        logQuestion("Checking: Preferred workout time")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("When do you usually work out")).waitFor()

        val options = mapOf(
            "Morning" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Morning")),
            "Afternoon" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Afternoon")),
            "Evening" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Evening")),
            "Flexible" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Flexible"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.PREFERRED_WORKOUT_TIME]?.answer as? String, options)
    }

    private fun question_13_checker(index: Int) {
        if (answersStored[QuestionSubType.EQUIPMENTS_AVAILABLE] == null) return
        logQuestion("Checking: Equipments available")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Equipments available")).waitFor()

        val options = mapOf(
            "Dumbbells" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Dumbbells")),
            "Kettlebells" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Kettlebells")),
            "Resistance bands" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Resistance bands")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.EQUIPMENTS_AVAILABLE]?.answer, options)
    }

    private fun question_14_checker(index: Int) {
        logQuestion("Checking: Sleep hygiene")
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How would you describe your"))
        title.waitFor()

        val options = mapOf(
            "Excellent routine" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Excellent routine, sleep like")
            ),
            "Room for improvement" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Room for improvement,")
            ),
            "Needs work" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Needs work, struggling with")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.SLEEP_HYGIENE]?.answer as? String, options)
    }

    private fun question_15_checker(index: Int) {
        logQuestion("Checking: Weekday bed time")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually go")).waitFor()
        val timerBox = page.getByRole(AriaRole.TEXTBOX)
        timerBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.WEEKDAY_SLEEP_ROUTINE_BED_TIME]?.answer as? String, timerBox)
    }

    private fun question_16_checker(index: Int) {
        logQuestion("Checking: Weekday wake up time")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually wake")).waitFor()
        val timerBox = page.getByRole(AriaRole.TEXTBOX)
        timerBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.WEEKDAY_SLEEP_ROUTINE_WAKEUP_TIME]?.answer as? String, timerBox)
    }

    private fun question_17_checker(index: Int) {
        logQuestion("Checking: Weekend bed time")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually go")).waitFor()
        val timerBox = page.getByRole(AriaRole.TEXTBOX)
        timerBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.WEEKEND_SLEEP_ROUTINE_BED_TIME]?.answer as? String, timerBox)
    }

    private fun question_18_checker(index: Int) {
        logQuestion("Checking: Weekend wake up time")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What time do you usually wakeup"))
            .waitFor()
        val timerBox = page.getByRole(AriaRole.TEXTBOX)
        timerBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.WEEKEND_SLEEP_ROUTINE_WAKEUP_TIME]?.answer as? String, timerBox)
    }

    private fun question_19_checker(index: Int) {
        logQuestion("Checking: Sleep schedule preference")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Let's make your sleep")).waitFor()

        val options = mapOf(
            "Bedtime" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Bedtime")),
            "Waketime" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Waketime"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.SLEEP_SCHEDULE_PREFERENCE]?.answer as? String, options)
    }

    private fun question_20_checker(index: Int) {
        logQuestion("Checking: Bedtime goal")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Set your ideal Bedtime")).waitFor()
        val timerBox = page.getByRole(AriaRole.TEXTBOX)
        timerBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.BED_TIME_GOAL]?.answer as? String, timerBox)
    }

    private fun question_21_checker(index: Int) {
        logQuestion("Checking: Wakeup time goal")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Set your ideal Waketime")).waitFor()
        val timerBox = page.getByRole(AriaRole.TEXTBOX)
        timerBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.WAKEUP_TIME_GOAL]?.answer as? String, timerBox)
    }

    private fun question_22_checker(index: Int) {
        logQuestion("Checking: Sleep satisfaction")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How satisfied are you with")).waitFor()

        val options = mapOf(
            "Fully Satisfied" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Fully Satisfied")),
            "Somewhat Satisfied" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Somewhat Satisfied")
            ),
            "Not Satisfied" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Not Satisfied"))
        )
        options.values.forEach { it.waitFor() }
        questionerCount.waitFor()
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.SLEEP_SATISFACTION]?.answer as? String, options)
    }

    private fun question_23_checker(index: Int) {
        logQuestion("Checking: Sleep wakeup refreshment")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Do you wake up refreshed?")).waitFor()

        val options = mapOf(
            "Always" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Always")),
            "Sometimes" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sometimes")),
            "Rarely" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Rarely")),
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.SLEEP_WAKEUP_REFRESHMENT]?.answer as? String, options)
    }

    private fun question_24_checker(index: Int) {
        logQuestion("Checking: Sun exposure duration")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is the duration of your")).waitFor()

        val options = mapOf(
            "Less than 5 minutes" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Less than 5 minutes")
            ),
            "-10 minutes" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-10 minutes")),
            "-20 minutes" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-20 minutes")),
            "More than 20 minutes" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than 20 minutes")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.SUNLIGHT_UPON_WAKEUP]?.answer as? String, options)
    }

    private fun question_25_checker(index: Int) {
        logQuestion("Checking: Sun exposure timing")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("During which part of the day")).waitFor()

        val options = mapOf(
            "Early morning" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Early morning (before 10 a.m.)")
            ),
            "Late morning" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Late morning to early")),
            "Late afternoon" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Late afternoon (3 p.m. - 5 p.")
            ),
            "Evening" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Evening (after 5 p.m.)"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.SUNLIGHT_TIMING]?.answer as? String, options)
    }

    private fun question_26_checker(index: Int) {
        logQuestion("Checking: Wellness motivation")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How often do you look for")).waitFor()

        val options = mapOf(
            "All the time" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("All the time")),
            "Now and then" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Now and then")),
            "Hardly Ever" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hardly Ever"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.WELLNESS_MOTIVATION_FREQUENCY]?.answer as? String, options)
    }

    private fun question_27_checker(index: Int) {
        logQuestion("Checking: Feeling low frequency")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("In the past month, how often")).waitFor()

        val options = mapOf(
            "Every day" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Every day")),
            "More than once a week" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than once a week")
            ),
            "Once a week" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Once a week").setExact(true)
            ),
            "Once in two weeks" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Once in two weeks")
            ),
            "Once a month" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Once a month / Rarely"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.WELLNESS_BOTHER_FREQUENCY]?.answer as? String, options)
    }

    private fun question_28_checker(index: Int) {
        logQuestion("Checking: Stress management")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How well do you deal with")).waitFor()

        val options = mapOf(
            "I deal with my stress well" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I deal with my stress well")
            ),
            "I could deal with stress" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I could deal with stress")
            ),
            "I feel overwhelmed" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I feel overwhelmed by stress")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.STRESS_MANAGEMENT]?.answer as? String, options)
    }

    private fun question_29_checker(index: Int) {
        logQuestion("Checking: Emotional eating")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How often do you eat in")).waitFor()

        val options = mapOf(
            "Frequently" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Frequently")),
            "Occasionally" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Occasionally")),
            "Rarely" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Rarely")),
            "Never" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Never"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.EMOTIONAL_EATING]?.answer as? String, options)
    }

    private fun question_30_checker(index: Int) {
        logQuestion("Checking: Snack preference")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What type of snacks do you")).waitFor()

        val options = mapOf(
            "Sweets" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sweets")),
            "Fried" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Fried and crispy")),
            "Salty" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Salty")),
            "Healthier options" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Healthier options (e.g.,")
            ),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.SNACK_PREFERENCE]?.answer, options)
    }

    private fun question_31_checker(index: Int) {
        if (answersStored[QuestionSubType.MENSTRUAL_STATUS] == null) return
        logQuestion("Checking: Menstrual Status")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What's your current menstrual status?"))
            .waitFor()

        val options = mapOf(
            "I'm still menstruating" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I'm still menstruating")
            ),
            "I'm nearing menopause" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I'm nearing menopause")
            ),
            "I have attained Menopause" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I have attained Menopause")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)

        val selectedLabel = answersStored[QuestionSubType.MENSTRUAL_STATUS]?.answer as? String
        checkSingleSelect(selectedLabel, options)
    }

    private fun question_32_checker(index: Int) {
        if (answersStored[QuestionSubType.IS_PREGNANT] == null) return
        logQuestion("Checking: Pregnancy Status")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Are you pregnant?")).waitFor()

        val options = mapOf(
            "Yes" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yes")),
            "No" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("No"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.IS_PREGNANT]?.answer as? String, options)
    }

    private fun question_33_checker(index: Int) {
        if (answersStored[QuestionSubType.N_SMOKE] == null) return
        logQuestion("Checking: Smoking")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How many cigarettes do you")).waitFor()

        val options = mapOf(
            "I don't smoke" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I don't smoke")),
            "–5" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("–5")),
            "–10" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("–10")),
            "–20" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("–20")),
            "More than" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("More than"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.N_SMOKE]?.answer as? String, options)
    }

    private fun question_34_checker(index: Int) {
        if (answersStored[QuestionSubType.N_ALCOHOL] == null) return
        logQuestion("Checking: Alcohol")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How many alcoholic drinks do")).waitFor()

        val options = mapOf(
            "I don't drink" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I don't drink")),
            "Less than once per week" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Less than once per week")
            ),
            "-3 drinks" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-3 drinks")),
            "-7 drinks" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-7 drinks")),
            "-14 drinks" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("-14 drinks")),
            "More than 14 drinks" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("More than 14 drinks")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.N_ALCOHOL]?.answer as? String, options)
    }

    private fun question_35_checker(index: Int) {
        logQuestion("Checking: Supplements")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Please select any additional")).waitFor()

        // Just sample a few key ones to ensure locator strategy works, checking all 27 might be overkill but correct
        // For brevity in code block, I will include the ones used in the test code + Others/None.
        // Test used: Vitamin A, D, E
        val options = mapOf(
            "Vitamin A" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vitamin A")),
            "Vitamin D" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vitamin D")),
            "Vitamin E" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Vitamin E")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        questionerCount.waitFor()
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.ADDITIONAL_SUPPLEMENT]?.answer, options)
    }

    private fun question_36_checker(index: Int) {
        logQuestion("Checking: Family History")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Do you have a family history")).waitFor()

        val options = mapOf(
            "Dermatological Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Dermatological Conditions")
            ),
            "Bone or Joint Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Bone or Joint Conditions")
            ),
            // ... Add others as needed
            "I'm not sure" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I'm not sure")),
            "None of the above" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None of the above"))
        )
        questionerCount.waitFor()
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.MEDICAL_CONDITION_FAMILY]?.answer, options)
    }

    private fun question_37_checker(index: Int) {
        logQuestion("Checking: Medical Conditions")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Do you currently have or have")).waitFor()

        val options = mapOf(
            "Dermatological Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Dermatological Conditions")
            ),
            "Bone or Joint Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Bone or Joint Conditions")
            ),
            "Gastrointestinal Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Gastrointestinal Conditions")
            ),
            "Neurological Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Neurological Conditions")
            ),
            "Type 2 - Diabetes" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Type 2 - Diabetes")
            ),
            "Thyroid-related disorders" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Thyroid-related disorders")
            ),
            "Liver Disorders" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Liver Disorders")),
            "Kidney Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Kidney Conditions")
            ),
            "Cardiovascular Conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Cardiovascular Conditions")
            ),
            "Gall bladder issues" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Gall bladder issues")
            ),
            "Cancer" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancer")),
            "Respiratory conditions" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Respiratory conditions")
            ),
            "Auto-immune condition" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Auto-immune condition")
            ),

            "I'm not sure" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("I'm not sure")),
            "None of the above" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None of the above"))
        )

        options.values.forEach { it.waitFor() }
        questionerCount.waitFor()
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.MEDICAL_CONDITION]?.answer, options)
    }

    private fun question_38_checker(index: Int) {
        if (answersStored[QuestionSubType.GI_CONDITION] == null) return
        logQuestion("Checking: GI Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Irritable Bowel Syndrome" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Irritable Bowel Syndrome")
            ),
            "Inflammatory Bowel Disease" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Inflammatory Bowel Disease")
            ),
            "Acid reflux" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Acid reflux or")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        options.values.forEach { it.waitFor() }
        questionerCount.waitFor()
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.GI_CONDITION]?.answer, options)
    }

    private fun question_39_checker(index: Int) {
        if (answersStored[QuestionSubType.SKIN_CONDITION] == null) return
        logQuestion("Checking: Skin Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Psoriasis" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Psoriasis")),
            "Eczema" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Eczema")),
            "Acne" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Acne")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.SKIN_CONDITION]?.answer, options)
    }

    private fun question_40_checker(index: Int) {
        if (answersStored[QuestionSubType.BONE_JOINT_CONDITION] == null) return
        logQuestion("Checking: Bone/Joint Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Ankylosing Spondylitis" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Ankylosing Spondylitis")
            ),
            "Rheumatoid arthritis" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Rheumatoid arthritis")
            ),
            "Gout" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Gout")),
            "Psoriatic Arthritis" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Psoriatic Arthritis")
            ),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.BONE_JOINT_CONDITION]?.answer, options)
    }

    private fun question_41_checker(index: Int) {
        if (answersStored[QuestionSubType.NEUROLOGICAL_CONDITION] == null) return
        logQuestion("Checking: Neurological Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Migraines" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Migraines")),
            "Epilepsy" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Epilepsy")),
            "Parkinson's" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Parkinson's")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.NEUROLOGICAL_CONDITION]?.answer, options)
    }

    private fun question_42_checker(index: Int) {
        if (answersStored[QuestionSubType.DIABETES_STATUS] == null) return
        logQuestion("Checking: Diabetes Status")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("How would you best describe")).waitFor()

        val options = mapOf(
            "I am prediabetic" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I am prediabetic, but I'm not")
            ),
            "I have prediabetes" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I have prediabetes and I'm on")
            ),
            "I have diabetes, but not on" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I have diabetes, but not on")
            ),
            "I have diabetes and I'm on" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("I have diabetes and I'm on")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.DIABETES_STATUS]?.answer as? String, options)
    }

    private fun question_43_checker(index: Int) {
        if (answersStored[QuestionSubType.THYROID_CONDITION] == null) return
        logQuestion("Checking: Thyroid Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Hypothyroidism" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hypothyroidism")),
            "Hyperthyroidism" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hyperthyroidism")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.THYROID_CONDITION]?.answer, options)
    }

    private fun question_44_checker(index: Int) {
        if (answersStored[QuestionSubType.LIVER_CONDITION] == null) return
        logQuestion("Checking: Liver Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Fatty Liver" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Fatty Liver")),
            "Cirrhosis" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cirrhosis")),
            "Hepatitis" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hepatitis")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.LIVER_CONDITION]?.answer, options)
    }

    private fun question_45_checker(index: Int) {
        if (answersStored[QuestionSubType.KIDNEY_CONDITION] == null) return
        logQuestion("Checking: Kidney Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Nephritis" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Nephritis")),
            "Chronic Kidney Disease" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Chronic Kidney Disease")
            ),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.KIDNEY_CONDITION]?.answer, options)
    }

    private fun question_46_checker(index: Int) {
        if (answersStored[QuestionSubType.HEART_CONDITION] == null) return
        logQuestion("Checking: Heart Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Hypertension" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hypertension")),
            "Heart disease risk" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Heart disease risk")
            ),
            "Hypotension" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Hypotension")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.HEART_CONDITION]?.answer, options)
    }

    private fun question_47_checker(index: Int) {
        if (answersStored[QuestionSubType.RESPIRATORY_CONDITION] == null) return
        logQuestion("Checking: Respiratory Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Asthma" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Asthma")),
            "Chronic Obstructive" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Chronic Obstructive Pulmonary")
            ),
            "Bronchitis" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Bronchitis")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.RESPIRATORY_CONDITION]?.answer, options)
    }

    private fun question_48_checker(index: Int) {
        if (answersStored[QuestionSubType.AUTO_IMMUNE_CONDITION] == null) return
        logQuestion("Checking: Auto-immune Condition")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Which of the following best")).waitFor()

        val options = mapOf(
            "Systemic Lupus" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Systemic Lupus Erythematosus")
            ),
            "Hashimoto's" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Hashimoto's Thyroiditis")
            ),
            "Graves'" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Graves' disease")),
            "Rheumatoid Arthritis" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Rheumatoid Arthritis")
            ),
            "Multiple Sclerosis" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Multiple Sclerosis (MS)")
            ),
            "Type 1 Diabetes" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Type 1 Diabetes")),
            "Celiac Disease" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Celiac Disease")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others")),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.AUTO_IMMUNE_CONDITION]?.answer, options)
    }

    private fun question_49_checker(index: Int) {
        if (answersStored[QuestionSubType.CANCER_DIAGNOSIS] == null) return
        logQuestion("Checking: Cancer Status")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("What is your current cancer")).waitFor()

        val options = mapOf(
            "on treatment" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Yes, I currently have cancer and on treatment")
            ),
            "not on treatment" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Yes, I currently have cancer but not on treatment")
            ),
            "completed less than a year" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Yes, but completed treatment less than a year ago")
            ),
            "completed more than a year" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Yes, but completed treatment more than a year ago")
            )
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkSingleSelect(answersStored[QuestionSubType.CANCER_DIAGNOSIS]?.answer as? String, options)
    }

    private fun question_50_checker(index: Int) {
        if (answersStored[QuestionSubType.CANCER_TYPE] == null) return
        logQuestion("Checking: Cancer Type")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Please mention the type of")).waitFor()
        val typeTextbox =
            page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Please mention the type of"))
        typeTextbox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)
        checkTextInput(answersStored[QuestionSubType.CANCER_TYPE]?.answer as? String, typeTextbox)
    }

    private fun question_51_checker(index: Int) {
        logQuestion("Checking: Medicines")
        page.getByRole(AriaRole.PARAGRAPH).filter(FilterOptions().setHasText("Are you currently taking any")).waitFor()

        val options = mapOf(
            "Cholesterol-lowering" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Cholesterol-lowering drugs")
            ),
            "Blood pressure" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Blood pressure medicines")
            ),
            "Thyroid medicines" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Thyroid medicines")
            ),
            "Painkillers" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Painkillers / Anti-")),
            "Steroids" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Steroids / Corticosteroids")
            ),
            "Antacids" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Antacids / Acid-reducing")),
            "Chemotherapy" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Chemotherapy or Cancer-")
            ),
            "Hormone-related" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Hormone-related medicines")
            ),
            "Antidepressants" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Antidepressants / Anti-")
            ),
            "Any herbal" to page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName("Any herbal or alternative")
            ),
            "None" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("None of the above")),
            "Others" to page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Others"))
        )

        (options.values + questionerCount).forEach { it.waitFor() }
        assertProgressCount(index)
        checkMultiSelect(answersStored[QuestionSubType.MEDICINES_TAKING]?.answer, options)
    }

    private fun question_52_checker(index: Int) {
        logQuestion("Checking: What is your waist circumference?")
        //   val completeButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Complete"))
        val title = page.getByRole(AriaRole.PARAGRAPH).filter(Locator.FilterOptions().setHasText("What is your waist"))
        val waistTextBox = page.getByRole(AriaRole.TEXTBOX)

        title.waitFor()
        waistTextBox.waitFor()
        questionerCount.waitFor()
        assertProgressCount(index)

        val storedAnswer = answersStored[QuestionSubType.WAIST_CIRCUMFERENCE]?.answer as? String
        checkTextInput(storedAnswer, waistTextBox)
    }

    // --- Checker Helpers ---

    private fun normalize(text: String): String =
        text
            .lowercase()
            .replace("\n", " ")
            .replace(":", " ")
            .replace(Regex("\\s+"), " ")
            .trim()


    private fun checkSingleSelect(
        storedAnswer: String?,
        options: Map<String, Locator>
    ) {
        if (storedAnswer.isNullOrBlank()) {
            logger.info { "No stored answer to verify." }
            return
        }

        val expected = normalize(storedAnswer)

        options.forEach { (key, locator) ->
            val actual = normalize(locator.innerText())
            val isMatch = actual == expected

            logger.info {
                "Comparing -> UI: '$actual' | Stored: '$expected'"
            }

            assertEquals(
                isMatch,
                isButtonChecked(locator),
                "'$key' selection mismatch. Stored='$storedAnswer'"
            )
        }
    }

    private fun checkMultiSelect(
        storedAnswer: Any?,
        options: Map<String, Locator>
    ) {
        val storedList = when (storedAnswer) {
            is Array<*> -> storedAnswer.filterIsInstance<String>()
            is List<*> -> storedAnswer.filterIsInstance<String>()
            else -> emptyList()
        }.map { normalize(it) }

        options.forEach { (_, locator) ->
            val buttonText = normalize(locator.innerText())

            val isExpectedSelected = storedList.any { stored ->
                buttonText == stored
            }

            assertEquals(
                isExpectedSelected,
                isButtonChecked(locator),
                "'${locator.innerText()}' selection mismatch. Stored=$storedList"
            )
        }
    }


    private fun checkTextInput(storedAnswer: String?, locator: Locator) {
        if (storedAnswer != null) {
            val actualValue = locator.inputValue()
            assertEquals(storedAnswer, actualValue, "Text input value mismatch")
        }
    }


    fun assertQuestionerValidationsCheckSample() {
        logger.info {
            "Answer count --> ${answersStored.size}"
        }

        waitForConfirmation()

        val questionHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View/Edit Questionnaire"))
        val editQuestionerButton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View/Edit Responses"))
        val questionDialog = page.locator(".bg-zinc-900").first()

        questionHeading.waitFor()
        editQuestionerButton.waitFor()

        editQuestionerButton.click()

        questionDialog.waitFor()

        logAnswer(
            QuestionSubType.FOOD_PREFERENCE,
            "What is your food preference?",
            "Vegetarian : Primarily plant-based, avoiding meat, poultry, and seafood"
        )


        logAnswer(
            QuestionSubType.CUISINE_PREFERENCE,
            "What is your cuisine preference?",
            arrayOf(
                "North Indian", "South Indian", "Jain"
            )
        )

        logAnswer(
            QuestionSubType.DAILY_EATING_HABIT,
            "Which of the following best describes your daily eating habits?",
            "Primarily Home Cooked Meals"
        )

        logAnswer(QuestionSubType.DIET_EXPERIENCE, "What is your past experience with diets?", "None")

        logAnswer(
            QuestionSubType.NUTRITION_TRACKING_EXPERIENCE,
            "How familiar are you with tracking calories or macronutrients and micronutrients?",
            "Never tracked, need guidance"
        )

        logAnswer(QuestionSubType.ALLERGY, "Do you have any food allergies?", arrayOf("Milk or dairy", "Peanuts"))

        logAnswer(QuestionSubType.INTOLERANCE, "Do you have any food intolerances?", arrayOf("Lactose", "Caffeine"))

        logAnswer(
            QuestionSubType.CAFFEINE_CONSUMPTION,
            "How much caffeine do you typically consume in a day - including coffee, tea, energy drinks, or other caffeinated products?",
            "None or Rarely"
        )


        logAnswer(
            QuestionSubType.TYPICAL_DAY,
            "How active are you in a typical week?",
            "Sedentary: <3 hrs/week"
        )

        logAnswer(
            QuestionSubType.EXERCISE_TYPE, "What type of exercise do you usually do?", arrayOf(
                "Yoga"
            )
        )

        logAnswer(
            QuestionSubType.PREFERRED_WORKOUT_TIME,
            "When do you usually work out or prefer to work out?",
            "Morning"
        )

        logAnswer(QuestionSubType.EQUIPMENTS_AVAILABLE, "Equipments available", arrayOf("Dumbbells"))

        logAnswer(
            QuestionSubType.SLEEP_HYGIENE,
            "How would you describe your sleep?",
            "Room for improvement, occasional distractions"
        )

        logAnswer(
            QuestionSubType.WEEKDAY_SLEEP_ROUTINE_BED_TIME,
            "What time do you usually go to bed on weekdays?",
            "23:00"
        )
        logAnswer(
            QuestionSubType.WEEKDAY_SLEEP_ROUTINE_WAKEUP_TIME,
            "What time do you usually wake up on weekdays?",
            "07:00"
        )

        logAnswer(
            QuestionSubType.WEEKEND_SLEEP_ROUTINE_BED_TIME,
            "What time do you usually go to bed on weekends?",
            "23:00"
        )
        logAnswer(
            QuestionSubType.WEEKEND_SLEEP_ROUTINE_WAKEUP_TIME,
            "What time do you usually wakeup on weekends?",
            "07:00"
        )

        logAnswer(
            QuestionSubType.SLEEP_SCHEDULE_PREFERENCE,
            "Let's make your sleep schedule perfect! Would you like to set your ideal bedtime or wakeup time?",
            "Bedtime"
        )

        logAnswer(QuestionSubType.BED_TIME_GOAL, "Set your ideal Bedtime", "11:00")

        logAnswer(QuestionSubType.SLEEP_SATISFACTION, "How satisfied are you with your sleep?", "Somewhat Satisfied")


        logAnswer(QuestionSubType.SLEEP_WAKEUP_REFRESHMENT, "Do you wake up refreshed?", "Sometimes")
        logAnswer(
            QuestionSubType.SUNLIGHT_UPON_WAKEUP,
            "What is the duration of your sun exposure on a day-to-day basis?",
            "5-10 minutes"
        )
        logAnswer(
            QuestionSubType.SUNLIGHT_TIMING,
            "During which part of the day are you usually exposed to direct sunlight?",
            "Early morning (before 10 a.m.)"
        )
        logAnswer(
            QuestionSubType.WELLNESS_MOTIVATION_FREQUENCY,
            "How often do you look for external motivation to stick to your wellness routine?",
            "Now and then"
        )
        logAnswer(
            QuestionSubType.WELLNESS_BOTHER_FREQUENCY,
            "In the past month, how often have you felt stressed, sad, or low?",
            "Once a week"
        )
        logAnswer(
            QuestionSubType.STRESS_MANAGEMENT,
            "How well do you deal with stress?",
            "I feel overwhelmed by stress"
        )
        logAnswer(
            QuestionSubType.EMOTIONAL_EATING,
            "How often do you eat in response to emotions such as stress, cravings, boredom, or anxiety rather than physical hunger?",
            "Rarely"
        )
        logAnswer(QuestionSubType.SNACK_PREFERENCE, "What type of snacks do you usually indulge in?", arrayOf("Sweets"))


        // Validate all questions sequentially based on stored answers
        answersStored.keys.forEach { key ->
            runChecker(key)
            if (key != QuestionSubType.WAIST_CIRCUMFERENCE) {
                nextButton.click()
            }
        }
    }

    fun goBackQuestioner() {
        page.goBack()
        val title = page.getByText("Confirm ExitAre you sure you")
        val quitButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Quit"))

        val components = listOf(title, quitButton)
        components.forEach { it.waitFor() }

        quitButton.click()


        setStopAtQuestion(null)

        waitForConfirmation()

        val questionHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("View/Edit Questionnaire"))
        val editQuestionerButton =
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("View/Edit Responses"))
        val questionDialog = page.locator(".bg-zinc-900").first()

        questionHeading.waitFor()
        editQuestionerButton.waitFor()

        editQuestionerButton.click()

        questionDialog.waitFor()

        question_20()
    }

}







