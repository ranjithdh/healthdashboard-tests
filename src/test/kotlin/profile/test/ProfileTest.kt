package profile.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProfileTest {
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
    fun teardown() {
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


    /**-----------Address----------------*/
    @Test
    fun `address information validation`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage
        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickProfile()
            .waitForConfirmation()

        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
    }


    @Test
    fun `new address add`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickProfile()
            .waitForConfirmation()

        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        assert(profilePage.isAddNewAddressVisible()) { "Add new address visibility is not visible" }
        profilePage.clickAddNewAddress()
        assert(profilePage.isNewAddressDialogVisible()) { "Add new address dialog is not visible" }
        profilePage.assertAddressFormFieldsVisible()
        profilePage.addAddressAndValidate()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
    }


    @Test
    fun `remove address`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickProfile()
            .waitForConfirmation()
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        profilePage.removeUserAddress()
    }


    @Test
    fun `edit address`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickProfile()
            .waitForConfirmation()
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        profilePage.editUserAddress()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
    }

    /**-----------Tone Preference----------------*/
    @Test
    fun `tone preference selection`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickProfile()
            .waitForConfirmation()

        profilePage.selectCommunicationOption()

    }
}