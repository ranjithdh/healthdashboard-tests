package login.test.page

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_ONBOARDING)
class PersonalDetailsPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

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

    private fun navigateToPersonalDetails(): login.page.PersonalDetailsPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.NEW_USER

        return loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
    }

    @Test
    fun `should display all form fields`() {
        val personalDetailsPage = navigateToPersonalDetails()

        assert(personalDetailsPage.isDateOfBirthVisible()) { "Date of Birth should be visible" }
        assert(personalDetailsPage.isGenderVisible()) { "Gender dropdown should be visible" }
        assert(personalDetailsPage.isHeightVisible()) { "Height field should be visible" }
        assert(personalDetailsPage.isWeightVisible()) { "Weight field should be visible" }

        personalDetailsPage.takeScreenshot("personal-details-all-fields")
    }

    @Test
    fun `should select date of birth correctly`() {
        val personalDetailsPage = navigateToPersonalDetails()

        personalDetailsPage.selectDateOfBirth("5", "1995", "15")
        personalDetailsPage.takeScreenshot("dob-selected")
    }

    @Test
    fun `should select gender correctly`() {
        val personalDetailsPage = navigateToPersonalDetails()

        personalDetailsPage.selectGender("Male")
        personalDetailsPage.takeScreenshot("gender-selected")
    }

    @Test
    fun `should select female gender`() {
        val personalDetailsPage = navigateToPersonalDetails()

        personalDetailsPage.selectGender("Female")
        personalDetailsPage.takeScreenshot("gender-female-selected")
    }

    @Test
    fun `should enter height correctly`() {
        val personalDetailsPage = navigateToPersonalDetails()

        personalDetailsPage.enterHeight("175")
        personalDetailsPage.takeScreenshot("height-entered")
    }

    @Test
    fun `should enter weight correctly`() {
        val personalDetailsPage = navigateToPersonalDetails()

        personalDetailsPage.enterWeight("70")
        personalDetailsPage.takeScreenshot("weight-entered")
    }

    @Test
    fun `should have Continue disabled with empty height`() {
        val personalDetailsPage = navigateToPersonalDetails()
        personalDetailsPage.fillDetails()
        assert(personalDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        personalDetailsPage.clearHeight()
        assert(!personalDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled when height is empty" }
        personalDetailsPage.takeScreenshot("continue-disabled-empty-height")
    }

    @Test
    fun `should have Continue disabled with empty weight`() {
        val personalDetailsPage = navigateToPersonalDetails()
        personalDetailsPage.fillDetails()
        assert(personalDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        personalDetailsPage.clearWeight()
        assert(!personalDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled when weight is empty" }
        personalDetailsPage.takeScreenshot("continue-disabled-empty-weight")
    }

    @Test
    fun `should fill all details correctly`() {
        val personalDetailsPage = navigateToPersonalDetails()

        personalDetailsPage.fillDetails(
            gender = "Male",
            height = "175",
            weight = "70",
            month = "3",
            year = "1990",
            day = "20"
        )
        personalDetailsPage.takeScreenshot("personal-details-all-filled")
    }

    @Test
    fun `should navigate to address page on valid submission`() {
        val personalDetailsPage = navigateToPersonalDetails()

        val addressPage = personalDetailsPage.fillPersonalDetails()

        assert(addressPage.isAddressVisible()) { "Should be on address page" }
        addressPage.takeScreenshot("navigated-to-address")
    }
}
