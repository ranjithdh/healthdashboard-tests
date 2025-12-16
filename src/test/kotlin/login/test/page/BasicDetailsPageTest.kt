package login.test.page

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicDetailsPageTest {

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

    private fun navigateToBasicDetailsPage(): login.page.BasicDetailsPage {
        val testUser = TestConfig.TestUsers.NEW_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        return loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToAccountCreation(testUser.otp)
    }

    @Test
    fun `should display all form fields`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        assert(basicDetailsPage.isFirstNameVisible()) { "First name field should be visible" }
        assert(basicDetailsPage.isLastNameVisible()) { "Last name field should be visible" }
        assert(basicDetailsPage.isEmailVisible()) { "Email field should be visible" }
        assert(basicDetailsPage.isContinueButtonVisible()) { "Continue button should be visible" }
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("basic-details-all-fields")
    }

    @Test
    fun `should fill first name correctly`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.enterFirstName("DH")
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("first-name-filled")
    }

    @Test
    fun `should fill last name correctly`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.enterLastName("DashBoard")
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("last-name-filled")
    }

    @Test
    fun `should fill email correctly`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("email-filled")
    }

    @Test
    fun `should fill all details correctly`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.fillDetails("John", "Doe", "john.doe@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.takeScreenshot("all-details-filled")
    }

    @Test
    fun `should have Continue disabled with empty firstName`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.enterFirstName("DH")
        basicDetailsPage.enterLastName("DashBoard")
        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.clearFirstName()
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("continue-disabled-empty")
    }

    @Test
    fun `should have Continue disabled with empty lastName`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.enterFirstName("DH")
        basicDetailsPage.enterLastName("DashBoard")
        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.clearLastName()
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("continue-disabled-empty")
    }


    @Test
    fun `should have Continue disabled with empty email`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        basicDetailsPage.enterFirstName("DH")
        basicDetailsPage.enterLastName("DashBoard")
        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.clearEmail()
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("continue-disabled-empty")
    }

    @Test
    fun `should navigate to personal details on valid submission`() {
        val basicDetailsPage = navigateToBasicDetailsPage()

        val personalDetailsPage = basicDetailsPage
            .fillAndContinue("John", "Doe", "john.doe@test.com")

        assert(personalDetailsPage.isDateOfBirthVisible()) { "Should be on personal details page" }
        personalDetailsPage.takeScreenshot("navigated-to-personal-details")
    }
}
