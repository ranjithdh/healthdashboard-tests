package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class PersonalDetailsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    private val dateOfBirthLabel = byText("Date of Birth")
    private val genderInput = byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("Gender"))
    private val heightInput = byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm)"))
    private val weightInput = byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg)"))
    private val continueButton = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))

    fun selectDateOfBirth(month: String, year: String, day: String): PersonalDetailsPage {
        logger.info { "selectDateOfBirth($month/$day/$year)" }
        dateOfBirthLabel.click()
        page.getByLabel("Month:").selectOption(month)
        page.getByLabel("Year:").selectOption(year)
        byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).click()
        return this
    }

    fun selectGender(gender: String): PersonalDetailsPage {
        logger.info { "selectGender($gender)" }
        genderInput.click()
        byRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(gender).setExact(true)).click()
        return this
    }

    fun enterHeight(height: String): PersonalDetailsPage {
        logger.info { "enterHeight($height)" }
        heightInput.fill(height)
        return this
    }

    fun clearHeight(): PersonalDetailsPage {
        logger.info { "clearHeight()" }
        heightInput.clear()
        return this
    }

    fun enterWeight(weight: String): PersonalDetailsPage {
        logger.info { "enterWeight($weight)" }
        weightInput.fill(weight)
        return this
    }

    fun clearWeight(): PersonalDetailsPage {
        logger.info { "clearWeight()" }
        weightInput.clear()
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
        continueButton.click()
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
        dateOfBirthLabel.waitFor()
        return this
    }

    fun isDateOfBirthVisible(): Boolean {
        return dateOfBirthLabel.isVisible
    }

    fun isGenderVisible(): Boolean {
        return genderInput.isVisible
    }

    fun isHeightVisible(): Boolean {
        return heightInput.isVisible
    }

    fun isWeightVisible(): Boolean {
        return weightInput.isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return continueButton.isEnabled
    }
}
