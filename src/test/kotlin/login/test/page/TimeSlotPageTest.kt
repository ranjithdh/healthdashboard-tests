package login.test.page

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import login.page.TimeSlotPage
import org.junit.jupiter.api.*


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
            .fillAndContinue("Test Address", "Chennai", "Tamil Nadu", "600001")
    }

    @Test
    fun `should display available time slots`() {
        val timeSlotPage = navigateToTimeSlotPage()

        assert(timeSlotPage.hasAvailableSlots()) { "Should have available slots" }
        timeSlotPage.takeScreenshot("time-slots-available")
    }

    @Test
    fun `should display morning slot option`() {
        val timeSlotPage = navigateToTimeSlotPage()

        assert(timeSlotPage.isTimeSlotVisible(":00 - 08:30 AM")) { "Morning slot should be visible" }
        timeSlotPage.takeScreenshot("morning-slot-visible")
    }

    @Test
    fun `should select morning slot successfully`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage.selectMorningSlot()
        timeSlotPage.takeScreenshot("morning-slot-selected")
    }

    @Test
    fun `should select second slot successfully`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage.selectSecondSlot()
        timeSlotPage.takeScreenshot("second-slot-selected")
    }

    @Test
    fun `should select multiple slots`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage
            .selectMorningSlot()
            .selectSecondSlot()

        timeSlotPage.takeScreenshot("multiple-slots-selected")
    }

    @Test
    fun `should select slot by name and index`() {
        val timeSlotPage = navigateToTimeSlotPage()

        timeSlotPage.selectSlot(":00 - 08:30 AM", 0)
        timeSlotPage.takeScreenshot("slot-by-name-selected")
    }


    @Test
    fun `should verify date selection and schedule flow`() {
        val timeSlotPage = navigateToTimeSlotPage()

        assert(timeSlotPage.isScheduleHeaderVisible()) { "Schedule header verification failed" }
        assert(timeSlotPage.isAddressSectionVisible()) { "Address section verification failed" }

        timeSlotPage.selectDate("14")

        assert(timeSlotPage.isFastingSlotsHeaderVisible()) { "Fasting slots header verification failed" }
        assert(timeSlotPage.isPostMealSlotsVisible()) { "Post meal slots header verification failed" }
        assert(timeSlotPage.isFastingInstructionVisible()) { "Fasting instruction verification failed" }
        assert(timeSlotPage.isPostMealInstructionVisible()) { "Post meal instruction verification failed" }

        timeSlotPage.selectMorningSlot()
        timeSlotPage.selectSecondSlot()

        timeSlotPage.clickSchedule()

        timeSlotPage.takeScreenshot("scheduled-successfully")
    }
}
