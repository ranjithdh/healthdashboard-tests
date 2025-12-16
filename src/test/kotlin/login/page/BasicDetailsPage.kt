package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class BasicDetailsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"
    override val pageLoadedSelector =  byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Set up your account")).toString()


    fun enterFirstName(firstName: String): BasicDetailsPage {
        logger.info { "enterFirstName($firstName)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name")).fill(firstName)
        return this
    }

    fun enterLastName(lastName: String): BasicDetailsPage {
        logger.info { "enterLastName($lastName)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Last name")).fill(lastName)
        return this
    }

    fun enterEmail(email: String): BasicDetailsPage {
        logger.info { "enterEmail($email)" }
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Email")).fill(email)
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
        byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name")).waitFor()
        return this
    }


    fun isFirstNameVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Enter name")).isVisible
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
}
