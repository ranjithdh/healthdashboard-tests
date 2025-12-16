package profile.page

import com.microsoft.playwright.Page
import config.BasePage
import config.TestConfig

class ProfilePage(page: Page) : BasePage(page) {


    override val pageUrl = TestConfig.Urls.PROFILE_PAGE_URL

    private val tonePreference = byText("Tone Preference")

    fun waitForConfirmation(): ProfilePage {
        tonePreference.waitFor()
        return this
    }



}









