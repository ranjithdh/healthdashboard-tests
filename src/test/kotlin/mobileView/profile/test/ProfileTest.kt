package mobileView.profile.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import io.qameta.allure.Epic
import mobileView.profile.page.ProfilePage
import onboard.page.LoginPage
import model.profile.QuestionerMealType
import mu.KotlinLogging
import org.junit.jupiter.api.*
import utils.report.Modules

private val logger = KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Epic(Modules.EPIC_PROFILE)
@Tag("mobile")
class ProfileTest : BaseTest() {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var profilePage: ProfilePage


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
        // context.setDefaultTimeout(TestConfig.Browser.TIMEOUT * 2)
        page = context.newPage()
        profilePage = performInitialNavigation()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun performInitialNavigation(): ProfilePage {
        logger.info { "[INIT] Step 1: Navigating to Login page..." }
        val loginPage = LoginPage(page).navigate() as LoginPage
        logger.info { "[INIT] Step 1 OK: Login page loaded. Current URL: ${page.url()}" }

        logger.info { "[INIT] Step 2: Entering mobile number and clicking Continue..." }
        val otpPage = loginPage.enterMobileAndContinue()
        logger.info { "[INIT] Step 2 OK: OTP screen reached. Current URL: ${page.url()}" }

        logger.info { "[INIT] Step 3: Entering OTP and waiting for Home page..." }
        val homePage = otpPage.enterOtpAndContinueToHomePage()
        logger.info { "[INIT] Step 3 OK: Home page confirmed. Current URL: ${page.url()}" }

        logger.info { "[INIT] Step 4: Clicking account profile icon..." }
        val profilePage = homePage.clickAccountProfile()
        logger.info { "[INIT] Step 4 OK: Profile page reached. Current URL: ${page.url()}" }

        logger.info { "[INIT] Step 5: Waiting for Profile page confirmation (Tone Preference)..." }
        profilePage.waitForConfirmation()
        logger.info { "[INIT] Step 5 OK: Profile page fully loaded." }

        return profilePage
    }


    /**-----------Address----------------*/

    @Test
    @Order(1)
    fun `address information validation`() {
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
        profilePage.clickAddressDropDown()
    }


    @Test
    @Order(2)
    fun `new address add`() {
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        assert(profilePage.isAddNewAddressVisible()) { "Add new address visibility is not visible" }
        profilePage.clickAddNewAddress()
        assert(profilePage.isNewAddressDialogVisible()) { "Add new address dialog is not visible" }
        profilePage.assertAddressFormFieldsVisible()
        profilePage.addAddressAndValidate()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
        profilePage.clickAddressDropDown()
    }


    @Test
    @Order(3)
    fun `remove address`() {
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        profilePage.removeUserAddress()
        profilePage.clickAddressDropDown()
    }


    @Test
    @Order(4)
    fun `edit address`() {
        assert(profilePage.isSaveAddressDropDownVisible()) { "Save address drop down is not visible" }
        profilePage.clickAddressDropDown()
        profilePage.editUserAddress()
        assertDoesNotThrow { profilePage.assertAddressesFromApi() }
    }

    /**-----------Tone Preference----------------*/

    @Test
    @Order(5)
    fun `tone preference selection`() {
        profilePage.selectCommunicationOption()
    }


    /**------------Account Information----------------*/

    @Test
    @Order(6)
    fun `account information validation`() {
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
    @Order(7)
    fun `health metrics validations`() {
        profilePage.assertHealthMetrics()
    }

    @Test
    @Order(8)
    fun `health metrics edit`() {
        profilePage.assertEditHealthMetrics()
    }

/**-------------Questioner---------------*/


    @Test
    @Order(11)
    fun `questioner validation vegetarian`() { //done
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.assertQuestionerVegInitialCheck(QuestionerMealType.VEGETARIAN)
        profilePage.setShouldClickComplete(true)

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(10)
    fun `questioner validation vegan`() { //done
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.assertQuestionerVegInitialCheck(QuestionerMealType.VEGAN)
        profilePage.setShouldClickComplete(true)

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(12)
    fun `questioner validation eggetarian`() { //done
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.assertQuestionerVegInitialCheck(QuestionerMealType.EGGETARIAN)
        profilePage.setShouldClickComplete(true)

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(13)
    fun `questioner validation non_vegetarian`() { //done
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setShouldClickComplete(true)

        profilePage.assertQuestionerVegInitialCheck(QuestionerMealType.NON_VEGETARIAN)

        profilePage.assertQuestionerValidationsCheck()
    }


    @Test
    @Order(14)
    fun `questioner validation skipping the exercise`() { //done
        // Pass HARDLY_EXERCISE to skip Q11-Q13 and go directly to Q14
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.HARDLY_EXERCISE)
        profilePage.setShouldClickComplete(true)

        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    //Medical Conditions Flow Tests

    @Test
    @Order(15)
    fun `medical conditions - no conditions selected`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.NONE))
        profilePage.setShouldClickComplete(true)

        // Test: Select "None of the above" in Q37
        // Expected: Q37 → Q51 (skip all condition detail questions)
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(16)
    fun `medical conditions - single gastrointestinal`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.GASTROINTESTINAL))
        profilePage.setShouldClickComplete(true)

