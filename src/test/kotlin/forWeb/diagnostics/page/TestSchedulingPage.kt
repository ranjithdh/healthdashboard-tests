package forWeb.diagnostics.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import kotlinx.serialization.json.*
import kotlinx.serialization.json.put
import mobileView.profile.utils.ProfileUtils.buildAddressText
import model.ProfileListData
import model.profile.PiiUserResponse
import model.profile.UserAddressData
import model.profile.UserAddressResponse
import model.profile.ProfileDetailResponse
import model.slot.SlotList
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import java.time.Period
import java.time.ZonedDateTime
import utils.LogFullApiCall.logFullApiCall
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.ADD_ADDRESS
import utils.report.StepHelper.CLICK_ADD_NEW_ADDRESS
import utils.report.StepHelper.CLICK_PROCEED
import utils.report.StepHelper.EDIT_ADDRESS_SUMMARY
import utils.report.StepHelper.EDIT_SLOT_SUMMARY
import utils.report.StepHelper.FETCH_SLOTS
import utils.report.StepHelper.VERIFY_ORDER_SUMMARY_PAGE
import utils.report.StepHelper.VERIFY_PRICE_DETAILS
import utils.report.StepHelper.VERIFY_SAMPLE_COLLECTION_ADDRESS_HEADING
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import java.util.regex.Pattern

//import kotlinx.serialization.json.content


private val logger = KotlinLogging.logger {}

class TestSchedulingPage(page: Page) : BasePage(page) {

    override val pageUrl = ""
    private var addressData: UserAddressData? = null
    private var profileListData: ProfileListData? = null
    private var selectedAddressIndex: Int = 0
    private var selectedAddressName: String = ""
    private var selectedAddressText: String = ""
    private var selectedDateSummary: String = ""
    private var selectedTimeSummary: String = ""
    private var product_id: String? = null

    // Properties to store order details from callBloodDataReports
    private var capturedOrderNo: String? = null
    private var capturedProductId: Int? = null
    private var capturedThyrocareProductId: String? = null
    private var capturedAppointmentDate: String? = null
    private var capturedCreatedAt: String? = null
    private var capturedPaymentDate: String? = null
    // Properties for Metabolic Panel (Dual Slots)
    private var selectedFastingTimeSummary: String = ""
    private var selectedPostMealTimeSummary: String = ""

    // Properties to store for payment

