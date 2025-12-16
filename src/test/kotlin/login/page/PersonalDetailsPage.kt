package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class PersonalDetailsPage(page: Page) : BasePage(page) {




    override val pageUrl = "/login"
    override val pageLoadedSelector = "text=Date of Birth"

    fun selectDateOfBirth(month: String, year: String, day: String): PersonalDetailsPage {
        logger.info { "selectDateOfBirth($month/$day/$year)" }
        byText("Date of Birth").click()
        page.getByLabel("Month:").selectOption(month)
        page.getByLabel("Year:").selectOption(year)
        byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).click()
        return this
    }

    fun selectGender(gender: String): PersonalDetailsPage {
        logger.info { "selectGender($gender)" }
        byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("Gender")).click()
        byRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(gender).setExact(true)).click()
        return this
    }

    fun enterHeight(height: String): PersonalDetailsPage {
        logger.info { "enterHeight($height)" }
        byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm)")).fill(height)
        return this
    }

    fun enterWeight(weight: String): PersonalDetailsPage {
        logger.info { "enterWeight($weight)" }
        byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg)")).fill(weight)
        return this
    }

    fun fillDetails(
        gender: String = "Male",
        height: String = "170",
        weight: String = "60",
        month: String = "2",
        year: String = "1998",
        day: String = "12"
    ): PersonalDetailsPage {
        logger.info { "fillDetails()" }
        selectDateOfBirth(month, year, day)
        selectGender(gender)
        enterHeight(height)
        enterWeight(weight)
        return this
    }

    fun clickContinue(): PersonalDetailsPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }

    /**
     * Fill details and continue to Address page
     */
    fun fillAndContinue(
        gender: String = "Male",
        height: String = "170",
        weight: String = "60",
        month: String = "2",
        year: String = "1998",
        day: String = "12"
    ): AddressPage {
        logger.info { "fillAndContinue()" }
        fillDetails(gender, height, weight, month, year, day)
        clickContinue()

        val addressPage = AddressPage(page)
        addressPage.waitForConfirmation()
        return addressPage
    }

    fun waitForConfirmation(): PersonalDetailsPage {
        byText("Date of Birth").waitFor()
        return this
    }

    fun isDateOfBirthVisible(): Boolean {
        return byText("Date of Birth").isVisible
    }

    fun isGenderVisible(): Boolean {
        return byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("Gender")).isVisible
    }

    fun isHeightVisible(): Boolean {
        return byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm)")).isVisible
    }

    fun isWeightVisible(): Boolean {
        return byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg)")).isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isEnabled
    }
}
