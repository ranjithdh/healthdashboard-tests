package login.test.page

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import login.page.LoginPage
import login.page.TimeSlotPage
import org.junit.jupiter.api.*
import utils.logger.logger


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimeSlotPageTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var page: Page

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
    }

    @AfterAll
    fun tearDown() {
        browser.close()
        playwright.close()
    }

    @BeforeEach
    fun createContext() {
        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    private fun navigateToTimeSlotPage(): TimeSlotPage {
        val testUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        return loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToAccountCreation(testUser.otp)
            .fillAndContinue("Test", "User", "test@test.com")
            .fillAndContinue()
            .fillAndContinue("Flat 101","Test Address", "Chennai", "Tamil Nadu", "600001")
    }

    @Test
    fun `should display available time slots`() {
        val timeSlotPage = navigateToTimeSlotPage()

        assert(timeSlotPage.hasAvailableSlots()) { "Should have available slots" }
        timeSlotPage.takeScreenshot("time-slots-available")
    }


    @Test
    fun `should select morning slot successfully`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage.selectMorningSlot()
        timeSlotPage.takeScreenshot("morning-slot-selected")
    }

    @Test
    fun `should select post meal slot successfully`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage.selectPostMealSlot()
        timeSlotPage.takeScreenshot("post-meal-slot-selected")
    }

    @Test
    fun `should select multiple slots`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage
            .selectMorningSlot()
            .selectPostMealSlot()

        timeSlotPage.takeScreenshot("multiple-slots-selected")
    }

    @Test
    fun `should verify invalid fasting slot conditions`() {
        val timeSlotPage = navigateToTimeSlotPage()
        
        val lateSlots = timeSlotPage.getFastingSlotsStartingAfter(11, 30)
        
        if (lateSlots.isNotEmpty()) {
            val lateSlot = lateSlots.first()
            println("Testing late slot: $lateSlot")
            timeSlotPage.clickSlot(lateSlot)
            assert(timeSlotPage.isInvalidFastingSlotPopupVisible()) { "Popup should appear for slot > 11:30 AM: $lateSlot" }
            timeSlotPage.closeInvalidFastingSlotPopup()
        }

        val unavailableSlots = timeSlotPage.getUnavailableFastingSlots()
        if (unavailableSlots.isNotEmpty()) {
            val unavailableSlot = unavailableSlots.first()
            println("Testing unavailable slot: $unavailableSlot")
            timeSlotPage.clickSlot(unavailableSlot)
            assert(timeSlotPage.isInvalidFastingSlotPopupVisible()) { "Popup should appear for unavailable slot: $unavailableSlot" }
            timeSlotPage.closeInvalidFastingSlotPopup()
        }
    }

    @Test
    fun `should verify post meal slots match api data`() {
        val timeSlotPage = navigateToTimeSlotPage()
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
        val timeSlotPage = navigateToTimeSlotPage()
        
        assert(timeSlotPage.validateFastingSlotDurations(30)) { 
            "Not all valid fasting slots (<= 11:30 AM) have a 30-minute duration" 
        }
        
        timeSlotPage.takeScreenshot("slot-duration-verified")
    }

    @Test
    fun `should verify date selection and limits in date picker dialog`() {
        val timeSlotPage = navigateToTimeSlotPage()
        
        timeSlotPage.openDatePicker()
        
        val today = java.time.LocalDate.now()
        
        for (i in 1..7) {
            val date = today.plusDays(i.toLong())
            val dayString = date.dayOfMonth.toString()
            logger.info("Checking enabled status for day: $dayString")
            assert(timeSlotPage.isCalendarDateEnabled(dayString)) { "Date $dayString should be enabled" }
        }
        
        val targetDate = today.plusDays(2)
        val targetDayStr = targetDate.dayOfMonth.toString()
        val expectedText = utils.DateHelper.formatDateWithOrdinal(targetDate)
        
        logger.info("Selecting date: $targetDayStr, expecting text: $expectedText")
        
        timeSlotPage.selectCalendarDate(targetDayStr)
        
        assert(timeSlotPage.isSelectedDateTextVisible(expectedText)) {
            "Selected date text '$expectedText' not visible" 
        }
        
        timeSlotPage.takeScreenshot("date-selection-verified")
    }

    @Test
    fun `should verify date selection and limits`() {
        val timeSlotPage = navigateToTimeSlotPage()


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


