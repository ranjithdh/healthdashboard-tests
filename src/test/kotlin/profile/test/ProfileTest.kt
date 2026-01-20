package profile.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*

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
        val contextOptions =
            Browser.NewContextOptions().setViewportSize(viewport.width, viewport.height).setHasTouch(viewport.hasTouch)
                .setIsMobile(viewport.isMobile).setDeviceScaleFactor(viewport.deviceScaleFactor)

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

        val loginPage = LoginPage(page).navigate() as LoginPage
        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
    }


    @Test
    fun `new address add`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

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

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        profilePage.removeUserAddress()
    }


    @Test
    fun `edit address`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        profilePage.editUserAddress()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
    }

    /**-----------Tone Preference----------------*/
    @Test
    fun `tone preference selection`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.selectCommunicationOption()

    }


    /**------------Account Information----------------*/
    @Test
    fun `account information validation`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.accountInformationValidation()
    }

    /*@Test
    fun `account information edit`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue(testUser.mobileNumber).enterOtpAndContinueToHomePage(testUser.otp)
                .clickAccountProfile().waitForConfirmation()

        profilePage.accountInformationEdit()
    }

    @Test
    fun `account information edit with close`() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue(testUser.mobileNumber).enterOtpAndContinueToHomePage(testUser.otp)
                .clickAccountProfile().waitForConfirmation()

        profilePage.accountInformationEditClose()
    }*/

    /**-------------Health Metrics---------------*/
    @Test
    fun `health metrics validations`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.assertHealthMetrics()
    }

    @Test
    fun `health metrics edit`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.assertEditHealthMetrics()
    }

    /**-------------Questioner---------------*/

    @Test
    fun `questioner validation vegetarian`() { //done

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()


        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `questioner validation non_vegetarian`() { //done

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)

        profilePage.assertQuestionerNonVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }


    @Test
    fun `questioner validation skipping the exercise`() { //done

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()
        // Pass HARDLY_EXERCISE to skip Q11-Q13 and go directly to Q14

        profilePage.setActivityType(type = profile.model.ActivityLevel.HARDLY_EXERCISE)

        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    //Medical Conditions Flow Tests

    @Test
    fun `medical conditions - no conditions selected`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.NONE))

        // Test: Select "None of the above" in Q37
        // Expected: Q37 → Q51 (skip all condition detail questions)
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - single gastrointestinal`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.GASTROINTESTINAL))


        // Test: Select "Gastrointestinal Conditions" only in Q37
        // Expected: Q37 → Q38 (GI details) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - multiple conditions GI and dermatological`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(
            profile.model.MedicalCondition.GASTROINTESTINAL, profile.model.MedicalCondition.DERMATOLOGICAL
        ))


        // Test: Select "Gastrointestinal" + "Dermatological" in Q37
        // Expected: Q37 → Q38 (GI) → Q39 (Skin) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - diabetes only`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.DIABETES))

        // Test: Select "Type 2 - Diabetes" only in Q37
        // Expected: Q37 → Q42 (Diabetes status) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - thyroid only`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.THYROID))

        // Test: Select "Thyroid-related disorders" only in Q37
        // Expected: Q37 → Q43 (Thyroid details) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - cancer flow`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.CANCER))

        // Test: Select "Cancer" only in Q37
        // Expected: Q37 → Q49 (Cancer status) → Q50 (Cancer type) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - cardiovascular and kidney`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions( listOf(
            profile.model.MedicalCondition.CARDIOVASCULAR, profile.model.MedicalCondition.KIDNEY
        ))

        // Test: Select "Cardiovascular" + "Kidney Conditions" in Q37
        // Expected: Q37 → Q46 (Heart) → Q45 (Kidney) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - complex multi selection`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()


        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions( listOf(
            profile.model.MedicalCondition.DIABETES,
            profile.model.MedicalCondition.THYROID,
            profile.model.MedicalCondition.CANCER
        ))

        // Test: Select "Diabetes" + "Thyroid" + "Cancer" in Q37
        // Expected: Q37 → Q42 (Diabetes) → Q43 (Thyroid) → Q49 (Cancer) → Q50 (Type) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - respiratory and auto-immune`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions( listOf(
            profile.model.MedicalCondition.RESPIRATORY, profile.model.MedicalCondition.AUTO_IMMUNE
        ))

        // Test: Select "Respiratory" + "Auto-immune" in Q37
        // Expected: Q37 → Q47 (Respiratory) → Q48 (Auto-immune) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    fun `medical conditions - all major conditions`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()


        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions( listOf(
            profile.model.MedicalCondition.GASTROINTESTINAL,
            profile.model.MedicalCondition.DERMATOLOGICAL,
            profile.model.MedicalCondition.DIABETES,
            profile.model.MedicalCondition.THYROID,
            profile.model.MedicalCondition.GALL_BLADDER,
        ))

        // Test: Select multiple major conditions in Q37
        // Expected: Q37 → All selected detail questions → Q51
        // This tests the queue handling with maximum load
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }


    /*   //Re-selection checking
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
   */

    @Test
    fun `questioner backward validation complete flow`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        // Set flag to stop before completion
        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.GASTROINTESTINAL))
        profilePage.setShouldClickComplete(false)

        // Fill the questionnaire (using Veg flow as example)
        profilePage.assertQuestionerVegInitialCheck()

        // Validate backward navigation
        profilePage.assertQuestionerBackwardValidationsCheck()
    }

    @Test
    fun `questioner backward validation at question 20`() {

        val loginPage = LoginPage(page).navigate() as LoginPage

        val profilePage =
            loginPage.enterMobileAndContinue().enterOtpAndContinueToHomePage()
                .clickAccountProfile().waitForConfirmation()

        // Set flag to stop at question 20 and goBack
        profilePage.setStopAtQuestion(20)
        profilePage.setActivityType(type = profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(profile.model.MedicalCondition.GASTROINTESTINAL))
        profilePage.setShouldClickComplete(false)

        // Fill the questionnaire (using Veg flow as example)
        profilePage.assertQuestionerVegInitialCheck()

        // Validate backward navigation
        profilePage.assertQuestionerBackwardValidationsCheck()
    }

}