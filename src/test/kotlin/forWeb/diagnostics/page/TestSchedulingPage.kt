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

import utils.LogFullApiCall.logFullApiCall
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
import utils.report.StepHelper.VERIFY_SLOT_SELECTION_PAGE
import kotlin.test.assertEquals
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
//import kotlinx.serialization.json.content


private val logger = KotlinLogging.logger {}

class TestSchedulingPage(page: Page) : BasePage(page) {

    override val pageUrl = ""
    private var addressData: UserAddressData? = null
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


        captureAddressData {
            newAddressSubmit.click()
        }

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

    fun verifyPriceDetails(expectedSubtotal: Double, expectedDiscount: Double) {
        StepHelper.step(VERIFY_PRICE_DETAILS)
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
        }

        // Get the most recent report
        val latestReport = reports[0].jsonObject

        val diOrder = latestReport["di_order"]?.jsonObject
        val order = latestReport["order"]?.jsonObject

        if (diOrder == null || order == null) {
            throw RuntimeException("Order details missing in report")
        }

        capturedOrderNo = diOrder["order_id"]?.jsonPrimitive?.content
        capturedProductId = diOrder["meta_data"]?.jsonObject?.get("product_id")?.jsonPrimitive?.int
        capturedThyrocareProductId = diOrder["product_id"]?.jsonPrimitive?.content
        capturedAppointmentDate = diOrder["appointment_date"]?.jsonPrimitive?.content
        capturedCreatedAt = diOrder["created_at"]?.jsonPrimitive?.content
        capturedPaymentDate = order["created_at"]?.jsonPrimitive?.content

        logger.info { "Captured Order Details: OrderNo=$capturedOrderNo, ProductId=$capturedProductId" }
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

        val paymentId = "order_RJURFxbJ6TYlyC"
// this RJURFxbJ6TYlyB will be dynamic
        val automateUrl = "${TestConfig.APIs.BASE_URL}/v4/human-token/automate-order-workflow-v2"

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
            put("payment_date", paymentDate)
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
}
