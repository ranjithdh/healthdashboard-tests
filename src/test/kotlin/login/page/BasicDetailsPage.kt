package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import io.qameta.allure.Step
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class BasicDetailsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    private val firstNameInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name"))
    private val emailInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email"))


    @Step("Enter First Name: {firstName}")
    fun enterFirstName(firstName: String): BasicDetailsPage {
        logger.info { "enterFirstName($firstName)" }
        firstNameInput.fill(firstName)
        return this
    }


    @Step("Enter Email: {email}")
    fun enterEmail(email: String): BasicDetailsPage {
        logger.info { "enterEmail($email)" }
        emailInput.fill(email)
        return this
    }

    fun clearFirstName(): BasicDetailsPage {
        logger.info { "clearFirstName()" }
        firstNameInput.clear()
        return this
    }


    fun clearEmail(): BasicDetailsPage {
        logger.info { "clearEmail()" }
        emailInput.clear()
        return this
    }

    @Step("Fill Basic Details")
    fun fillDetails(firstName: String,email: String): BasicDetailsPage {
        logger.info { "fillDetails($firstName, $email)" }
        utils.SignupDataStore.update {
            this.firstName = firstName
            this.email = email
        }
        enterFirstName(firstName)
        enterEmail(email)
        return this
    }


    @Step("Click Continue")
    fun clickContinue(): BasicDetailsPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    @Step("Fill Basic Details and Continue")
    fun fillBasicDetails(): PersonalDetailsPage {
        val testUser: TestUser = TestConfig.TestUsers.NEW_USER
        
        logger.info { "fillBasicDetails()" }
        fillDetails(testUser.firstName, testUser.email)
        clickContinue()
        val personalDetailsPage = PersonalDetailsPage(page)
        personalDetailsPage.waitForConfirmation()
        return personalDetailsPage
    }

    fun waitForConfirmation(): BasicDetailsPage {
        firstNameInput.waitFor()
        return this
    }


    fun isFirstNameVisible(): Boolean {
        return firstNameInput.isVisible
    }



    fun isEmailVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email")).isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isEnabled
    }

    fun isContinueButtonVisible(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isVisible
    }
}
