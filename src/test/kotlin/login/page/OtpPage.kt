package login.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.Response
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import mobileView.home.HomePage
import forWeb.diagnostics.page.LabTestsPage
import mu.KotlinLogging
import profile.page.ProfilePage
import java.util.Scanner

private val logger = KotlinLogging.logger {}


class OtpPage(page: Page) : BasePage(page) {

    override val pageUrl = "/login"

    fun enterOtp(otp: String): OtpPage {
        logger.info { "enterOtp($otp)" }
        byRole(AriaRole.TEXTBOX).fill(otp)
        return this
    }

    fun clickContinue(): OtpPage {
        logger.info { "clickContinue()" }
        byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).click()
        return this
    }


    fun enterOtpAndContinueToAccountCreation(otp: String): BasicDetailsPage {
        enterOtp(otp)
        clickContinue()

        val basicDetailsPage = BasicDetailsPage(page)
        basicDetailsPage.waitForConfirmation()
        return basicDetailsPage
    }


    fun enterOtpAndContinueToMobileHomePage(otp: String): HomePage {
        enterOtp(otp)
        clickContinue()

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    fun enterOtpAndContinueToProfile(otp: String): ProfilePage {
        enterOtp(otp)
        clickContinue()

        val profilePage = ProfilePage(page)

        profilePage.waitForConfirmation()

        return profilePage
    }



    fun enterOtpAndContinueToHomePage(otp: String): HomePage {
        enterOtp(otp)
        clickContinue()

        val homePage = HomePage(page)
        homePage.waitForMobileHomePageConfirmation()

        return homePage
    }

    fun clickEdit(): LoginPage {
        logger.info { "clickEdit()" }
        byText("Edit").click()
        return LoginPage(page)
    }

    fun waitForConfirmScreen(): OtpPage {
        logger.info { "waitForConfirmScreen()" }
        byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Confirm your number")).waitFor()
        return this
    }

    fun isOnConfirmScreen(): Boolean {
        return byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Confirm your number")).isVisible
    }

    fun isOtpInputVisible(): Boolean {
        return byRole(AriaRole.TEXTBOX).isVisible
    }

    fun isResendTimerVisible(): Boolean {
        return byText("Resend code in").first().isVisible
    }

    fun getResendTimerText(): String? {
        return byText("Resend code in").first().textContent()
    }

    fun isEditButtonVisible(): Boolean {
        return byText("Edit").isVisible
    }

    fun isContinueButtonEnabled(): Boolean {
        return byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).isEnabled
    }

    fun isWhatsAppCheckboxVisible(): Boolean {
        return byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isVisible
    }

    fun isWhatsAppCheckboxChecked(): Boolean {
        return byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).isChecked
    }

    fun toggleWhatsAppCheckbox(): OtpPage {
        logger.info { "toggleWhatsAppCheckbox()" }
        byRole(AriaRole.CHECKBOX, Page.GetByRoleOptions().setName("Send OTP on WhatsApp")).click()
        return this
    }

    fun enterOtpAndContinueToLabTestForWeb(otp: String): LabTestsPage {
        enterOtp(otp)
        clickContinue()

        // Create LabTestsPage instance BEFORE navigation to set up response listener
        val labTestPage = LabTestsPage(page)

        // Set up response listener BEFORE navigation to capture API response
        var capturedResponse: Response? = null
        val listener = page.onResponse { response ->
            if (response.url().contains("https://api.stg.dh.deepholistics.com/v4/human-token/lab-test") && response.status() == 200) {
                capturedResponse = response
            }
        }

        // Navigate to diagnostics (API call happens during this navigation)
        page.navigate("https://app.stg.deepholistics.com/diagnostics")

        // Remove listener
//        listener.close()

        // Process captured response if available
        if (capturedResponse != null) {
            labTestPage.processApiResponse(capturedResponse!!)
        }

        labTestPage.waitForConfirmation()

        logger.info { "enterOtpAndContinueToHomePage($otp)...${page.url()}" }

        return labTestPage
    }

    fun isIncorrectOtpMessageVisible(): Boolean {
        return page.getByText("Incorrect OTP").isVisible
    }
}

