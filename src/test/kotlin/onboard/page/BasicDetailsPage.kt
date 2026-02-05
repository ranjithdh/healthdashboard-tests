package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_CONTINUE
import utils.report.StepHelper.ENTER_EMAIL
import utils.report.StepHelper.ENTER_FIRST_NAME
import utils.report.StepHelper.FILL_BASIC_DETAILS
import utils.report.StepHelper.FILL_BASIC_DETAILS_CONTINUE
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class BasicDetailsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/onboard"

    private val firstNameInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name"))
    private val emailInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email"))


    fun enterFirstName(firstName: String): BasicDetailsPage {
        StepHelper.step(ENTER_FIRST_NAME + firstName)
        logger.info { "enterFirstName($firstName)" }
        firstNameInput.fill(firstName)
        return this
    }


    fun enterEmail(email: String): BasicDetailsPage {
        StepHelper.step(ENTER_EMAIL + email)
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

    fun fillDetails(firstName: String,email: String): BasicDetailsPage {
        StepHelper.step(FILL_BASIC_DETAILS)
        logger.info { "fillDetails($firstName, $email)" }
        utils.SignupDataStore.update {
            this.firstName = firstName
            this.email = email
        }
        enterFirstName(firstName)
        enterEmail(email)
        return this
    }


    fun clickContinue(): BasicDetailsPage {
        StepHelper.step(CLICK_CONTINUE)
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    fun fillBasicDetails(): PersonalDetailsPage {
        StepHelper.step(FILL_BASIC_DETAILS_CONTINUE)
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
