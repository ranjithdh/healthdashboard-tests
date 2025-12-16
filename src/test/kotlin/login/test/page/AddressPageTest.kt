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
        val testUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        return loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToAccountCreation(testUser.otp)
            .fillAndContinue("Test", "User", "test@test.com")
            .fillAndContinue()
    }

    @Test
    fun `should display all form fields`() {
        val addressPage = navigateToAddressPage()

        assert(addressPage.isAddressVisible()) { "Address field should be visible" }
        assert(addressPage.isCityVisible()) { "City field should be visible" }
        assert(addressPage.isStateVisible()) { "State dropdown should be visible" }
        assert(addressPage.isPinCodeVisible()) { "Pin code field should be visible" }

        addressPage.takeScreenshot("address-all-fields")
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
            address = "123, Test Street",
            city = "Chennai",
            state = "Tamil Nadu",
            pinCode = "600001"
        )
        addressPage.takeScreenshot("address-all-filled")
    }

    @Test
    fun `should have Continue disabled with empty fields`() {
        val addressPage = navigateToAddressPage()

        assert(!addressPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }
        addressPage.takeScreenshot("continue-disabled-address")
    }

    @Test
    fun `should navigate to time slot page on valid submission`() {
        val addressPage = navigateToAddressPage()

        val timeSlotPage = addressPage.fillAndContinue(
            address = "123, Test Street",
            city = "Chennai",
            state = "Tamil Nadu",
            pinCode = "600001"
        )

        assert(timeSlotPage.hasAvailableSlots()) { "Should be on time slot page" }
        timeSlotPage.takeScreenshot("navigated-to-timeslots")
    }
}
