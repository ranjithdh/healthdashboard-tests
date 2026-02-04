package login.page

import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import model.addontest.AddOnTests
import model.addontest.OnboardDiagnosticProductList
import model.slot.SlotData
import model.slot.SlotList
import utils.OnboardAddOnTestDataStore
import utils.json.json
import utils.logger.logger
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_SCHEDULE_BUTTON
import utils.report.StepHelper.CLICK_SLOT
import utils.report.StepHelper.CLOSE_INVALID_FASTING_SLOT_POPUP
import utils.report.StepHelper.OPEN_DATE_PICKER
import utils.report.StepHelper.SELECT_CALENDAR_DATE
import utils.report.StepHelper.SELECT_DATE
import utils.report.StepHelper.SELECT_DATE_FILTER
import utils.report.StepHelper.SELECT_DATE_VIEW
import utils.report.StepHelper.SELECT_MORNING_SLOT
import utils.report.StepHelper.SELECT_POST_MEAL_SLOT
import utils.report.StepHelper.SELECT_SLOT
import utils.report.StepHelper.SELECT_SLOTS_CONTINUE
import java.time.LocalDateTime
import java.util.Locale


class TimeSlotPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    private var slotData = SlotData()


    init {
        getSlotList()
    }

    fun getSlotList() {
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()
                    ?.contains("https://api.stg.dh.deepholistics.com/v3/diagnostics/slots-availability?platform=web") == true && response.status() == 200
            },
            {
                byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Schedule your Blood Test")).waitFor()
            }
        )

        val responseBody = response.text()
        if (responseBody.isNullOrBlank()) {
            logger.info { "API response body is empty" }
        }

        logger.info { "API response...${response.text()}" }

        try {
            val responseObj = json.decodeFromString<SlotList>(responseBody)
            logger.error { "responseObj...$responseObj" }

            if (responseObj.data != null) {
                slotData = responseObj.data
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse API response..${e.message}" }
        }
    }


    fun selectDate(day: String): TimeSlotPage {
        StepHelper.step(SELECT_DATE + day)
        logger.info { "selectDate($day)" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Select Date")).click()
        byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).click()
        return this
    }

    fun selectSlot(slotName: String, index: Int = 0): TimeSlotPage {
        StepHelper.step(SELECT_SLOT + slotName + " at index " + index)
        logger.info { "selectSlot($slotName, index: $index)" }
        val slot = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(slotName))
        if (index == 0) {
            slot.first().click()
        } else {
            slot.nth(index).click()
        }
        return this
    }

    fun selectMorningSlot(): TimeSlotPage {
        StepHelper.step(SELECT_MORNING_SLOT)
        val availableSlots = getAvailableFastingSlots()
        if (availableSlots.isNotEmpty()) {
            val slotName = availableSlots[0]
            logger.info { "selectMorningSlot: $slotName" }
            val split = slotName.split("-")
            utils.SignupDataStore.update { fastingSlot = split.first() }
            return selectSlot(slotName, 0)
        } else {
            logger.warn { "No available morning slots found from API" }
            return this
        }
    }

    fun selectPostMealSlot(): TimeSlotPage {
        StepHelper.step(SELECT_POST_MEAL_SLOT)
        val availableSlots = getAvailableFastingSlots()
        if (availableSlots.isNotEmpty()) {
            val firstFastingSlot = availableSlots[0]
            val expectedPostMealSlots = getExpectedPostMealSlots(firstFastingSlot)

            if (expectedPostMealSlots.isNotEmpty()) {
                val slotName = expectedPostMealSlots[0]
                logger.info { "selectPostMealSlot: $slotName" }
                // Post meal slots are in the second section, so we use index 1
                // (Assuming Playwright finds multiple buttons with same name if times overlap, 
                // or just to target the second grid if structure allows, 
                // but strictly based on isPostMealSlotVisible using nth(1), we use index 1 here)
                return selectSlot(slotName, 1)
            }
        }
        logger.warn { "No available post meal slots found or could not calculate" }
        return this
    }

    fun clickSchedule() {
        StepHelper.step(CLICK_SCHEDULE_BUTTON)
        val response = page.waitForResponse(
            { response: Response? ->
                response?.url()?.contains(
                    "https://api.stg.dh.deepholistics.com/v4/human-token/diagnostics/onboarding-addon?show_onboarding_addon=true"
                ) == true
            },
            {
                byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Schedule")).click()
            }
        )
        parseAddOnTest(response.text())
    }

    fun parseAddOnTest(response: String) {
        try {
            val result = json.decodeFromString<AddOnTests>(response)
            logger.error { "getAddOnTestList...$result" }

            val diagnosticProductList = result.diagnostic_product_list

            OnboardAddOnTestDataStore.update {
                this.tests = diagnosticProductList?.tests ?: emptyList()
                this.test_profiles = diagnosticProductList?.test_profiles ?: emptyList()
                this.packages = diagnosticProductList?.packages ?: emptyList()
            }

        } catch (e: Exception) {
            logger.error { "getAddOnTestList....Failed to parse API response..${e.message}" }
        }
    }


    fun selectSlotsAndContinue(): OrderSummaryPage {
        StepHelper.step(SELECT_SLOTS_CONTINUE)
        val currentDate = LocalDateTime.now().plusDays(1)
        selectDateView(
            currentDate.dayOfMonth.toString(),
        )

        utils.SignupDataStore.update { slotDate = currentDate }

        selectMorningSlot()
        selectPostMealSlot()
        clickSchedule()
        val orderSummaryPage = OrderSummaryPage(page)
        orderSummaryPage.waitForConfirmation()
        return orderSummaryPage
    }

    fun waitForConfirmation(): TimeSlotPage {
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Schedule your Blood Test")).waitFor()
        return this
    }


    fun isScheduleHeaderVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Schedule your Blood Test")).isVisible
    }

    fun isAddressSectionVisible(): Boolean {
        return byText("Your sample collection").isVisible
    }

    fun isFastingSlotsHeaderVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Fasting Test Slots")).isVisible
    }

    fun isPostMealSlotsVisible(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Post Meal Test Slots")).isVisible
    }

    fun isFastingInstructionVisible(): Boolean {
        return byText("hours of fasting required").first().isVisible
    }

    fun isPostMealInstructionVisible(): Boolean {
        return byText("hours post meal required").first().isVisible
    }

    fun isTimeSlotVisible(slotName: String? = null): Boolean {
        val targetSlot = slotName ?: getAvailableFastingSlots().firstOrNull() ?: return false
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(targetSlot)).first().isVisible
    }

    fun isPostMealSlotVisible(slotName: String): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(slotName)).nth(1).isVisible
    }

    fun hasAvailableSlots(): Boolean {
        val availableSlots = getAvailableFastingSlots()
        return availableSlots.isNotEmpty() && isTimeSlotVisible(availableSlots[0])
    }

    fun clickSlot(slotName: String): TimeSlotPage {
        StepHelper.step(CLICK_SLOT + slotName)
        logger.info { "clickSlot($slotName)" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(slotName)).click()
        return this
    }

    fun isInvalidFastingSlotPopupVisible(): Boolean {
        return byText("Invalid Fasting Slot").isVisible
    }

    fun closeInvalidFastingSlotPopup(): TimeSlotPage {
        StepHelper.step(CLOSE_INVALID_FASTING_SLOT_POPUP)
        logger.info { "closeInvalidFastingSlotPopup()" }
        byText("Invalid Fasting Slot").click()
        return this
    }

    fun selectDateByFilter(dateText: String): TimeSlotPage {
        StepHelper.step(SELECT_DATE_FILTER + dateText)
        logger.info { "selectDateByFilter($dateText)" }
        byRole(AriaRole.BUTTON).filter(FilterOptions().setHasText("Select Date")).click()
        byText(dateText).click()
        return this
    }

    fun formatSlotLabel(start: String, end: String): String {
        val startDateTime = utils.DateHelper.utcToLocalDateTime(start)
        val endDateTime = utils.DateHelper.utcToLocalDateTime(end)

        val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a",Locale.ENGLISH)
        val startStr = startDateTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm",Locale.ENGLISH))
        val endStr = endDateTime.format(formatter) // 05:30 AM

        return "$startStr - $endStr"
    }

    fun getAvailableFastingSlots(): List<String> {
        return slotData.slots?.filter { it.is_available == true }
            ?.map { formatSlotLabel(it.start_time!!, it.end_time!!) }
            ?: emptyList()
    }

    fun getUnavailableFastingSlots(): List<String> {
        return slotData.slots?.filter { it.is_available == false }
            ?.map { formatSlotLabel(it.start_time!!, it.end_time!!) }
            ?: emptyList()
    }

    fun getFastingSlotsStartingAfter(hour: Int, minute: Int): List<String> {
        val cutoffTime = java.time.LocalTime.of(hour, minute)
        return slotData.slots?.filter {
            val startTime = utils.DateHelper.utcToLocalDateTime(it.start_time).toLocalTime()
            it.is_available == true && startTime.isAfter(cutoffTime)
        }?.map { formatSlotLabel(it.start_time!!, it.end_time!!) }
            ?: emptyList()
    }

    fun validateFastingSlotDurations(expectedMinutes: Long): Boolean {
        val cutoffTime = java.time.LocalTime.of(11, 30)
        
        return slotData.slots?.filter {
            val startTime = utils.DateHelper.utcToLocalDateTime(it.start_time).toLocalTime()
            it.is_available == true && !startTime.isAfter(cutoffTime)
        }?.all {
            val start = utils.DateHelper.utcToLocalDateTime(it.start_time)
            val end = utils.DateHelper.utcToLocalDateTime(it.end_time)
            java.time.Duration.between(start, end).toMinutes() == expectedMinutes
        } ?: true
    }

    fun selectFirstAvailableFastingSlot(): String {
        val slots = slotData.slots?.filter { it.is_available == true }
        if (slots.isNullOrEmpty()) {
            throw RuntimeException("No available fasting slots found in API response")
        }
        val slot = slots.first()
        val label = formatSlotLabel(slot.start_time!!, slot.end_time!!)
        logger.info { "Selecting first available slot: $label" }
        clickSlot(label)
        return label
    }

    private fun formatTimeRange(start: java.time.LocalDateTime, end: java.time.LocalDateTime): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val startStr = start.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm",Locale.ENGLISH))
        val endStr = end.format(formatter)
        return "$startStr - $endStr"
    }

    fun getExpectedPostMealSlots(selectedFastingSlotStartTime: String): List<String> {
        val selectedSlot = slotData.slots?.find {
            formatSlotLabel(it.start_time!!, it.end_time!!) == selectedFastingSlotStartTime 
        } ?: return emptyList()

        val startDateTime = utils.DateHelper.utcToLocalDateTime(selectedSlot.start_time)
        var currentSlotStart = startDateTime.plusHours(2)
        
        val generatedSlots = mutableListOf<String>()
        repeat(5) {
            val currentSlotEnd = currentSlotStart.plusMinutes(30)
            generatedSlots.add(formatTimeRange(currentSlotStart, currentSlotEnd))
            currentSlotStart = currentSlotEnd
        }
        return generatedSlots
    }

    fun openDatePicker(): TimeSlotPage {
        StepHelper.step(OPEN_DATE_PICKER)
        logger.info { "openDatePicker()" }
        byRole(AriaRole.BUTTON).filter(FilterOptions().setHasText("Select Date")).click()
        return this
    }

    fun selectCalendarDate(day: String): TimeSlotPage {
        StepHelper.step(SELECT_CALENDAR_DATE + day)
        logger.info { "selectCalendarDate($day)" }
        byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).first().click()
        return this
    }

    fun isCalendarDateEnabled(day: String): Boolean {
        return byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).first().isEnabled
    }

    fun isSelectedDateTextVisible(dateText: String): Boolean {
        return byText(dateText).isVisible
    }

    fun selectDateView(day: String): TimeSlotPage {
        StepHelper.step(SELECT_DATE_VIEW + day)
        logger.info { "selectDateView($day)" }
        page.getByText(day, Page.GetByTextOptions().setExact(true)).first().click()
        return this
    }

    fun isSelectedDateTextVisibleByExact(dateText: String): Boolean {
        return page.getByText(dateText, Page.GetByTextOptions().setExact(true)).isVisible
    }
}


