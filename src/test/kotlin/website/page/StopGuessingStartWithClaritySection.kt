package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole


enum class StopGuessingPageType {
    LANDING,
    HOW_IT_WORKS,
    WHAT_WE_TEST,
    OUR_WHY
}

class StopGuessingStartWithClaritySection(val page: Page,val pageType: StopGuessingPageType) {

    fun getBookNowButton(): Locator {
        return when (pageType) {
            StopGuessingPageType.LANDING -> {
                page.locator("#join-now-btn-foot-hero")
            }

            StopGuessingPageType.HOW_IT_WORKS -> {
                page.locator("#join-hiw-footer")
            }

            StopGuessingPageType.WHAT_WE_TEST -> {
                page.locator("#join-btn-test-footer")
            }
            StopGuessingPageType.OUR_WHY -> {
                page.locator("#join-our-why-footer")
            }
        }
    }

    fun stopGuessingSectionElementsVisible(): Boolean {
        val header = page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("S t o p g u e s s i n g . S t a r t w i t h c l a r i t y .")
        )
        val description =
            page.getByText("It’s time to reclaim control and address what’s holding you back so you can look, feel and perform 10/10, day after day")
        header.waitFor()

        return header.isVisible && description.isVisible
    }

    fun stopGuessingBookNowButtonVisible(): Boolean {
        return getBookNowButton().isVisible
    }

    fun clickStopGuessingBookNowButtonVisible() {
        getBookNowButton().click()
    }

}