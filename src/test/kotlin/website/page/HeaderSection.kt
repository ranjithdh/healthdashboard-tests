package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole


class HeaderSection(val page: Page) {

    private val bookNow = page.locator("#join-now-btn-header")
    private val howItWorksPage = page.locator("#nav-how-it-works")
    private val whatWeTestPage = page.locator("#nav-what-we-test")

    private val faqPage = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("FAQs"))
    private val login= page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Login"))
    private val landingPage = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("home"))
    private val ourWhyPage = page.getByRole(AriaRole.NAVIGATION).getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Our Why"))


    fun isBookNowVisible() = bookNow.isVisible
    fun isHowItWorksPageVisible() = howItWorksPage.isVisible
    fun isWhatWeTestPageVisible() = whatWeTestPage.isVisible
    fun isFaqPageVisible() = faqPage.isVisible
    fun isLoginVisible() = login.isVisible
    fun isLandingPageVisible() = landingPage.isVisible
    fun isOurWhyPageVisible() = whatWeTestPage.isVisible


    fun navigateToHowItWorksPage(): HowItWorksPage {
        val locator = howItWorksPage
        locator.click()
        val howItWorksPage = HowItWorksPage(page)
        howItWorksPage.waitForPageLoad()
        return howItWorksPage
    }

    fun navigateToWhatWeTestPage(): WhatWeTestPage {
        val locator = whatWeTestPage
        locator.click()
        val whatWeTestPage = WhatWeTestPage(page)
        whatWeTestPage.waitForPageLoad()
        return whatWeTestPage
    }

    fun navigateToOurWhyPage(): OurWhyPage {
        val locator = ourWhyPage
        locator.click()
        val ourWhyPage = OurWhyPage(page)
        ourWhyPage.waitForPageLoad()
        return ourWhyPage
    }

    fun navigateToFaq(): FaqPage {
        val locator = faqPage
        locator.click()
        val faqPage = FaqPage(page)
        faqPage.waitForPageLoad()
        return faqPage
    }

    fun navigateToLanding(): LandingPage {
        val locator = landingPage
        locator.click()
        val landingPage = LandingPage(page)
        landingPage.waitForPageLoad()
        return landingPage
    }

    fun navigateToBookNow() {
        bookNow.click()
    }

    fun navigateToLogin() {
        login.click()
    }

}