package onboard.test.page

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.TestConfig
import onboard.page.LoginPage
import org.junit.jupiter.api.*


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_ONBOARDING)
class AddressPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var addressPage: onboard.page.AddressPage

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
        addressPage = performInitialNavigation()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun performInitialNavigation(): onboard.page.AddressPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER
        return loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
    }

    @Test
    @Order(1)
    fun `should display all form fields`() {
        addressPage.waitForConfirmation()

        assert(addressPage.isFlatHouseNoOrBuildingVisible()) { "Flat/House No/Building field should be visible" }
        assert(addressPage.isAddressVisible()) { "Address field should be visible" }
        assert(addressPage.isCityVisible()) { "City field should be visible" }
        assert(addressPage.isStateVisible()) { "State dropdown should be visible" }
        assert(addressPage.isPinCodeVisible()) { "Pin code field should be visible" }

        addressPage.takeScreenshot("address-all-fields")
    }

    @Test
    @Order(2)
    fun `should enter flat house no or building correctly`() {

        addressPage.enterFlatHouseNoOrBuilding("Flat 101")
        addressPage.takeScreenshot("flat-house-entered")
    }

    @Test
    @Order(3)
    fun `should enter address correctly`() {

        addressPage.enterAddress("123, Test Street, Test Area")
        addressPage.takeScreenshot("address-entered")
    }

    @Test
    @Order(4)
    fun `should enter city correctly`() {

        addressPage.enterCity("Chennai")
        addressPage.takeScreenshot("city-entered")
    }

    @Test
    @Order(5)
    fun `should select state correctly`() {

        addressPage.selectState("Tamil Nadu")
        addressPage.takeScreenshot("state-selected")
    }

    @Test
    @Order(6)
    fun `should enter pin code correctly`() {

        addressPage.enterPinCode("600001")
        addressPage.takeScreenshot("pincode-entered")
    }

    @Test
    @Order(7)
    fun `should fill all address details correctly`() {

        addressPage.fillAddress(
            flatHouseNoOrBuilding = "Flat 101",
            address = "123, Test Street",
            city = "Chennai",
            state = "Tamil Nadu",
            pinCode = "600001"
        )
        addressPage.takeScreenshot("address-all-filled")
    }

    @Test
    @Order(8)
    fun `should have Continue disabled with empty address`() {
        addressPage.fillAddress("Flat 101", "123, Test Street", "Chennai", "Tamil Nadu", "600001")
        assert(addressPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        addressPage.clearAddress()
        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled when address is empty" }
        addressPage.takeScreenshot("continue-disabled-empty-address")
    }

    @Test
    @Order(9)
    fun `should have Continue disabled with empty city`() {
        addressPage.fillAddress("Flat 101", "123, Test Street", "Chennai", "Tamil Nadu", "600001")
        assert(addressPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        addressPage.clearCity()
        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled when city is empty" }
        addressPage.takeScreenshot("continue-disabled-empty-city")
    }

    @Test
    @Order(10)
    fun `should have Continue disabled with empty pincode`() {
        addressPage.fillAddress("Flat 101", "123, Test Street", "Chennai", "Tamil Nadu", "600001")
        assert(addressPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        addressPage.clearPinCode()
        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled when pincode is empty" }
        addressPage.takeScreenshot("continue-disabled-empty-pincode")
    }

    @Test
    @Order(11)
    fun `should navigate to time slot page on valid submission`() {

        val timeSlotPage = addressPage.fillAddressDetails(TestConfig.TestUsers.NEW_USER)

        timeSlotPage.waitForConfirmation()

        assert(timeSlotPage.hasAvailableSlots()) { "Should be on time slot page" }
        timeSlotPage.takeScreenshot("navigated-to-timeslots")
    }
}
