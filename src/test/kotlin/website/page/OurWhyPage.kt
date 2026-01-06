package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


class OurWhyPage(page: Page) : MarketingBasePage(page) {

    override val pageUrl = TestConfig.Urls.OUR_WHY

    val everyThingYouNeedToKnowCard = EveryThingYouNeedToKnowCard(page, EveryThingYouNeedToKnowPageType.OUR_WHY)
    val stopGuessingStartWithClaritySection = StopGuessingStartWithClaritySection(page,StopGuessingPageType.OUR_WHY)

    private val header = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("T a k e c h a r g e o f y o u r h e a l t h t o d a y")
    )

    private val mostPeopleDontWantSixPackAbsTextVisible = page.getByText(
        "Most people don’t want six-pack abs. They just want to wake up with energy. Focus without three cups of coffee. Eat without bloating. Feel sharp, light, and strong every day."
    )

    fun waitForPageLoad(): OurWhyPage {
        header.waitFor()
        logger.info { "Our Why page loaded" }
        return this
    }

    fun isHederVisible(): Boolean {
        return header.isVisible
    }


    fun waitForMostPeopleDontWantSixPackAbsText() {
        page.getByText("Most people don’t want six-pack abs. ").waitFor()
        page.getByText("They just want to wake up with energy.").waitFor()
        page.getByText("Focus without three cups of coffee.").waitFor()
        page.getByText("Eat without bloating.").waitFor()
        page.getByText("Feel sharp, light, and strong every day. ").waitFor()
    }

    fun isMostPeopleDontWantSixPackAbsTextVisible(): Boolean {
        return page.getByText("Most people don’t want six-pack abs. ").isVisible &&
                page.getByText("They just want to wake up with energy.").isVisible &&
                page.getByText("Focus without three cups of coffee.").isVisible &&
                page.getByText("Eat without bloating.").isVisible &&
                page.getByText("Feel sharp, light, and strong every day. ").isVisible
    }


    fun isWeLookAroundHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName("We looked around and saw the problem. Everyone had a part of the puzzle.")
        ).isVisible
    }

    fun isNoOneWasHelpingYouTextVisible(): Boolean {
        return page.getByText("No one was helping you see the full picture. Your doctor has your blood tests.").isVisible &&
                page.getByText("Your trainer has your workout plan.").isVisible &&
                page.getByText("Your watch tracks your heart rate.").isVisible &&
                page.getByText("Your gut, your genes, your sleep, your stress — all disconnected.").isVisible &&
                page.getByText("Everyone’s talking. No one’s listening.").isVisible &&
                page.getByText("And you’re the only one trying to make it all make sense.").isVisible
    }


    fun isWeBuildSomethingHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("So we built something new.")).isVisible
    }


    fun isCoverImageVisible(): Boolean {
        return page.locator(".dashboard-cover-l").isVisible
    }


    fun isSystemThatConnectHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "A system that connects it all. Your blood, your genetics, your gut, your habits, your goals — all in one place. Backed by data. Explained in your language. Made for action, not overwhelm."
            )
        ).isVisible
    }

    fun isWeBuiltDeepHolisticsHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "We built Deep Holistics because we wanted something simple."
            )
        ).isVisible
    }


    fun isToHelpPeopleLookBetterSectionVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "To help people look better, feel better, perform better every single day. - Not just to “avoid disease.” - Not to hit arbitrary fitness goals. - But to actually feel good waking up. - To have focus at 3PM without a fourth coffee. - To go out to eat without gut problems. To do hard things, without health holding them back."
            )
        ).isVisible
    }

    fun isThisIsNotAWellnessHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "This isn’t a wellness brand. It’s a performance system."
            )
        ).isVisible
    }


    fun forEverySingleDayTextVisible(): Boolean {
        return page.getByText("For everyday people who want to feel extraordinary.").isVisible &&
                page.getByText("Not someday. Every. Single. Day.").isVisible
    }


    fun weDidNotStartThisHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "\"We didn’t start this because we had perfect health. We started this because we didn’t. Because we were tired of watching the people around us run on fumes, run into walls, and still keep running\""
            )
        ).isVisible
    }


    fun isCeoNameVisible(): Boolean {
        return page.getByText("Sanath Kumar").isVisible && page.getByText("Co-Founder & CEO").isVisible
    }


    fun isWeAreNoteHereHeadingVisible(): Boolean {
        return page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(
                "We’re not here to make health sound cool. We’re here to make health work."
            )
        ).isVisible
    }


    fun whenPeopleAroundAsTextVisible(): Boolean {
        return page.getByText(
            "When the people around us are operating at 10/10, mentally, physically, emotionally, the world around them changes too.").isVisible &&
                page.getByText("That’s the vision.").isVisible &&
                page.getByText("That’s the work.").isVisible &&
                page.getByText("That’s Deep Holistics.").isVisible
    }


}
