package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig


/**
 * Base class for marketing website pages (www.deepholistics.com)
 * Provides common navigation and header/footer functionality
 */
abstract class MarketingBasePage(page: Page) : BasePage(page) {

    /**
     * Navigate to this marketing page
     */
    override fun navigate(): MarketingBasePage {
        val fullUrl = pageUrl
        page.navigate(fullUrl)
        return this
    }

    // ---------------------- Header Navigation ----------------------

    fun isLogoVisible(): Boolean {
        return element("a[aria-label='home']").isVisible
    }

    fun clickLogo(): MarketingBasePage {
        element("a[aria-label='home']").click()
        return this
    }

    fun isHowItWorksLinkVisible(): Boolean {
        return element("a#nav-how-it-works").isVisible
    }

    fun clickHowItWorksLink(): HowItWorksPage {
        element("a#nav-how-it-works").click()
        return HowItWorksPage(page)
    }

    fun isWhatWeTestLinkVisible(): Boolean {
        return element("a#nav-what-we-test").isVisible
    }

    fun clickWhatWeTestLink(): WhatWeTestPage {
        element("a#nav-what-we-test").click()
        return WhatWeTestPage(page)
    }

    fun isOurWhyLinkVisible(): Boolean {
        return page.getByRole(AriaRole.BANNER).getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Our Why")).isVisible
    }

    fun clickOurWhyLink(): OurWhyPage {
        page.getByRole(AriaRole.BANNER).getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Our Why")).click()
        return OurWhyPage(page)
    }

    fun isFaqLinkVisible(): Boolean {
        return element("a[href='/faq']").isVisible
    }

    fun clickFaqLink(): FaqPage {
        element("a[href='/faq']").click()
        return FaqPage(page)
    }

    fun isLoginLinkVisible(): Boolean {
        return element("a#nav-login").isVisible
    }

    fun clickLoginLink() {
        element("a#nav-login").click()
    }

    fun isHeaderBookNowVisible(): Boolean {
        return element("a#join-now-btn-header").isVisible
    }

    fun clickHeaderBookNow() {
        element("a#join-now-btn-header").click()
    }

    // ---------------------- Footer ----------------------

    fun isFooterVisible(): Boolean {
        return element("footer").isVisible
    }

    fun isPrivacyPolicyFooterLinkVisible(): Boolean {
        return element("a[href='/privacy']").isVisible
    }

    fun isTermsFooterLinkVisible(): Boolean {
        return element("a[href='/terms']").isVisible
    }

    // ---------------------- Intercom Chat ----------------------

    fun isIntercomChatVisible(): Boolean {
        return element("div[aria-label='Open Intercom Messenger']").isVisible
    }

    fun clickIntercomChat() {
        element("div[aria-label='Open Intercom Messenger']").click()
    }
}