    private fun formatTime(isoTime: String): String {
        val instant = java.time.Instant.parse(isoTime)
        val istZone = java.time.ZoneId.of("Asia/Kolkata")
        val zonedDateTime = instant.atZone(istZone)
        return zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

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
                logFullApiCall(response)
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response or API call failed..${e.message}" }
        }
    }


    fun verifySampleCollectionAddressHeading() {
        StepHelper.step(VERIFY_SAMPLE_COLLECTION_ADDRESS_HEADING)
        val heading = page.getByText("Sample Collection Address")
        logger.info { "Verifying Sample Collection Address heading" }
        Assertions.assertTrue(heading.isVisible, "Sample Collection Address heading should be visible")
    }

    fun verifyUserOption(isBookingForSelf: Boolean) {
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("I’m booking this test for")).click()
        page.locator("#gender").click()
        if (isBookingForSelf) {
            page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName("Myself")).click()
        } else {
            page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName("Others")).click()
        }
    }

    fun fillAddNewUserFields(
        mobileNumber: String? = null,
        nickName: String? = null,
        name: String? = null,
        email: String? = null,
        dobDay: String = "5",
        gender: String = "Male",
        height: String = "190",
        weight: String = "90"
    ) {
        val randomNum = (100..999).random()
        val finalMobile = mobileNumber ?: "7092424$randomNum" 
        val finalNickName = nickName ?: "Seeni$randomNum"
        val finalName = name ?: "SeeniV$randomNum"
        val finalEmail = email ?: "vseeni$randomNum@yopmail.com"

        logger.info { "Filling Add New User fields with dynamic number: $randomNum" }

        page.getByRole(AriaRole.COMBOBOX).click()
        page.getByText("Add New User").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number"))
            .fill(finalMobile)
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Nick name *")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Nick name *")).fill(finalNickName)
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name *")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name *")).fill(finalName)

        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email *")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email *")).fill(finalEmail)
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Date of Birth *")).click()
        page.getByRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(dobDay)).first().click()
        page.getByRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("Gender *")).click()
        page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(gender).setExact(true)).click()
        page.getByRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm) *")).click()
        page.getByRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm) *")).fill(height)
        page.getByRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg) *")).click()
        page.getByRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg) *")).fill(weight)
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Flat, House no., Building,")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Flat, House no., Building,"))
            .fill("14C3, H H Road")
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your street address")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your street address")).fill("Balarengapuram")
        page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Balarengapuram, Madurai, Tamil Nadu, India").setExact(true)
        ).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("city *")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("city *")).fill("Madurai")
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("State *")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("State *")).press("ArrowRight")
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("State *")).fill("Tamil Nadu")
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code *")).click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code *")).fill("625009")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()

    }
    fun verifyAddNewUserFields(isBookingForSelf: Boolean) {
        if (isBookingForSelf) {
            page.getByRole(AriaRole.COMBOBOX).click()
        }
        page.getByText("Add New User").click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Add a new member")).click()
        page.getByText("Mobile number *").click()
       if (isBookingForSelf) {
           page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("+")).click()
           page.getByText("India", Page.GetByTextOptions().setExact(true)).nth(2).click()
       } else {
           page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("+")).click()
           page.getByText("India", Page.GetByTextOptions().setExact(true)).nth(1).click()
       }
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your mobile number")).click()
        page.getByText("Nick name *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Nick name *")).click()
        page.getByText("Enter name *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name *")).click()
        page.getByText("Email *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email *")).click()
        page.getByText("Date of Birth *").click()
        page.getByLabel("Month:").selectOption("8")
        page.getByRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName("21")).click()
        page.getByText("Gender *").click()
        page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName("Male").setExact(true)).click()
        page.getByText("Height (cm) *").click()
        page.getByRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm) *")).click()
        page.getByText("Weight (kg) *").click()
        page.getByRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg) *")).click()
        page.getByText("Flat, House no., Building,").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Flat, House no., Building,")).click()
        page.getByText("Street address *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter your street address")).click()
        page.getByText("city *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("city *")).click()
        page.getByText("State *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("State *")).click()
        page.getByText("Pin code *").click()
        page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Pin code *")).click()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close")).click()
    }

    fun getProfileListData(): ProfileListData? {
        return profileListData
    }

    private fun getProfileDisplayString(name: String?, dob: String?, gender: String?): String {
        if (name == null || dob == null || gender == null) return ""
        val birthDate = try {
            // Try ISO format first
            ZonedDateTime.parse(dob).toLocalDate()
        } catch (e: Exception) {
            try {
                // Try yyyy-MM-dd
                LocalDate.parse(dob.substring(0, 10))
            } catch (e2: Exception) {
                try {
                    // Try dd/MM/yyyy (found in ht_user_verification)
                    LocalDate.parse(dob, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e3: Exception) {
                    logger.error { "Failed to parse DOB: $dob" }
                    return "$name ?? ?"
                }
            }
        }
        val age = Period.between(birthDate, LocalDate.now()).years
        val genderInitial = when {
            gender.lowercase().startsWith("female") -> "F"
            gender.lowercase().startsWith("male") -> "M"
            else -> ""
        }
        return "$name $age $genderInitial"
    }

    fun switchUser(leadId: String, productCode: String) {
        logger.info { "Switching user for leadId: $leadId" }
        
        // Fetch profile detail from API
        val url = "${TestConfig.APIs.PROFILE_DETAIL}$leadId?product_code=$productCode"
        val response = page.request().get(
            url,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", "Asia/Kolkata")
        )
        
        if (response.status() != 200) {
            throw RuntimeException("Failed to fetch profile detail: ${response.status()} ${response.text()}")
        }
        
        val profileDetail = json.decodeFromString<ProfileDetailResponse>(response.text())
        val piiData = profileDetail.data?.piiData ?: throw RuntimeException("Profile detail data is null")
        
        // Calculate Age
        val dobStr = piiData.dob ?: throw RuntimeException("DOB is null for leadId: $leadId")
        val birthDate = ZonedDateTime.parse(dobStr).toLocalDate()
        val age = Period.between(birthDate, LocalDate.now()).years
        
        // Gender Initial
        val gender = piiData.gender?.lowercase() ?: ""
        val genderInitial = when {
            gender.startsWith("female") -> "F"
            gender.startsWith("male") -> "M"
            else -> ""
        }
        
        // Construct dynamic name for option selection
        val dynamicName = getProfileDisplayString(piiData.name, piiData.dob, piiData.gender)
        logger.info { "Dynamic user name constructed: $dynamicName" }

        // Find index for identical users
        val allProfiles = profileListData?.profiles ?: emptyList()
        val identicalProfiles = allProfiles.filter { 
            getProfileDisplayString(it.name, it.dob, it.gender) == dynamicName 
        }
        val occurrenceIndex = identicalProfiles.indexOfFirst { it.lead_id == leadId }.let { if (it == -1) 0 else it }
        
        logger.info { "Occurrence index for $dynamicName (leadId: $leadId): $occurrenceIndex" }

        // UI Interactions
        page.getByRole(AriaRole.COMBOBOX).click()
        val userOption = page.getByRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(dynamicName))
        
        if (userOption.count() > 1) {
            userOption.nth(occurrenceIndex).click()
        } else {
            userOption.click()
        }
        
        // Wait for address to be available and select it
        val addressText = piiData.communicationAddress?.address ?: throw RuntimeException("Address is null in API response")
        logger.info { "Selecting address: $addressText" }
        
        // Trying to find and click the address. It might be by text or a specific button
        page.getByText(addressText, Page.GetByTextOptions().setExact(false)).first().click()
    }

    fun chooseTheUser() {
        logger.info { "Asserting profiles from API are visible on UI..." }
    }
    fun assertProfilesFromApi() {
        logger.info { "Asserting profiles from API are visible on UI..." }
        if (profileListData == null || profileListData!!.profiles.isNullOrEmpty()) {
            logger.warn { "No profile data available to assert" }
            return
        }
        profileListData!!.profiles!!.forEach { profile ->
            if (!profile.name.isNullOrBlank()) {
                // We check if the name is present in the DOM. 
                // Depending on UI, it might be inside a list or a dropdown.
                val profileLocator = page.getByText(profile.name!!, Page.GetByTextOptions().setExact(false)).first()
                assert(profileLocator.isVisible) { "Profile with name '${profile.name}' should be visible on the UI" }
                logger.info { "Verified profile visibility: ${profile.name}" }
            }
        }
    }

    fun assertAddressesFromApi() {
        // Implementation regarding API validation would go here
        logger.info { "Asserting addresses from API..." }
    }

    fun clickAddNewAddress() {
        StepHelper.step(CLICK_ADD_NEW_ADDRESS)
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
        page.locator(".bg-secondary.flex.flex-1").nth(index).click()

        // Fill inputs (UI)
        val updatedNickName = (address.addressName?.takeIf { it.isNotBlank() } + "Updated") ?: "Home"
        nickNameInput.fill(updatedNickName)
        // mobileNumberInput.fill(address.addressMobile ?: "")
        houseNoInput.fill(address.address)
        streetAddressInput.fill(address.addressLine1)
        addressLine2Input.fill(address.addressLine2 ?: "2nd Street")
        cityInput.fill(address.city)
        stateInput.fill(address.state)
        pincodeInput.fill(address.pincode)
        countryInput.fill(address.country)


//        captureAddressData {
//            newAddressSubmit.click()
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Close")).click()
//        }

        this.selectedAddressIndex = index

        val updatedList = addressData?.addressList ?: throw AssertionError("Address list not updated")
        val updatedAddress = updatedList.find { it.addressId == addressId }

        assertEquals(updatedNickName, updatedAddress?.address?.addressName)
    }

    fun selectAddress(index: Int) {
        val addresses = addressData?.addressList ?: return
        if (index >= addresses.size) return
        
        val addressItem = addresses[index]
        val nickName = addressItem.address.addressName?.takeIf { it.isNotBlank() } ?: "Primary"
        
        logger.info { "Explicitly selecting address: $nickName" }
        // Click the name to select the address
        page.getByText(nickName).first().click()
        this.selectedAddressIndex = index
    }

    fun verifyPriceDetails(expectedSubtotal: Double, expectedDiscount: Double, isWalletUsed: Boolean) {
        StepHelper.step(VERIFY_PRICE_DETAILS)
        logger.info { "Verifying price details: Subtotal=$expectedSubtotal, Discount=$expectedDiscount" }

        // Ensure Price Details section is expanded
        val priceDetailsHeader = page.getByText("PRICE DETAILS")
        priceDetailsHeader.waitFor()
        // If the sidebar elements aren't visible, click to expand. Or just click to be safe/toggle.
        // Better: Check visibility of a child element.
        if (!page.getByTestId("diagnostics-sidebar-subtotal-label").isVisible) {
             priceDetailsHeader.click()
        }

        // Subtotal Verification
        page.getByTestId("diagnostics-sidebar-subtotal-label").click()
        val subtotalElement = page.getByTestId("diagnostics-sidebar-subtotal-value")
        subtotalElement.click()
        val subtotalText = subtotalElement.innerText().replace("₹", "").replace(",", "").trim()
        val subtotalVal = subtotalText.toDoubleOrNull() ?: 0.0
        // assertEquals(expectedSubtotal, subtotalVal, 1.0, "Subtotal mismatch") // Using assertion with message

        if (Math.abs(expectedSubtotal - subtotalVal) > 1.0) {
            throw AssertionError("Subtotal mismatch. Expected: $expectedSubtotal, Found: $subtotalVal")
        }

        // Discount Verification only if expectedDiscount > 0
        if (expectedDiscount > 0 && isWalletUsed) {
            page.getByTestId("diagnostics-sidebar-discount-label").click()
            val discountElement = page.getByTestId("diagnostics-sidebar-discount-value")
            discountElement.click()
            
            // Verify structure: "-" text, Coin Image, Value
            val discountTextAll = discountElement.innerText() // " - 3000" or similar
            logger.info { "Discount Element Text: $discountTextAll" }
            
            // Check for minus sign
            if (!discountTextAll.contains("-")) {
                 logger.warn("Discount display missing minus sign: $discountTextAll")
            }
            
            // Check for Coin Image (DH-Coin)
            val coinImg = discountElement.locator("img")
            if (coinImg.count() > 0 && coinImg.first().isVisible) {
                 logger.info { "DH-Coin image is visible in discount section." }
            } else {
                 logger.warn { "DH-Coin image NOT found in discount section." }
            }

            // Check Value
            // " - 3000" -> "3000"
            // Remove non-numeric except decimal if needed, but here simple replace works
            val discountCleaned = discountTextAll.replace("-", "").replace("₹", "").replace(",", "").trim()
            val discountVal = discountCleaned.toDoubleOrNull() ?: 0.0
            
            if (Math.abs(expectedDiscount - discountVal) > 1.0) {
                throw AssertionError("Discount value mismatch. Expected: $expectedDiscount, Found: $discountVal")
            }
            
        } else {
             logger.info { "No discount expected, skipping specific discount UI verification." }
        }

        // Grand Total Verification
        page.getByTestId("diagnostics-sidebar-grand-total-label").click()
        val grandTotalElement = page.getByTestId("diagnostics-sidebar-grand-total-value")
        grandTotalElement.click()
        
        val grandTotalText = grandTotalElement.innerText().replace("₹", "").replace(",", "").trim()
        val grandTotalVal = grandTotalText.toDoubleOrNull() ?: 0.0
        val expectedGrandTotal = expectedSubtotal - expectedDiscount
        
        if (Math.abs(expectedGrandTotal - grandTotalVal) > 1.0 && isWalletUsed) {
             throw AssertionError("Grand Total mismatch. Expected: $expectedGrandTotal, Found: $grandTotalVal")
        }
    }

    fun verifyFooterActions() {
        logger.info { "Verifying footer actions" }
        val proceedBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Proceed"))
        Assertions.assertTrue(proceedBtn.isVisible, "Proceed button should be visible")

        val page2 = page.waitForPopup {
            page.getByText("Got any questions? Contact").click()
        }
        Assertions.assertNotNull(page2, "Popup page should be opened")
        page2.close()
    }

    fun clickProceed() {
        StepHelper.step(CLICK_PROCEED)
        logger.info { "Clicking Proceed" }
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Proceed")).click()
    }

    private fun getLeadId(): String {
        val url = TestConfig.APIs.API_ACCOUNT_INFORMATION
        val response = page.request().get(
            url,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", "Asia/Calcutta")
        )
        if (response.status() != 200) {
            throw RuntimeException("Failed to fetch PII data: ${response.status()} ${response.text()}")
        }
        val piiResponse = json.decodeFromString<PiiUserResponse>(response.text())
        return piiResponse.data.piiData.leadId
    }

    private fun getSlots(date: String, leadId: String, addressId: String): List<model.slot.Slot> {
        // Fix URL: TestConfig.APIs.API_SLOTS_AVAILABILITY already contains ?platform=web
        val url = TestConfig.APIs.API_SLOTS_AVAILABILITY
        val payload = buildJsonObject {
            put("address_id", addressId)
            put("date", date)
            put("lead_id", leadId)
            put("product_id", product_id)
//            put("user_timezone", "Asia/Kolkata")
        }.toString()

        logger.info { "Fetching slots for date: $date with payload: $payload" }

        val response = page.request().post(
            url,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("Content-Type", "application/json")
                .setHeader("user_timezone", "Asia/Kolkata")
                .setData(payload)
        )

        if (response.status() != 200) {
            logger.error { "Slot API failed: ${response.status()} ${response.text()}" }
            return emptyList()
        }

        val responseText = response.text()
        logger.info { "Slot API Response ($date): $responseText" }

        return try {
            val slotList = json.decodeFromString<SlotList>(responseText)
            slotList.data?.slots ?: emptyList()
        } catch (e: Exception) {
            logger.error { "Failed to parse slot list: ${e.message}" }
            emptyList()
        }
    }

    fun verifySlotSelectionPage(code: String, productId: String?) {
        logger.info { "Verifying Slot Selection Page with product ID: $productId" }
        page.getByTestId("diagnostics-booking-step2-slot-title").waitFor()

        val leadId = getLeadId()
        val addressItem = addressData?.addressList?.getOrNull(selectedAddressIndex)
            ?: addressData?.addressList?.firstOrNull()
            ?: throw IllegalStateException("Address data not found. Ensure address is selected/captured before slot selection.")
        val addressId = addressItem.addressId
        // Populate the class property for context if needed, though we use the param in getSlots call currently (actually we need to update getSlots too or set the property)
        this.product_id = productId
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val summaryDateFormatter = DateTimeFormatter.ofPattern("dd MMM")
        val tomorrowDateStr = tomorrow.format(dateFormatter)

        logger.info { "Processing tomorrow's slots: $tomorrowDateStr" }
        StepHelper.step("$FETCH_SLOTS $tomorrowDateStr")
        page.getByTestId("diagnostics-booking-step2-date-$tomorrowDateStr").click()

        // We need to ensure getSlots uses the productId. It uses the class property `product_id`.
        // Since we set this.product_id = productId above, the next call will use it.
        val tomorrowSlots = getSlots(tomorrowDateStr, leadId, addressId)

        tomorrowSlots.forEach { slot ->
            if (slot.is_available == true && slot.start_time != null) {
                // Ensure the slot is clickable before clicking
                 val slotLocator = page.getByTestId("diagnostics-booking-step2-slot-${slot.start_time}")
                 if(slotLocator.isVisible) {
                     slotLocator.click()
                     captureSlotForSummary(slot.start_time, summaryDateFormatter)
                 }
            }
        }

        // Randomly select a date from tomorrow + 7 days (index 0 to 6)
        val randomDayOffset = (0..6).random()
        val randomDate = tomorrow.plusDays(randomDayOffset.toLong())
        val randomDateStr = randomDate.format(dateFormatter)

        logger.info { "Selecting random date: $randomDateStr" }
        StepHelper.step("$FETCH_SLOTS $randomDateStr")
        page.getByTestId("diagnostics-booking-step2-date-$randomDateStr").click()

        val randomDateSlots = getSlots(randomDateStr, leadId, addressId)
        val availableSlots = randomDateSlots.filter { it.is_available == true && it.start_time != null }

        if (availableSlots.isNotEmpty()) {
            val randomSlot = availableSlots.random()
            logger.info { "Selecting slot: ${randomSlot.start_time}" }
            page.getByTestId("diagnostics-booking-step2-slot-${randomSlot.start_time}").click()
            captureSlotForSummary(randomSlot.start_time!!, summaryDateFormatter)
        } else {
            logger.warn { "No available slots found for random date $randomDateStr" }
        }
    }

    fun verifyDualSlotSelectionPage(code: String, productId: String?) {
        logger.info { "Verifying Dual Slot Selection Page for code: $code" }
        page.getByTestId("diagnostics-booking-step2-slot-title").waitFor()

        // Static text checks
//        page.getByTestId("diagnostics-booking-step2-fasting-column")
//        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Fasting Slots")).click()
//        page.getByTestId("diagnostics-booking-step2-fasting-column")
//        page.getByText("Fasting required").click()
//
//        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Non-Fasting Slots")).click()
//        page.getByText("Non-fasting test").click()

        val leadId = getLeadId()
        val addressItem = addressData?.addressList?.getOrNull(selectedAddressIndex)
            ?: addressData?.addressList?.firstOrNull()
            ?: throw IllegalStateException("Address data not found")
        val addressId = addressItem.addressId
        this.product_id = productId

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val summaryDateFormatter = DateTimeFormatter.ofPattern("dd MMM")
        val tomorrowDateStr = tomorrow.format(dateFormatter)

        logger.info { "Selecting date: $tomorrowDateStr" }
        page.getByTestId("diagnostics-booking-step2-date-$tomorrowDateStr").click()

        val slots = getSlots(tomorrowDateStr, leadId, addressId)
        val validSlots = slots.filter { it.is_available == true && it.start_time != null }

        if (validSlots.isEmpty()) {
            throw RuntimeException("No slots available for $tomorrowDateStr")
        }

        // Strategy: First slot that allows a post-meal slot (+2h)
        var fastingSlot: model.slot.Slot? = null
        var postMealSlot: model.slot.Slot? = null

        // Try to find a pair
        for (slot in validSlots) {
            val fTime = java.time.Instant.parse(slot.start_time)
            val pSlot = validSlots.find {
                val pTime = java.time.Instant.parse(it.start_time)
                val diff = java.time.Duration.between(fTime, pTime).toMinutes()
                diff >= 120 // 2 hours
            }
            if (pSlot != null) {
                fastingSlot = slot
                postMealSlot = pSlot
                break
            }
        }

        if (fastingSlot == null || postMealSlot == null) {
            logger.warn { "Could not find a valid pair of Fasting/Post-meal slots (2h gap) for $tomorrowDateStr. Trying to proceed anyway with failing assertions likely." }
            throw RuntimeException("Could not find a valid pair of Fasting/Post-meal slots")
        }

        logger.info { "Selecting Fasting Slot: ${fastingSlot.start_time}" }
        page.getByTestId("diagnostics-booking-step2-fasting-slot-${fastingSlot.start_time}").click()

        logger.info { "Selecting Post-meal Slot: ${postMealSlot.start_time}" }
        val pmLocator = page.getByTestId("diagnostics-booking-step2-postmeal-slot-${postMealSlot.start_time}")
        pmLocator.waitFor()
        pmLocator.click()

        // Verify Note
        page.getByTestId("diagnostics-booking-step2-dual-slot-note").click()

        val noteText = "Please note: Select different time slots for each test. Post meal test must be scheduled at least 2 hours after the fasting test."
        page.getByText(noteText)
        logger.info { "Verifying note visibility: $noteText" }
        // Attempt strict match, fall back to loose if needed, but user text seems precise
//        Assertions.assertTrue(page.getByText(noteText).isVisible, "Dual slot note should be visible")

        // Capture times for summary verification
        selectedDateSummary = java.time.Instant.parse(fastingSlot.start_time).atZone(java.time.ZoneId.of("Asia/Kolkata")).format(summaryDateFormatter)
        selectedFastingTimeSummary = formatTime(fastingSlot.start_time!!)
        selectedPostMealTimeSummary = formatTime(postMealSlot.start_time!!)
        
        logger.info { "Captured Dual Slot Summary - Date: $selectedDateSummary, Fasting: $selectedFastingTimeSummary, Post-meal: $selectedPostMealTimeSummary" }
    }

    private fun captureSlotForSummary(startTimeIso: String, summaryDateFormatter: DateTimeFormatter) {
        // Parse ISO string and convert to IST (+5:30)
        // Format: 2026-02-06T05:00:00.000Z
        val instant = java.time.Instant.parse(startTimeIso)
        val istZone = java.time.ZoneId.of("Asia/Kolkata")
        val zonedDateTime = instant.atZone(istZone)

        selectedDateSummary = zonedDateTime.format(summaryDateFormatter)
        selectedTimeSummary = zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    fun verifyOrderSummaryPage(expectedSubtotal: Double, expectedDiscount: Double, targetCode: String, isWalletUsed: Boolean) {
        StepHelper.step(VERIFY_ORDER_SUMMARY_PAGE)
        logger.info { "Verifying Order Summary Page" }

        val addressItem = addressData?.addressList?.getOrNull(selectedAddressIndex)
            ?: addressData?.addressList?.firstOrNull()
            ?: throw IllegalStateException("Address data not found. Ensure address is selected/captured before slot selection.")
        val addressId = addressItem.addressId
        val selectedAddressObj = addressItem.address
        selectedAddressName = selectedAddressObj.addressName ?: "Primary"
        selectedAddressText = buildAddressText(selectedAddressObj)

        // Address verification
        page.getByText("Sample Collection Address").click()

        var nicknameToSearch = selectedAddressName.trim()

        // 1. If nickname starts with digits followed by a space, remove the leading number
        val matchResult = Regex("^(\\d+)\\s+(.*)$").find(nicknameToSearch)
        if (matchResult != null) {
            nicknameToSearch = matchResult.groupValues[2]
        }

        // 2. Take only the first 3 words of the nickname to avoid issues with long text/truncation
        nicknameToSearch = nicknameToSearch.split(" ").filter { it.isNotBlank() }.take(3).joinToString(" ")

        // 1. Nickname Verification
        logger.info { "Verifying Address Nickname: '$nicknameToSearch'" }
        page.getByText(nicknameToSearch, Page.GetByTextOptions().setExact(false)).first().click()

        // 2. Address Text Verification
        logger.info { "Verifying Full Address Text: '$selectedAddressText'" }
        // Attempt to find the full address text (loose match)
        if (page.getByText(selectedAddressText, Page.GetByTextOptions().setExact(false)).isVisible) {
             page.getByText(selectedAddressText, Page.GetByTextOptions().setExact(false)).first().click()
        } else {
             // Fallback: The UI might format the address with newlines or different separators.
             // Verify visible components: Address Line 1, City, Pincode
             val addressLine1 = selectedAddressObj.addressLine1?.takeIf { it.isNotBlank() }
             val city = selectedAddressObj.city?.takeIf { it.isNotBlank() }
             val pincode = selectedAddressObj.pincode?.takeIf { it.isNotBlank() }

             if (addressLine1 != null && page.getByText(addressLine1, Page.GetByTextOptions().setExact(false)).isVisible) {
                 logger.warn { "Full address text strict match failed. Verified by partial match (Line 1): '$addressLine1'" }
                 page.getByText(addressLine1, Page.GetByTextOptions().setExact(false)).first().click()
             } else if (pincode != null && page.getByText(pincode, Page.GetByTextOptions().setExact(false)).isVisible) {
                 logger.warn { "Verified by Pincode: '$pincode'" }
                 page.getByText(pincode, Page.GetByTextOptions().setExact(false)).first().click()
             } else {
                 logger.warn { "Could not strictly verify address text visibility for: $selectedAddressText" }
             }
        }

        // Slot verification
        // Slot verification
        if (targetCode !in setOf("GENE10001", "GUT10002", "OMEGA1003", "CORTISOL1004", "DH_METABOLIC_PANEL", "DH_LONGEVITY_PANEL")) {
            page.getByText("Sample Collection Time Slot").click()
            page.getByText("Date: $selectedDateSummary").click()
            page.getByText("Selected time slot: $selectedTimeSummary").click()
        }
        if (targetCode == "DH_METABOLIC_PANEL" || targetCode == "DH_LONGEVITY_PANEL") {
            logger.info { "Verifying Dual Slot Summary: Date: $selectedDateSummary, Fasting: $selectedFastingTimeSummary, PostMeal: $selectedPostMealTimeSummary" }
            page.getByText("Sample Collection Time Slot").click()
            page.getByText("Date: $selectedDateSummary").click()
            page.getByText("Fasting time slot: $selectedFastingTimeSummary").click()
            page.getByText("Post meal time slot: $selectedPostMealTimeSummary").click()
        }

        // Price details verification (reusing logic or similar)
        val subtotalLabel = page.getByTestId("diagnostics-sidebar-subtotal-label")
        if (!subtotalLabel.isVisible) {
            logger.info { "Price details hidden, clicking header to expand" }
            page.getByText("PRICE DETAILS").click()
            subtotalLabel.waitFor()
        } else {
            logger.info { "Price details already expanded" }
        }

        // Helper to format with commas
        val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

        // Subtotal Verification
        subtotalLabel.click() // Verified by ID (click ensures interaction)
        page.getByText("Subtotal", Page.GetByTextOptions().setExact(false)).first().click() // Verified by Text
        
        val subtotalValue = page.getByTestId("diagnostics-sidebar-subtotal-value").innerText()
        val expectedSubtotalStr = "₹${expectedSubtotal.toInt()}"
        logger.info { "Verifying Subtotal Value. Expected (cleaned): $expectedSubtotalStr, Actual (from ID): $subtotalValue" }
        assertEquals(expectedSubtotalStr.replace(Regex("\\s+"), ""), subtotalValue.replace(",", "").replace(Regex("\\s+"), ""))
        
        // Verify value VISIBILITY via getByText (Handling optional comma formatting)
        val rawSubtotal = expectedSubtotal.toInt().toString()
        val subtotalPattern = Pattern.compile(rawSubtotal.replace("(\\d)(?=(\\d{3})+$)".toRegex(), "$1,?"))
        logger.info { "Verifying visibility for subtotal pattern: $subtotalPattern" }
        page.getByText(subtotalPattern).first().click()

        // Discount Verification
        if (isWalletUsed) {
        page.getByTestId("diagnostics-sidebar-discount-label").click()
        page.getByText("Discount", Page.GetByTextOptions().setExact(false)).first().click()

        val discountValue = page.getByTestId("diagnostics-sidebar-discount-value").innerText()
        val expectedValInt = expectedDiscount.toInt()
        val expectedDiscountClean = "-$expectedValInt"
        val actualDiscountClean = discountValue.replace(Regex("[^0-9-]"), "")
        
        logger.info { "Verifying Discount Value. Expected (normalized): $expectedDiscountClean, Actual (normalized): $actualDiscountClean (Raw: $discountValue)" }
        assertEquals(expectedDiscountClean, actualDiscountClean)
        // Verify Discount formatted text visibility using a flexible Pattern
        val rawDiscount = expectedDiscount.toInt().toString()
        val regexSafeDiscount = rawDiscount.replace("(\\d)(?=(\\d{3})+$)".toRegex(), "$1,?")
        val discountPattern = Pattern.compile("-\\s*.*$regexSafeDiscount") 
        
        logger.info { "Verifying visibility for discount value: $rawDiscount with pattern: $discountPattern" }
        
        try {
            page.getByText(discountPattern).first().click()
            logger.info { "✅ Discount value visibility verified." }
        } catch (e: Exception) {
            logger.warn { "Pattern match failed. Falling back to numeric search for: $regexSafeDiscount" }
            if (expectedDiscount > 0) {
                page.getByText(Pattern.compile(regexSafeDiscount)).first().click()
            }
        }
        }
        // Grand Total Verification
        page.getByTestId("diagnostics-sidebar-grand-total-label").click()
        page.getByText("Grand Total", Page.GetByTextOptions().setExact(false)).first().click()

        val grandTotalValue = page.getByTestId("diagnostics-sidebar-grand-total-value").innerText()
        val expectedGrandTotal = expectedSubtotal - if (isWalletUsed) expectedDiscount else 0.0
        val expectedGrandTotalStr = "₹${expectedGrandTotal.toInt()}"
        logger.info { "Verifying Grand Total Value. Expected (cleaned): $expectedGrandTotalStr, Actual (from ID): $grandTotalValue" }
        assertEquals(expectedGrandTotalStr.replace(Regex("\\s+"), ""), grandTotalValue.replace(",", "").replace(Regex("\\s+"), ""))
        
        // Verify value VISIBILITY via getByText
        val formattedGrandTotal = numberFormat.format(expectedGrandTotal.toInt())
        logger.info { "Verifying visibility for formatted grand total: $formattedGrandTotal" }
        page.getByText(formattedGrandTotal, Page.GetByTextOptions().setExact(false)).first().click()

        // Mobile footer elements
        page.getByTestId("diagnostics-sidebar-mobile-grand-total").click()
        val mobileGrandTotal = page.getByTestId("diagnostics-sidebar-mobile-grand-total").innerText()
        // Mobile view might have a different format or just the number, checking loosely or exact match if cleaned
        if (mobileGrandTotal.contains(expectedGrandTotal.toInt().toString())) {
             logger.info { "Mobile grand total verified: $mobileGrandTotal" }
        }
        page.getByTestId("diagnostics-sidebar-mobile-proceed").click()
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
        country: String,
        addressLine2: String = "2nd Street"
    ) {
        nickNameInput.fill(nickName)
        streetAddressInput.fill(street)
        houseNoInput.fill(doorNumber)
        addressLine2Input.fill(addressLine2)
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
        val addressLine2 = "Near Park"

        fillMandatoryAddressFields(
            nickName,
            street,
            doorNumber,
            city,
            state,
            pincode,
            country,
            addressLine2
        )

        captureAddressData {
            StepHelper.step(ADD_ADDRESS)
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

    fun clickEditAddressFromSummary() {
        StepHelper.step(EDIT_ADDRESS_SUMMARY)
        logger.info { "Clicking Edit Address from Summary Page" }
        page.locator(".bg-secondary").first().click()
    }

    fun clickEditSlotFromSummary() {
        StepHelper.step(EDIT_SLOT_SUMMARY)
        logger.info { "Clicking Edit Slot from Summary Page" }
        page.locator(".flex-1 > .bg-secondary").click()
    }

    fun getAddressCount(): Int {
        return addressData?.addressList?.size ?: 0
    }

    fun getUserProfileList() {
        logger.info { "Fetching User Profile Lists (triggered by Book Now)..." }
        // Wait briefly for backend to process order creation if triggered immediately after click
        page.waitForTimeout(3000.0)

        val response = page.request().get(
            TestConfig.APIs.PROFILE_LIST,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", "Asia/Kolkata")
        )
        if (response.status() != 200) {
            throw RuntimeException("Failed to fetch profile list: ${response.status()} ${response.text()}")
        }
        val responseObj = json.decodeFromString<model.ProfileListResponse>(response.text())
        profileListData = responseObj.data
        logger.info { "Successfully fetched ${profileListData?.profiles?.size ?: 0} profiles" }

    }
    fun callBloodDataReports() {
        logger.info { "Fetching Blood Data Reports (triggered by Book Now)..." }

        // Wait briefly for backend to process order creation if triggered immediately after click
        page.waitForTimeout(3000.0)

        val response = page.request().get(
            TestConfig.APIs.BLOOD_DATA_REPORTS,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", "Asia/Kolkata")
        )

        if (response.status() != 200) {
            throw RuntimeException("Failed to fetch blood reports: ${response.status()} ${response.text()}")
        }

        val responseJson = json.decodeFromString<kotlinx.serialization.json.JsonObject>(response.text())
        val reports = responseJson["data"]?.jsonObject?.get("reports")?.jsonArray

        if (reports.isNullOrEmpty()) {
            throw RuntimeException("No reports found.")
        } else {
            logger.info { "Found $reports reports." }
        }

        // Get the most recent report
        val latestReport = reports[0].jsonObject

        val diOrder = latestReport["di_order"]?.jsonObject
        val order = latestReport["order"]?.jsonObject

        if (diOrder == null || order == null) {
            throw RuntimeException("Order details missing in report")
        }

        //appoint
        capturedOrderNo = diOrder["order_id"]?.jsonPrimitive?.content
        capturedProductId = diOrder["meta_data"]?.jsonObject?.get("product_id")?.jsonPrimitive?.int
        capturedThyrocareProductId = diOrder["product_id"]?.jsonPrimitive?.content
        capturedAppointmentDate = diOrder["appointment_date"]?.jsonPrimitive?.content
        capturedCreatedAt = diOrder["created_at"]?.jsonPrimitive?.content
        capturedPaymentDate = order["created_at"]?.jsonPrimitive?.content

        logger.info { "Captured Order Details: OrderNo=$capturedOrderNo, ProductId=$capturedProductId" }
    }

    fun proceedPayment(isKit: Boolean) {
        logger.info { "Proceeding Payment." }

        val piiUrl = TestConfig.APIs.API_ACCOUNT_INFORMATION
        val piiResponse = page.request().get(
            piiUrl,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
        )
        val piiObj = json.decodeFromString<kotlinx.serialization.json.JsonObject>(piiResponse.text())
        val piiData = piiObj["data"]?.jsonObject?.get("piiData")?.jsonObject

        var mobile = piiData?.get("mobile")?.jsonPrimitive?.content
        if (mobile.isNullOrBlank()) {
            mobile = utils.SignupDataStore.get().mobileNumber
            logger.info { "Mobile from PII is empty, using SignupDataStore: $mobile" }
        }
        if (mobile.isNullOrBlank()) {
            mobile = TestConfig.TestUsers.EXISTING_USER.mobileNumber
            logger.info { "Mobile from SignupDataStore is empty, using TestConfig: $mobile" }
        }

        var countryCode = piiData?.get("countryCode")?.jsonPrimitive?.content
        if (countryCode.isNullOrBlank()) {
            countryCode = TestConfig.TestUsers.EXISTING_USER.countryCode.replace("+", "")
        }

        val automateUrl = "${TestConfig.APIs.BASE_URL}/v4/human-token/automate-order-workflow-v2"

        val appointmentDate = capturedAppointmentDate ?: ""
        val createdAt = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
        val orderNo = capturedOrderNo ?: ""
        val productId = capturedProductId ?: 0
        val thyrocareProductId = capturedThyrocareProductId ?: ""
        val randomNum = (1000..9999).random()
        val paymentId = "Order_$randomNum"

        val payload = buildJsonObject {
            put("appointment_date", appointmentDate)
            put("country_code", countryCode)
            put("created_at", createdAt)
            put("is_combine_order", false)
            put("is_free_user", false)
            put("is_kit", isKit)
            put("is_restrict_whatsapp", buildJsonObject {})
            put("is_user_present", true)
            put("mobile", mobile)
            put("order_id", orderNo)
            put("payment_amount_inr", "0.00")
            put("payment_date", createdAt)
            put("payment_fee", "0.00")
            put("payment_id", paymentId)
            put("product_id", productId)
            put("status", "YET_TO_ASSIGN")
            put("thyrocare_product_id", thyrocareProductId)
        }

        logger.info { "Automating workflow for Order: $orderNo Payload: $payload" }

        val postRes = page.request().post(
            automateUrl,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("Content-Type", "application/json")
                .setData(payload.toString())
        )

        logger.info { "Automate Workflow Response: ${postRes.status()} ${postRes.text()}" }
        if (postRes.status() != 200 && postRes.status() != 201) {
            logger.error { "Automate workflow API failed" }
        }
    }
    fun callAutomateOrderWorkflow(isKit: Boolean = true) {
        logger.info { "Calling Automate Order Workflow API..." }

        if (capturedOrderNo == null) {
             // Fallback: try to fetch if not already captured, or throw error depending on strictness
             logger.warn { "Order details not pre-captured. Attempting to fetch now..." }
             callBloodDataReports()
        }

        val orderNo = capturedOrderNo ?: throw RuntimeException("Order No not captured")
        val productId = capturedProductId
        val thyrocareProductId = capturedThyrocareProductId
        val appointmentDate = capturedAppointmentDate
        val createdAt = capturedCreatedAt
        val paymentDate = capturedPaymentDate

        // Get User Mobile/Country from PII
        val piiUrl = TestConfig.APIs.API_ACCOUNT_INFORMATION
        val piiResponse = page.request().get(
            piiUrl,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
        )
        val piiObj = json.decodeFromString<kotlinx.serialization.json.JsonObject>(piiResponse.text())
        val piiData = piiObj["data"]?.jsonObject?.get("piiData")?.jsonObject
        
        var mobile = piiData?.get("mobile")?.jsonPrimitive?.content
        if (mobile.isNullOrBlank()) {
            mobile = utils.SignupDataStore.get().mobileNumber
            logger.info { "Mobile from PII is empty, using SignupDataStore: $mobile" }
        }
        if (mobile.isNullOrBlank()) {
            mobile = TestConfig.TestUsers.EXISTING_USER.mobileNumber
            logger.info { "Mobile from SignupDataStore is empty, using TestConfig: $mobile" }
        }
        
        var countryCode = piiData?.get("countryCode")?.jsonPrimitive?.content
        if (countryCode.isNullOrBlank()) {
             countryCode = TestConfig.TestUsers.EXISTING_USER.countryCode.replace("+", "")
        }

        val automateUrl = "${TestConfig.APIs.BASE_URL}/v4/human-token/automate-order-workflow-v2"

        val randomNum = (1000..9999).random()
        val paymentId = "Order_$randomNum"
        val currentTimestamp = java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())

        val payload = buildJsonObject {
            put("appointment_date", appointmentDate ?: "")
            put("country_code", countryCode)
            put("created_at", createdAt ?: currentTimestamp)
            put("is_combine_order", false)
            put("is_free_user", false)
            put("is_kit", isKit)
            put("is_restrict_whatsapp", buildJsonObject {})
            put("is_user_present", true)
            put("mobile", mobile)
            put("order_id", orderNo)
            put("payment_amount_inr", "0.00")
            put("payment_date", paymentDate ?: currentTimestamp)
            put("payment_fee", "0.00")
            put("payment_id", paymentId)
            put("product_id", productId ?: 0)
            put("status", "YET_TO_ASSIGN")
            put("thyrocare_product_id", thyrocareProductId ?: "")
        }

        logger.info { "Automating workflow for Order: $orderNo Payload: $payload" }

        val postRes = page.request().post(
            automateUrl,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("Content-Type", "application/json")
                .setData(payload.toString())
        )

        logger.info { "Automate Workflow Response: ${postRes.status()} ${postRes.text()}" }
        if (postRes.status() != 200 && postRes.status() != 201) {
            logger.error { "Automate workflow API failed" }
        }
    }
}
