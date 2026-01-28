package login.test.page

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddressPageTest {

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

    private fun navigateToAddressPage(): login.page.AddressPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER
        return loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
            .fillPersonalDetails()
    }

    @Test
    fun `should display all form fields`() {
        val addressPage = navigateToAddressPage()

        assert(addressPage.isFlatHouseNoOrBuildingVisible()) { "Flat/House No/Building field should be visible" }
        assert(addressPage.isAddressVisible()) { "Address field should be visible" }
        assert(addressPage.isCityVisible()) { "City field should be visible" }
        assert(addressPage.isStateVisible()) { "State dropdown should be visible" }
        assert(addressPage.isPinCodeVisible()) { "Pin code field should be visible" }

        addressPage.takeScreenshot("address-all-fields")
    }

    @Test
    fun `should enter flat house no or building correctly`() {
        val addressPage = navigateToAddressPage()

        addressPage.enterFlatHouseNoOrBuilding("Flat 101")
        addressPage.takeScreenshot("flat-house-entered")
    }

    @Test
    fun `should enter address correctly`() {
        val addressPage = navigateToAddressPage()

        addressPage.enterAddress("123, Test Street, Test Area")
        addressPage.takeScreenshot("address-entered")
    }

    @Test
    fun `should enter city correctly`() {
        val addressPage = navigateToAddressPage()

        addressPage.enterCity("Chennai")
        addressPage.takeScreenshot("city-entered")
    }

    @Test
    fun `should select state correctly`() {
        val addressPage = navigateToAddressPage()

        addressPage.selectState("Tamil Nadu")
        addressPage.takeScreenshot("state-selected")
    }

    @Test
    fun `should enter pin code correctly`() {
        val addressPage = navigateToAddressPage()

        addressPage.enterPinCode("600001")
        addressPage.takeScreenshot("pincode-entered")
    }

    @Test
    fun `should fill all address details correctly`() {
        val addressPage = navigateToAddressPage()

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
    fun `should have Continue disabled with empty address`() {
        val addressPage = navigateToAddressPage()
        addressPage.fillAddress("Flat 101", "123, Test Street", "Chennai", "Tamil Nadu", "600001")
        assert(addressPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        addressPage.clearAddress()
        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled when address is empty" }
        addressPage.takeScreenshot("continue-disabled-empty-address")
    }

    @Test
    fun `should have Continue disabled with empty city`() {
        val addressPage = navigateToAddressPage()
        addressPage.fillAddress("Flat 101", "123, Test Street", "Chennai", "Tamil Nadu", "600001")
        assert(addressPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        addressPage.clearCity()
        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled when city is empty" }
        addressPage.takeScreenshot("continue-disabled-empty-city")
    }

    @Test
    fun `should have Continue disabled with empty pincode`() {
        val addressPage = navigateToAddressPage()
        addressPage.fillAddress("Flat 101", "123, Test Street", "Chennai", "Tamil Nadu", "600001")
        assert(addressPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        addressPage.clearPinCode()
        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled when pincode is empty" }
        addressPage.takeScreenshot("continue-disabled-empty-pincode")
    }

    @Test
    fun `should navigate to time slot page on valid submission`() {
        val addressPage = navigateToAddressPage()

        val timeSlotPage = addressPage.fillAddressDetails(TestConfig.TestUsers.NEW_USER)

        timeSlotPage.waitForConfirmation()

        assert(timeSlotPage.hasAvailableSlots()) { "Should be on time slot page" }
        timeSlotPage.takeScreenshot("navigated-to-timeslots")
    }
}
