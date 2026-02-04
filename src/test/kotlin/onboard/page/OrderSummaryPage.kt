package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.RequestOptions
import config.BasePage
import config.TestConfig
import io.qameta.allure.Step
import kotlinx.serialization.json.*
import mobileView.home.HomePage
import utils.DateHelper
import utils.SignupDataStore
import utils.json.json
import utils.logger.logger
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import io.qameta.allure.Step


class OrderSummaryPage(page: Page) : BasePage(page) {

    override val pageUrl = TestConfig.Urls.LOGIN_URL

    private var orderSummaryData: JsonObject? = null

    init {
        monitorTraffic()
    }



    private fun monitorTraffic() {
        page.onResponse { response ->
            if (response.url().contains("https://api.stg.dh.deepholistics.com/v4/human-token/diagnostics/onboarding-addon?show_onboarding_addon=true") && response.status() == 200) {
                try {
                    val responseBody = response.text()
                    if (!responseBody.isNullOrBlank()) {
                        val responseObj = json.decodeFromString<JsonObject>(responseBody)
                        orderSummaryData = responseObj["data"]?.jsonObject
                        logger.info { "Order Summary Data Captured: $orderSummaryData" }
                    }
                } catch (e: Exception) {
                    logger.warn { "Failed to parse order summary response: ${e.message}" }
                }
            }
        }
    }

