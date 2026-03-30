package onboard.test.page

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import onboard.page.PersonalDetailsPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_ONBOARDING)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PersonalDetailsPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var personalDetailsPage: PersonalDetailsPage

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
        personalDetailsPage = navigateToPersonalDetails()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun navigateToPersonalDetails(): PersonalDetailsPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.ONBOARD_USER

        return loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
            .fillBasicDetails()
    }

    @Test
    @Order(1)
    fun `should display all form fields`() {
        assert(personalDetailsPage.isDateOfBirthVisible()) { "Date of Birth should be visible" }
        assert(personalDetailsPage.isGenderVisible()) { "Gender dropdown should be visible" }
        assert(personalDetailsPage.isHeightVisible()) { "Height field should be visible" }
        assert(personalDetailsPage.isWeightVisible()) { "Weight field should be visible" }

        personalDetailsPage.takeScreenshot("personal-details-all-fields")
    }

    @Test
    @Order(2)
    fun `should select date of birth correctly`() {
        personalDetailsPage.selectDateOfBirth("5", "1995", "15")
        personalDetailsPage.takeScreenshot("dob-selected")
    }

    @Test
    @Order(3)
    fun `should select gender correctly`() {
        personalDetailsPage.selectGender("Male")
        personalDetailsPage.takeScreenshot("gender-selected")
    }

    @Test
    @Order(4)
    fun `should select female gender`() {
        personalDetailsPage.selectGender("Female")
        personalDetailsPage.takeScreenshot("gender-female-selected")
    }

    @Test
    @Order(5)
    fun `should enter height correctly`() {
        personalDetailsPage.enterHeight("175")
        personalDetailsPage.takeScreenshot("height-entered")
    }

    @Test
    @Order(6)
    fun `should enter weight correctly`() {
        personalDetailsPage.enterWeight("70")
        personalDetailsPage.takeScreenshot("weight-entered")
    }

    @Test
    @Order(7)
    fun `should have Continue disabled with empty height`() {
        personalDetailsPage.fillDetails()
        assert(personalDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        personalDetailsPage.clearHeight()
        assert(!personalDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled when height is empty" }
        personalDetailsPage.takeScreenshot("continue-disabled-empty-height")
    }

    @Test
    @Order(8)
    fun `should have Continue disabled with empty weight`() {
        personalDetailsPage.fillDetails(
            gender = "Male",
            height = "175",
            weight = "70",
            month = "3",
            year = "1990",
            day = "20"
        )
        assert(personalDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled when all fields are filled" }

        personalDetailsPage.clearWeight()
        assert(!personalDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled when weight is empty" }
        personalDetailsPage.takeScreenshot("continue-disabled-empty-weight")
    }

    @Test
    @Order(9)
    fun `should fill all details correctly`() {

        personalDetailsPage.fillDetails(
            gender = "Male",
            height = "175",
            weight = "70",
            month = "3",
            year = "1998",
            day = "20"
        )
        personalDetailsPage.takeScreenshot("personal-details-all-filled")
    }

    @Test
    @Order(10)
    fun `should navigate to address page on valid submission`() {

        val addressPage = personalDetailsPage.fillPersonalDetails()
        addressPage.waitForConfirmation()
        addressPage.takeScreenshot("navigated-to-address")
    }
}
