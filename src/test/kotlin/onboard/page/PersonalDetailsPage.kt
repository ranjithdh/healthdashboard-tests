package onboard.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import config.TestUser
import io.qameta.allure.Step
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class PersonalDetailsPage(page: Page) : BasePage(page) {

    override val pageUrl = "/onboard"

    private val dateOfBirthLabel = byText("Date of Birth")
    private val genderInput = byRole(AriaRole.COMBOBOX, Page.GetByRoleOptions().setName("Gender"))
    private val heightInput = byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Height (cm)"))
    private val weightInput = byRole(AriaRole.SPINBUTTON, Page.GetByRoleOptions().setName("Weight (kg)"))
    private val continueButton = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))

    @Step("Select Date of Birth: {month}/{day}/{year}")
    fun selectDateOfBirth(month: String, year: String, day: String): PersonalDetailsPage {
        logger.info { "selectDateOfBirth($month/$day/$year)" }
        dateOfBirthLabel.click()
        page.getByLabel("Month:").selectOption(month)
        page.getByLabel("Year:").selectOption(year)
        byRole(AriaRole.GRIDCELL, Page.GetByRoleOptions().setName(day)).click()
        return this
    }

    @Step("Select Gender: {gender}")
    fun selectGender(gender: String): PersonalDetailsPage {
        logger.info { "selectGender($gender)" }
        genderInput.click()
        byRole(AriaRole.OPTION, Page.GetByRoleOptions().setName(gender).setExact(true)).click()
        return this
    }

    @Step("Enter Height: {height}")
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

    @Step("Enter Weight: {weight}")
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

    @Step("Fill Personal Details")
    fun fillDetails(
        gender: String = "Male",
        height: String = "170",
        weight: String = "60",
        month: String = "2",
        year: String = "1998",
        day: String = "12"
    ): PersonalDetailsPage {
        logger.info { "fillDetails()" }
        utils.SignupDataStore.update {
            this.gender = gender
            this.height = height
            this.weight = weight
            this.month = month
            this.year = year
            this.day = day
        }
        selectDateOfBirth(month, year, day)
        selectGender(gender)
        enterHeight(height)
        enterWeight(weight)
        return this
    }

    @Step("Click Continue")
    fun clickContinue(): PersonalDetailsPage {
        logger.info { "clickContinue()" }
        continueButton.click()
        return this
    }


    @Step("Fill personal details and continue")
    fun fillPersonalDetails(testUser: TestUser = TestConfig.TestUsers.NEW_USER): AddressPage {
        logger.info { "fillPersonalDetails()" }
        fillDetails(
            testUser.gender,
            testUser.height,
            testUser.weight,
            testUser.month,
            testUser.year,
            testUser.day
        )
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
