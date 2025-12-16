package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class BasicDetailsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    private val firstNameInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name"))
    private val lastNameInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Last name"))
    private val emailInput = byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email"))


    fun enterFirstName(firstName: String): BasicDetailsPage {
        logger.info { "enterFirstName($firstName)" }
        firstNameInput.fill(firstName)
        return this
    }

    fun enterLastName(lastName: String): BasicDetailsPage {
        logger.info { "enterLastName($lastName)" }
        lastNameInput.fill(lastName)
        return this
    }

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

    fun clearLastName(): BasicDetailsPage {
        logger.info { "clearLastName()" }
        lastNameInput.clear()
        return this
    }

    fun clearEmail(): BasicDetailsPage {
        logger.info { "clearEmail()" }
        emailInput.clear()
        return this
    }

    fun fillDetails(firstName: String, lastName: String, email: String): BasicDetailsPage {
        logger.info { "fillDetails($firstName, $lastName, $email)" }
        enterFirstName(firstName)
        enterLastName(lastName)
        enterEmail(email)
        return this
    }


    fun clickContinue(): BasicDetailsPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    fun fillAndContinue(firstName: String, lastName: String, email: String): PersonalDetailsPage {
        logger.info { "fillAndContinue()" }
        fillDetails(firstName, lastName, email)
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

    fun isLastNameVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Last name")).isVisible
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
