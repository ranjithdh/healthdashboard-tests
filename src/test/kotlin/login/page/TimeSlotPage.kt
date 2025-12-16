package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class TimeSlotPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    fun selectDate(day: String): TimeSlotPage {
        logger.info { "selectDate($day)" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Select Date")).click()
        byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).click()
        return this
    }

    fun selectSlot(slotName: String, index: Int = 0): TimeSlotPage {
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
        return selectSlot(":00 - 08:30 AM", 0)
    }

    fun selectSecondSlot(): TimeSlotPage {
        return selectSlot(":00 - 10:30 AM", 1)
    }

    fun clickSchedule(): TimeSlotPage {
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Schedule")).click()
        return this
    }

    fun selectSlotsAndContinue(): OrderSummaryPage {
        selectMorningSlot()
        selectSecondSlot()
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

    fun isTimeSlotVisible(slotName: String = ":00 - 08:30 AM"): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(slotName)).first().isVisible
    }

    fun hasAvailableSlots(): Boolean {
        return byText("AM").first().isVisible || byText("PM").first().isVisible
    }
}
