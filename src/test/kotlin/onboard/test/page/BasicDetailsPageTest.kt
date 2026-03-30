package onboard.test.page

import io.qameta.allure.Epic
import utils.report.Modules
import com.microsoft.playwright.*
import config.BaseTest
import config.TestConfig
import onboard.page.BasicDetailsPage
import onboard.page.LoginPage
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic(Modules.EPIC_ONBOARDING)
class BasicDetailsPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var basicDetailsPage: BasicDetailsPage

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

        basicDetailsPage = navigateToBasicDetailsPage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }


    private fun navigateToBasicDetailsPage(): onboard.page.BasicDetailsPage {
        val loginPage = LoginPage(page).navigate() as LoginPage
        val testUser = TestConfig.TestUsers.ONBOARD_USER

        return loginPage
            .enterMobileAndContinue(testUser)
            .enterOtpAndContinueToAccountCreation(testUser)
    }

    @Test
    fun `should display all form fields`() {

        assert(basicDetailsPage.isFirstNameVisible()) { "First name field should be visible" }
        assert(basicDetailsPage.isEmailVisible()) { "Email field should be visible" }
        assert(basicDetailsPage.isContinueButtonVisible()) { "Continue button should be visible" }
//        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("basic-details-all-fields")
    }

    @Test
    fun `should fill first name correctly`() {

        basicDetailsPage.enterFirstName("DH")
//        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("first-name-filled")
    }


    @Test
    fun `should fill email correctly`() {
        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
//        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("email-filled")
    }

    @Test
    fun `should fill all details correctly`() {
        basicDetailsPage.fillDetails("John", "john.doe@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.takeScreenshot("all-details-filled")
    }

    @Test
    fun `should have Continue disabled with empty firstName`() {
        basicDetailsPage.enterFirstName("DH")
        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.clearFirstName()
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("continue-disabled-empty")
    }

    @Test
    fun `should have Continue disabled with empty email`() {
        basicDetailsPage.enterFirstName("DH")
        basicDetailsPage.enterEmail("dhdashboard.dh@test.com")
        assert(basicDetailsPage.isContinueButtonEnabled()) { "Continue should be enabled with all fields are filled" }

        basicDetailsPage.clearEmail()
        assert(!basicDetailsPage.isContinueButtonEnabled()) { "Continue should be disabled with empty fields" }

        basicDetailsPage.takeScreenshot("continue-disabled-empty")
    }

    @Test
    fun `should navigate to personal details on valid submission`() {
        val personalDetailsPage = basicDetailsPage
            .fillBasicDetails()

        assert(personalDetailsPage.isDateOfBirthVisible()) { "Should be on personal details page" }
        personalDetailsPage.takeScreenshot("navigated-to-personal-details")
    }
}