        // Test: Select "Gastrointestinal Conditions" only in Q37
        // Expected: Q37 → Q38 (GI details) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(17)
    fun `medical conditions - multiple conditions GI and dermatological`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(
            listOf(
                mobileView.profile.model.MedicalCondition.GASTROINTESTINAL, mobileView.profile.model.MedicalCondition.DERMATOLOGICAL
            )
        )
        profilePage.setShouldClickComplete(true)


        // Test: Select "Gastrointestinal" + "Dermatological" in Q37
        // Expected: Q37 → Q38 (GI) → Q39 (Skin) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(18)
    fun `medical conditions - diabetes only`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.DIABETES))
        profilePage.setShouldClickComplete(true)

        // Test: Select "Type 2 - Diabetes" only in Q37
        // Expected: Q37 → Q42 (Diabetes status) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(19)
    fun `medical conditions - thyroid only`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.THYROID))
        profilePage.setShouldClickComplete(true)

        // Test: Select "Thyroid-related disorders" only in Q37
        // Expected: Q37 → Q43 (Thyroid details) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(20)
    fun `medical conditions - cancer flow`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.CANCER))
        profilePage.setShouldClickComplete(true)

        // Test: Select "Cancer" only in Q37
        // Expected: Q37 → Q49 (Cancer status) → Q50 (Cancer type) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(21)
    fun `medical conditions - cardiovascular and kidney`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(
            listOf(
                mobileView.profile.model.MedicalCondition.CARDIOVASCULAR, mobileView.profile.model.MedicalCondition.KIDNEY
            )
        )
        profilePage.setShouldClickComplete(true)

        // Test: Select "Cardiovascular" + "Kidney Conditions" in Q37
        // Expected: Q37 → Q46 (Heart) → Q45 (Kidney) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(22)
    fun `medical conditions - complex multi selection`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(
            listOf(
                mobileView.profile.model.MedicalCondition.DIABETES,
                mobileView.profile.model.MedicalCondition.THYROID,
                mobileView.profile.model.MedicalCondition.CANCER
            )
        )
        profilePage.setShouldClickComplete(true)

        // Test: Select "Diabetes" + "Thyroid" + "Cancer" in Q37
        // Expected: Q37 → Q42 (Diabetes) → Q43 (Thyroid) → Q49 (Cancer) → Q50 (Type) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(23)
    fun `medical conditions - respiratory and auto-immune`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(
            listOf(
                mobileView.profile.model.MedicalCondition.RESPIRATORY, mobileView.profile.model.MedicalCondition.AUTO_IMMUNE
            )
        )
        profilePage.setShouldClickComplete(true)

        // Test: Select "Respiratory" + "Auto-immune" in Q37
        // Expected: Q37 → Q47 (Respiratory) → Q48 (Auto-immune) → Q51
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }

    @Test
    @Order(24)
    fun `medical conditions - all major conditions`() {
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(
            listOf(
                mobileView.profile.model.MedicalCondition.GASTROINTESTINAL,
                mobileView.profile.model.MedicalCondition.DERMATOLOGICAL,
                mobileView.profile.model.MedicalCondition.DIABETES,
                mobileView.profile.model.MedicalCondition.THYROID,
                mobileView.profile.model.MedicalCondition.GALL_BLADDER,
            )
        )
        profilePage.setShouldClickComplete(true)

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
    @Order(25)
    fun `questioner backward validation complete flow`() {
        // Set flag to stop before completion
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.GASTROINTESTINAL))
        profilePage.setShouldClickComplete(false)

        // Fill the questionnaire (using Veg flow as example)
        profilePage.assertQuestionerVegInitialCheck()

        // Validate backward navigation
        profilePage.assertQuestionerBackwardValidationsCheck()
        profilePage.goBackQuestioner()
    }


    @Test
    @Order(9)
    fun `questioner backward validation at question 20`() {
        // Set flag to stop at question 20 and goBack
        profilePage.setStopAtQuestion(20)
        profilePage.setActivityType(type = mobileView.profile.model.ActivityLevel.SEDENTARY)
        profilePage.setMedicalConditions(listOf(mobileView.profile.model.MedicalCondition.GASTROINTESTINAL))
        profilePage.setShouldClickComplete(true)

        // Fill the questionnaire (using Veg flow as example)
        profilePage.assertQuestionerVegInitialCheck()

        profilePage.assertQuestionerValidationsCheck()
    }
}


