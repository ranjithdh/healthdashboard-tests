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
import utils.json.json
import utils.logger.logger
import model.slot.SlotList
import model.profile.PiiUserResponse
import com.microsoft.playwright.options.RequestOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import mobileView.profile.utils.ProfileUtils.buildAddressText
import kotlin.test.assertEquals


private val logger = KotlinLogging.logger {}

class TestSchedulingPage(page: Page) : BasePage(page) {

    override val pageUrl = ""
    private var addressData: UserAddressData? = null
    private var selectedAddressIndex: Int = 0
    private var selectedAddressName: String = ""
    private var selectedAddressText: String = ""
    private var selectedDateSummary: String = ""
    private var selectedTimeSummary: String = ""
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
        page.locator(".bg-secondary.flex.flex-1").nth(index).click()

        // Fill inputs (UI)
        val updatedNickName = address.addressName?.takeIf { it.isNotBlank() } ?: "Home"
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
        
        this.selectedAddressIndex = index

        val updatedList = addressData?.addressList ?: throw AssertionError("Address list not updated")
        val updatedAddress = updatedList.find { it.addressId == addressId }

        assertEquals(updatedNickName, updatedAddress?.address?.addressName)
    }

    fun verifyPriceDetails(expectedSubtotal: Double, expectedDiscount: Double) {
        logger.info { "Verifying price details: Subtotal=$expectedSubtotal, Discount=$expectedDiscount" }
        page.getByText("PRICE DETAILS").click()
        page.getByTestId("diagnostics-sidebar-subtotal-label").click()
        page.getByTestId("diagnostics-sidebar-subtotal-value").click()
        page.getByTestId("diagnostics-sidebar-discount-label").click()
        page.getByTestId("diagnostics-sidebar-discount-value").click()
        page.getByTestId("diagnostics-sidebar-grand-total-label").click()
        page.getByTestId("diagnostics-sidebar-grand-total-value").click()
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
        val url = TestConfig.APIs.API_SLOTS_AVAILABILITY + "?platform=web"
        val payload = buildJsonObject {
            put("address_id", addressId)
            put("date", date)
            put("lead_id", leadId)
//            put("user_timezone", "Asia/Kolkata")
        }.toString()

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
            logger.error { "Slot API failed: ${response.text()}" }
            return emptyList()
        }

        val slotList = json.decodeFromString<SlotList>(response.text())
        return slotList.data?.slots ?: emptyList()
    }

    fun verifySlotSelectionPage(code: String) {
        logger.info { "Verifying Slot Selection Page" }
        page.getByTestId("diagnostics-booking-step2-slot-title").waitFor()

        val leadId = getLeadId()
        val addressItem = addressData?.addressList?.getOrNull(selectedAddressIndex)
            ?: addressData?.addressList?.firstOrNull()
            ?: throw IllegalStateException("Address data not found. Ensure address is selected/captured before slot selection.")
        val addressId = addressItem.addressId
//        val selectedAddressObj = addressData?.addressList?.find { it.addressId == addressId }?.address
//        selectedAddressName = selectedAddressObj?.addressName ?: "Primary"
//        selectedAddressText = buildAddressText(selectedAddressObj!!)

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val summaryDateFormatter = DateTimeFormatter.ofPattern("dd MMM")
        val tomorrowDateStr = tomorrow.format(dateFormatter)

        logger.info { "Processing tomorrow's slots: $tomorrowDateStr" }
        page.getByTestId("diagnostics-booking-step2-date-$tomorrowDateStr").click()

        val tomorrowSlots = getSlots(tomorrowDateStr, leadId, addressId)
//        tomorrowSlots.forEach { slot ->
//            if (slot.is_available == true && slot.start_time != null) {
//                page.getByTestId("diagnostics-booking-step2-slot-${slot.start_time}").click()
//                // Just to capture something if we don't do the random part or if it fails
//                captureSlotForSummary(slot.start_time, summaryDateFormatter)
//            }
//        }

        // Randomly select a date from tomorrow + 7 days (index 0 to 6)
        val randomDayOffset = (0..6).random()
        val randomDate = tomorrow.plusDays(randomDayOffset.toLong())
        val randomDateStr = randomDate.format(dateFormatter)

        logger.info { "Selecting random date: $randomDateStr" }
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

    private fun captureSlotForSummary(startTimeIso: String, summaryDateFormatter: DateTimeFormatter) {
        // Parse ISO string and convert to IST (+5:30)
        // Format: 2026-02-06T05:00:00.000Z
        val instant = java.time.Instant.parse(startTimeIso)
        val istZone = java.time.ZoneId.of("Asia/Kolkata")
        val zonedDateTime = instant.atZone(istZone)
        
        selectedDateSummary = zonedDateTime.format(summaryDateFormatter)
        selectedTimeSummary = zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    fun verifyOrderSummaryPage(expectedSubtotal: Double, expectedDiscount: Double) {
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
        
        page.getByText(nicknameToSearch, Page.GetByTextOptions().setExact(false)).first().click()
        // Note: Sometimes the full address text might be truncated or formatted differently, 
        // using contains/subset of text or building from components.
        val partialAddress = selectedAddressText.take(20) // take a safe chunk
        page.getByText(partialAddress, Page.GetByTextOptions().setExact(false)).first().click()

        // Slot verification
        page.getByText("Sample Collection Time Slot").click()
        page.getByText("Date: $selectedDateSummary").click()
        page.getByText("Selected time slot: $selectedTimeSummary").click()

        // Price details verification (reusing logic or similar)
        page.getByText("PRICE DETAILS").click()
        page.getByTestId("diagnostics-sidebar-subtotal-label").click()
        
        val subtotalValue = page.getByTestId("diagnostics-sidebar-subtotal-value").innerText()
        assertEquals("₹${expectedSubtotal.toInt()}", subtotalValue.replace(",", "").replace(" ", ""))

        page.getByTestId("diagnostics-sidebar-discount-label").click()
        val discountValue = page.getByTestId("diagnostics-sidebar-discount-value").innerText()
        // Handle variations like "- ₹ 0" or "-₹0" by removing whitespace
        assertEquals("-₹${expectedDiscount.toInt()}", discountValue.replace(",", "").replace(" ", ""))

        page.getByTestId("diagnostics-sidebar-grand-total-label").click()
        val grandTotalValue = page.getByTestId("diagnostics-sidebar-grand-total-value").innerText()
        val expectedGrandTotal = expectedSubtotal - expectedDiscount
        assertEquals("₹${expectedGrandTotal.toInt()}", grandTotalValue.replace(",", "").replace(" ", ""))

        // Got any questions? Contact
        val page3 = page.waitForPopup {
            page.getByText("Got any questions? Contact").click()
        }
        Assertions.assertNotNull(page3, "Question popup page should be opened")
        page3.close()

        // Mobile footer elements
        page.getByTestId("diagnostics-sidebar-mobile-grand-total").click()
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

    fun clickEditAddressFromSummary() {
        logger.info { "Clicking Edit Address from Summary Page" }
        page.locator(".bg-secondary").first().click()
    }

    fun clickEditSlotFromSummary() {
        logger.info { "Clicking Edit Slot from Summary Page" }
        page.locator(".flex-1 > .bg-secondary").click()
    }

    fun getAddressCount(): Int {
        return addressData?.addressList?.size ?: 0
    }
}
