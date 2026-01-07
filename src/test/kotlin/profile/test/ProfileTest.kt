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
            .clickAccountProfile()
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
            .clickAccountProfile()
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
            .clickAccountProfile()
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
            .clickAccountProfile()
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
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.selectCommunicationOption()

    }


    /**------------Account Information----------------*/
    @Test
    fun `account information validation`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.accountInformationValidation()
    }

    @Test
    fun `account information edit`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.accountInformationEdit()
    }

    @Test
    fun `account information edit with close`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.accountInformationEditClose()
    }

    /**-------------Health Metrics---------------*/
    @Test
    fun `health metrics validations`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.assertHealthMetrics()
    }

    @Test
    fun `health metrics edit`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.assertEditHealthMetrics()
    }

    /**-------------Questioner---------------*/

    @Test
    fun `questioner validation vegetarian`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.assertQuestionerVegInitialCheck(type = profile.model.ActivityLevel.SEDENTARY, isMale = false)
    }

    @Test
    fun `questioner validation non_vegetarian`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.assertQuestionerNonVegInitialCheck()
    }


    @Test
    fun `questioner validation skipping the exercise`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()
        // Pass HARDLY_EXERCISE to skip Q11-Q13 and go directly to Q14
        profilePage.assertQuestionerVegInitialCheck(type = profile.model.ActivityLevel.HARDLY_EXERCISE, isMale = false)
    }

    //Medical Conditions Flow Tests

    @Test
    fun `medical conditions - no conditions selected`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "None of the above" in Q37
        // Expected: Q37 → Q51 (skip all condition detail questions)
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(profile.model.MedicalCondition.NONE),
             isMale = false
        )
    }

    @Test
    fun `medical conditions - single gastrointestinal`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Gastrointestinal Conditions" only in Q37
        // Expected: Q37 → Q38 (GI details) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(profile.model.MedicalCondition.GASTROINTESTINAL),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - multiple conditions GI and dermatological`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Gastrointestinal" + "Dermatological" in Q37
        // Expected: Q37 → Q38 (GI) → Q39 (Skin) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(
                profile.model.MedicalCondition.GASTROINTESTINAL,
                profile.model.MedicalCondition.DERMATOLOGICAL
            ),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - diabetes only`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Type 2 - Diabetes" only in Q37
        // Expected: Q37 → Q42 (Diabetes status) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(profile.model.MedicalCondition.DIABETES),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - thyroid only`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Thyroid-related disorders" only in Q37
        // Expected: Q37 → Q43 (Thyroid details) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(profile.model.MedicalCondition.THYROID),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - cancer flow`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Cancer" only in Q37
        // Expected: Q37 → Q49 (Cancer status) → Q50 (Cancer type) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(profile.model.MedicalCondition.CANCER),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - cardiovascular and kidney`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Cardiovascular" + "Kidney Conditions" in Q37
        // Expected: Q37 → Q46 (Heart) → Q45 (Kidney) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(
                profile.model.MedicalCondition.CARDIOVASCULAR,
                profile.model.MedicalCondition.KIDNEY
            ),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - complex multi selection`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Diabetes" + "Thyroid" + "Cancer" in Q37
        // Expected: Q37 → Q42 (Diabetes) → Q43 (Thyroid) → Q49 (Cancer) → Q50 (Type) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(
                profile.model.MedicalCondition.DIABETES,
                profile.model.MedicalCondition.THYROID,
                profile.model.MedicalCondition.CANCER
            ),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - respiratory and auto-immune`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select "Respiratory" + "Auto-immune" in Q37
        // Expected: Q37 → Q47 (Respiratory) → Q48 (Auto-immune) → Q51
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(
                profile.model.MedicalCondition.RESPIRATORY,
                profile.model.MedicalCondition.AUTO_IMMUNE
            ),
            isMale = false
        )
    }

    @Test
    fun `medical conditions - all major conditions`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        // Test: Select multiple major conditions in Q37
        // Expected: Q37 → All selected detail questions → Q51
        // This tests the queue handling with maximum load
        profilePage.assertQuestionerVegInitialCheck(
            type = profile.model.ActivityLevel.SEDENTARY,
            condition = listOf(
                profile.model.MedicalCondition.GASTROINTESTINAL,
                profile.model.MedicalCondition.DERMATOLOGICAL,
                profile.model.MedicalCondition.DIABETES,
                profile.model.MedicalCondition.THYROID,
                profile.model.MedicalCondition.GALL_BLADDER,
            ),
            isMale = false
        )

        profilePage.assertQuestionerValidationsCheck()
    }

    //Re-selection checking
    @Test
    fun `questioner re-selection validations`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.assertQuestionerValidationsCheck()
    }


    //Re-selection checking
    @Test
    fun `questioner re-selection validations sample`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage = loginPage
            .enterMobileAndContinue(testUser.mobileNumber)
            .enterOtpAndContinueToHomePage(testUser.otp)
            .clickAccountProfile()
            .waitForConfirmation()

        profilePage.assertQuestionerValidationsCheckSample()
    }


}