    @Step("Enter Coupon Code: {code}")
    fun enterCouponCode(code: String): OrderSummaryPage {
        logger.info { "enterCouponCode($code)" }
        if (!isCouponInputVisible()) {
            byText("Have a referral/ coupon code").click()
        }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).fill(code)
        return this
    }

    @Step("Click Apply Coupon")
    fun clickApplyCoupon(): OrderSummaryPage {
        logger.info { "clickApplyCoupon()" }
        val button = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Apply"))
        if (button.isVisible) {
            button.click()
        } else {
            byText("Apply").click()
        }
        return this
    }

    @Step("Clear Coupon Code")
    fun clearCouponCode(): OrderSummaryPage {
        logger.info { "clearCouponCode()" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).clear()
        return this
    }

    @Step("Click Checkout")
    fun clickCheckout(): HomePage {
        logger.info { "clickCheckout()" }
        page.getByRole(AriaRole.BUTTON,Page.GetByRoleOptions().setName("Checkout")).click()

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }


    fun waitForConfirmation(): OrderSummaryPage {
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Order summary")).waitFor()
        return this
    }

    fun isOrderSummaryHeaderVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Order summary")).isVisible
    }

    fun isCouponInputVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter code")).isVisible
    }

    fun isProductNameVisible(productName: String = "Baseline Test"): Boolean {
        return byText(productName).isVisible
    }

    fun isTotalVisible(): Boolean {
        return page.getByText("Total", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isInvalidCouponErrorVisible(): Boolean {
        return byText("Invalid Referral Code").isVisible
    }

    fun isCouponAppliedSuccessVisible(): Boolean {
        return byText("Offer has been successfully").isVisible
    }

    fun isCouponCodeAppliedVisible(code: String): Boolean {
        return page.locator("div")
            .filter(
                com.microsoft.playwright.Locator.FilterOptions()
                    .setHasText(java.util.regex.Pattern.compile("^$code applied$"))
            )
            .nth(1)
            .isVisible
    }

    @Step("Remove Coupon")
    fun removeCoupon(): OrderSummaryPage {
        logger.info { "removeCoupon()" }
        byRole(AriaRole.IMG).nth(3).click()
        return this
    }

    fun isTotalAmountVisible(amount: String): Boolean {
        return page.locator("span.text-xl").getByText(amount).isVisible
    }

    fun isCouponValueVisible(value: String): Boolean {
        return byText(value).isVisible
    }

    private val firstTest = page.getByTestId("addon-card-add-button-11")
    private val secondTest = page.getByTestId("addon-card-add-button-12")
    private val thirdTest = page.getByTestId("addon-card-add-button-4")
    private val fourthTest = page.getByTestId("addon-card-add-button-34")

    private val removeFirstTest = page.getByTestId("selected-addon-remove-11")
    private val removeSecondTest = page.getByTestId("selected-addon-remove-12")
    private val removeThirdTest = page.getByTestId("selected-addon-remove-4")
    private val removeFourthTest = page.getByTestId("selected-addon-remove-34")

    private val firstTestName = page.getByTestId("selected-addon-name-11")
    private val secondTestName = page.getByTestId("selected-addon-name-12")
    private val thirdTestName = page.getByTestId("selected-addon-name-4")
    private val fourthTestName = page.getByTestId("selected-addon-name-34")

    private val totalAmount = page.getByText("Total₹9,999", Page.GetByTextOptions().setExact(true))


    @Step("Add All Add-on Tests")
    fun addAllTheAddOnTests() {
        firstTest?.click()
        secondTest?.click()
        thirdTest?.click()
        fourthTest?.click()
    }

    @Step("Remove All Add-on Tests")
    fun removeAllTheAddOnTests() {
        removeFirstTest?.click()
        removeSecondTest?.click()
        removeThirdTest?.click()
        removeFourthTest?.click()
    }

    @Step("Add First Add-on")
    fun addFirstAddOn() {
        firstTest.click()
    }

    @Step("Add Second Add-on")
    fun addSecondAddOn() {
        secondTest.click()
    }

    fun addThirdAddOn() {
        thirdTest.click()
    }

    fun addFourthAddOn() {
        fourthTest.click()
    }

    fun removeFirstAddOn() {
        removeFirstTest.click()
    }

    fun removeSecondAddOn() {
        removeSecondTest.click()
    }

    fun removeThirdAddOn() {
        removeThirdTest.click()
    }

    fun removeFourthAddOn() {
        removeFourthTest.click()
    }

    fun getFirstAddOnName(): String {
        return firstTestName.innerText()
    }

    fun getSecondAddOnName(): String {
        return secondTestName.innerText()
    }

    fun getThirdAddOnName(): String {
        return thirdTestName.innerText()
    }

    fun getFourthAddOnName(): String {
        return fourthTestName.innerText()
    }

    @Step("Automate Order Workflow to Skip Razorpay")
    fun automateOrderWorkflow(isAddOn: Boolean = false): HomePage {
        logger.info { "automateOrderWorkflow(isAddOn=$isAddOn)" }

        val signupData = SignupDataStore.get()
        val fastingSlotTime = signupData.fastingSlot?.split(":")

        val savedLocalDate = signupData.slotDate?.withHour(fastingSlotTime?.first()?.trim()?.toInt() ?: 0)
            ?.withMinute(fastingSlotTime?.last()?.trim()?.toInt() ?: 0)?.withSecond(0)

        val appointmentUtcTime = DateHelper.localDateTimeToUtc(savedLocalDate)
        val utcNow = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

        val payload = buildJsonObject {
            put("is_user_present", false)

            if (isAddOn) {
                val productIds = mutableListOf<Int>()
                val thyrocareProductIds = mutableListOf<String>()
                val orderIds = mutableListOf<String>()

                productIds.add(103)
                thyrocareProductIds.add("DH_LONGEVITY_PANEL")
                orderIds.add("VL192030")

                signupData.selectedAddOns.forEach { name ->
                    val lowerName = name.lowercase()
                    when {
                        lowerName.contains("omega") -> {
                            productIds.add(108)
                            thyrocareProductIds.add("OMEGA1003")
                            orderIds.add("VL192031")
                        }
                        lowerName.contains("cortisol") -> {
                            productIds.add(110)
                            thyrocareProductIds.add("CORTISOL1004")
                            orderIds.add("VL192029")
                        }
                        lowerName.contains("toxic metal") -> {
                            productIds.add(91)
                            thyrocareProductIds.add("P250")
                            orderIds.add("VL192032")
                        }
                        lowerName.contains("allergy") -> {
                            productIds.add(92)
                            thyrocareProductIds.add("TTGA")
                            orderIds.add("VL192028")
                        }
                    }
                }

                putJsonArray("product_id") { productIds.forEach { add(it) } }
                putJsonArray("thyrocare_product_id") { thyrocareProductIds.forEach { add(it) } }
                putJsonArray("order_id") { orderIds.forEach { add(it) } }
                putJsonArray("appointment_date") { productIds.forEach { _ -> add(appointmentUtcTime) } }
                putJsonArray("is_kit") { productIds.forEach { _ -> add(false) } }
                putJsonArray("status") { productIds.forEach { _ -> add("YET_TO_ASSIGN") } }
            } else {
                put("product_id", 103)
                put("thyrocare_product_id", "DH_LONGEVITY_PANEL")
                put("order_id", "VL192029")
                put("appointment_date", appointmentUtcTime)
                put("is_kit", false)
                put("status", "YET_TO_ASSIGN")
            }

            put("payment_id", "pay_lipnoo")
            put("payment_date", utcNow)
            put("payment_amount_inr", "0.00")
            put("payment_fee", "0.00")
            put("is_free_user", false)
            put("created_at", utcNow)
            put("is_combine_order", false)
            putJsonObject("is_restrict_whatsapp") {}
            put("mobile", signupData.mobileNumber ?: "9876543210")
            put("country_code", "91")
            put("email", signupData.email ?: "user@example.com")
            put("first_name", signupData.firstName ?: "John")
            put("last_name", signupData.lastName ?: "Doe")
            put("gender", (signupData.gender ?: "male").lowercase())

            val dobDate = ZonedDateTime.of(
                signupData.year?.toInt() ?: 1990,
                signupData.month?.toInt() ?: 1,
                signupData.day?.toInt() ?: 15,
                0, 0, 0, 0, ZoneId.of("UTC")
            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
            put("dob", dobDate)

            put("height", signupData.height?.toIntOrNull() ?: 175)
            put("weight", signupData.weight?.toIntOrNull() ?: 70)
            put("ref_source", "automation")
            put("is_created_by_admin", true)

            putJsonObject("communication_address") {
                put("address_line_1", signupData.address ?: "123 Main Street")
                put("address_line_2", "Apt 4B")
                put("address_house_no", signupData.flatHouseNoOrBuilding ?: "123")
                put("city", signupData.city ?: "Mumbai")
                put("state", signupData.state ?: "Maharashtra")
                put("pincode", signupData.pinCode ?: "400001")
                put("country", "India")
            }
            putJsonObject("billing_address") {
                put("address_line_1", signupData.address ?: "123 Main Street")
                put("address_line_2", "Apt 4B")
                put("city", signupData.city ?: "Mumbai")
                put("state", signupData.state ?: "Maharashtra")
                put("pincode", signupData.pinCode ?: "400001")
                put("country", "India")
            }
        }

        logger.info { "Automation API Payload: $payload" }

        val response = page.context().request().post(
            "https://api.stg.dh.deepholistics.com/v4/human-token/automate-order-workflow-v2",
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setData(payload.toString())
        )

        logger.info { "Automation API -- status: ${response.status()}" }
        logger.info { "Automation API -- response text: ${response.text()}" }


        if (response.status() != 200 && response.status() != 201) {
            logger.warn { "Automation API failed with status ${response.status()}: ${response.text()}" }
        }

        page.navigate(TestConfig.Urls.HOME_PAGE_URL)
        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()
        return homePage
    }
}
