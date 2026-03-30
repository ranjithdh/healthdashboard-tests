package onboard.test.page

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import onboard.page.TimeSlotPage
import org.junit.jupiter.api.*
import utils.logger.logger


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_BOOKLABTEST)
class TimeSlotPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var timeSlotPage: TimeSlotPage

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())

        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        timeSlotPage = navigateToTimeSlotPage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun navigateToTimeSlotPage(): TimeSlotPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.ONBOARD_USER

        return loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
            .fillAddressDetails()
    }

    @Test
    fun `should display available time slots`() {
        assert(timeSlotPage.hasAvailableSlots()) { "Should have available slots" }
        timeSlotPage.takeScreenshot("time-slots-available")
    }


    @Test
    fun `should select morning slot successfully`() {
        timeSlotPage.selectMorningSlot()
        timeSlotPage.takeScreenshot("morning-slot-selected")
    }

    @Test
    fun `should select post meal slot successfully`() {
        timeSlotPage.selectMorningSlot()
        timeSlotPage.selectPostMealSlot()
        timeSlotPage.takeScreenshot("post-meal-slot-selected")
    }

    @Test
    fun `should select multiple slots`() {
        timeSlotPage
            .selectMorningSlot()
            .selectPostMealSlot()

        timeSlotPage.takeScreenshot("multiple-slots-selected")
    }

//    @Test
//    fun `should verify invalid fasting slot conditions`() {
//        val timeSlotPage = navigateToTimeSlotPage()
//
//        val lateSlots = timeSlotPage.getFastingSlotsStartingAfter(11, 30)
//
//        if (lateSlots.isNotEmpty()) {
//            val lateSlot = lateSlots.first()
//            println("Testing late slot: $lateSlot")
//            timeSlotPage.clickSlot(lateSlot)
//            assert(timeSlotPage.isInvalidFastingSlotPopupVisible()) { "Popup should appear for slot > 11:30 AM: $lateSlot" }
//            timeSlotPage.closeInvalidFastingSlotPopup()
//        }
//
//        val unavailableSlots = timeSlotPage.getUnavailableFastingSlots()
//        if (unavailableSlots.isNotEmpty()) {
//            val unavailableSlot = unavailableSlots.first()
//            println("Testing unavailable slot: $unavailableSlot")
//            timeSlotPage.clickSlot(unavailableSlot)
//            assert(timeSlotPage.isInvalidFastingSlotPopupVisible()) { "Popup should appear for unavailable slot: $unavailableSlot" }
//            timeSlotPage.closeInvalidFastingSlotPopup()
//        }
//    }

    @Test
    fun `should verify post meal slots match api data`() {
        val selectedSlotLabel = timeSlotPage.selectFirstAvailableFastingSlot()
        
        val expectedPostMealSlots = timeSlotPage.getExpectedPostMealSlots(selectedSlotLabel)

        assert(expectedPostMealSlots.isNotEmpty()) { "Should expect some post meal slots" }

        
        expectedPostMealSlots.forEach { expected ->
            logger.info("Testing post meal slot: $expected")
            assert(timeSlotPage.isPostMealSlotVisible(expected)) { "Expected post meal slot '$expected' not visible as a secondary slot (nth(1))" }
        }
        
        timeSlotPage.takeScreenshot("post-meal-slots-dynamic-verified")
    }

    @Test
    fun `should verify slot duration is 30 minutes`() {
        assert(timeSlotPage.validateFastingSlotDurations(30)) { 
            "Not all valid fasting slots (<= 11:30 AM) have a 30-minute duration" 
        }
        
        timeSlotPage.takeScreenshot("slot-duration-verified")
    }

    @Test
    fun `should verify date selection and limits in date picker dialog`() {
        timeSlotPage.openDatePicker()
        
        val today = java.time.LocalDate.now()
        
        for (i in 1..7) {
            val date = today.plusDays(i.toLong())
            val dayString = date.dayOfMonth.toString()
            val useIndex = dayString == "1" || dayString == "2"
            logger.info("Checking enabled status for day: $dayString")
            assert(timeSlotPage.isCalendarDateEnabled(dayString, useIndex)) { "Date $dayString should be enabled" }
        }
        
        val targetDate = today.plusDays(2)
        val targetDayStr = targetDate.dayOfMonth.toString()
        val expectedText = utils.DateHelper.formatDateWithOrdinal(targetDate)
        
        logger.info("Selecting date: $targetDayStr, expecting text: $expectedText")
        val useIndex = targetDayStr == "1" || targetDayStr == "2"
        timeSlotPage.selectCalendarDate(targetDayStr, useIndex)
        
        assert(timeSlotPage.isSelectedDateTextVisible(expectedText)) {
            "Selected date text '$expectedText' not visible" 
        }
        
        timeSlotPage.takeScreenshot("date-selection-verified")
    }

    @Test
    fun `should verify date selection and limits`() {
        val today = java.time.LocalDate.now()

        for (i in 1..7) {
            val date = today.plusDays(i.toLong())
            val dayString = date.dayOfMonth.toString()
            logger.info("Checking day view: $dayString")

            val monthString = date.month.toString()

            assert(timeSlotPage.isSelectedDateTextVisibleByExact(dayString)) { "Date $dayString should be enabled" }
            assert(timeSlotPage.isSelectedDateTextVisible(monthString)) { "Month $monthString should be enabled" }
        }

        val targetDate = today.plusDays(2)
        val targetDayStr = targetDate.dayOfMonth.toString()
        val expectedText = utils.DateHelper.formatDateWithOrdinal(targetDate)

        logger.info("Selecting day view: $targetDayStr, expecting text: $expectedText")

        timeSlotPage.selectDateView(targetDayStr)

        assert(timeSlotPage.isSelectedDateTextVisible(expectedText)) {
            "Selected date text '$expectedText' not visible"
        }

        timeSlotPage.takeScreenshot("date-selection-verified")
    }

}


