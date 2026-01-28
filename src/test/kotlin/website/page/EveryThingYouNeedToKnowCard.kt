package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

enum class EveryThingYouNeedToKnowPageType {
    LANDING,
    HOW_IT_WORKS,
    OUR_WHY
}

class EveryThingYouNeedToKnowCard(val page: Page, val pageType: EveryThingYouNeedToKnowPageType) {

    fun getBookNowButton(): Locator {
        return when (pageType) {
            EveryThingYouNeedToKnowPageType.LANDING -> {
                page.locator("#join-now-btn-membership-pricing")
            }

            EveryThingYouNeedToKnowPageType.HOW_IT_WORKS -> {
                page.locator("#join-hiw-pricing")
            }

            EveryThingYouNeedToKnowPageType.OUR_WHY -> {
                page.locator("#join-our-why-pricing")
            }
        }
    }

    fun isEverythingYouNeedToKnowHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("Everything you need to know")
        ).isVisible
    }

    fun isEverythingYouNeedToKnowDescriptionVisible(): Boolean {
        return page.getByText("Baseline brings together a preventive health system to give you clarity and help you take action with confidence.").isVisible
    }

    fun isWhatsIncludedPointsVisible(): Boolean {
        return page.getByText("What's Included", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Advanced Biomarker at-home").isVisible &&
                page.getByText(":1 Expert Consults for all the tests").isVisible &&
                page.getByText("Longevity platform to manage").isVisible &&
                page.getByText("Add-on tests to help you go").isVisible &&
                page.getByText("DH Reward points and").isVisible &&
                page.getByText("This isn’t a check-up. It’s").isVisible &&
                page.getByText("₹9,999", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun isEveryThingYouNeedToKnowBookNowVisible(): Boolean {
        return getBookNowButton().isVisible
    }

    fun clickEveryThingYouNeedToKnowBookNow() {
        getBookNowButton().click()
    }
}