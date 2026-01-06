package website.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

enum class FooterPageType {
    LANDING,
    HOW_IT_WORKS,
    WHAT_WE_TEST,
    OUR_WHY,
}

class FooterSection(val page: Page, val footerPageType: FooterPageType) {

    private val deepHolistics = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Deep Holistics"))
    private val instagram = page.locator(".footer-social-link").first()
    private val linkedIn = page.locator(".row-regular > a:nth-child(2)")

    private val termsOfService = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Terms of Service"))
    private val privacyPolicy = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Privacy Policy"))


    fun getHowItWorksPage(): Locator {
        return when (footerPageType) {
            FooterPageType.LANDING -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("How it Works")).nth(1)
            }

            FooterPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("How it Works")).nth(1)
            }

            FooterPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("How it Works")).nth(1)
            }

            FooterPageType.OUR_WHY -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("How it Works")).nth(1)
            }
        }
    }

    fun getWhatWeTestPage(): Locator {
        return when (footerPageType) {
            FooterPageType.LANDING -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What We Test")).nth(1)
            }

            FooterPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What We Test")).nth(1)
            }

            FooterPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What We Test")).nth(1)
            }

            FooterPageType.OUR_WHY -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("What We Test")).nth(1)
            }
        }
    }

    fun getOurWhyPage(): Locator {
        return when (footerPageType) {
            FooterPageType.LANDING -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Our Why")).nth(1)
            }

            FooterPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Our Why")).nth(1)
            }

            FooterPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Our Why")).nth(1)
            }

            FooterPageType.OUR_WHY -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Our Why")).nth(1)
            }
        }
    }

    fun getMyDashBoard(): Locator {
        return when (footerPageType) {
            FooterPageType.LANDING -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("My Dashboard"))
            }

            FooterPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("My Dashboard"))
            }

            FooterPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("My Dashboard"))
            }

            FooterPageType.OUR_WHY -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("My Dashboard"))
            }
        }
    }

    fun getBookNow(): Locator {
        return when (footerPageType) {
            FooterPageType.LANDING -> {
                page.locator("#join-now-btn-footer")
            }

            FooterPageType.HOW_IT_WORKS -> {
                page.locator("#join-now-btn-footer")
            }

            FooterPageType.WHAT_WE_TEST -> {
                page.locator("#join-now-btn-footer")
            }

            FooterPageType.OUR_WHY -> {
                page.locator("#join-now-btn-footer")
            }
        }
    }

    fun getCareers(): Locator {
        return when (footerPageType) {
            FooterPageType.LANDING -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Careers"))
            }

            FooterPageType.HOW_IT_WORKS -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Careers"))
            }

            FooterPageType.WHAT_WE_TEST -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Careers"))
            }

            FooterPageType.OUR_WHY -> {
                page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Careers"))
            }
        }
    }


    fun isDeeHolisticsNameVisible(): Boolean {
        deepHolistics.scrollIntoViewIfNeeded()
        return deepHolistics.isVisible
    }

    fun isInstagramNameVisible(): Boolean {
        return instagram.isVisible
    }

    fun isLinkedInVisible(): Boolean {
        return linkedIn.isVisible
    }

    fun isSingaporeAddressVisible(): Boolean {
        return page.getByText("\uD83C\uDDF8\uD83C\uDDEC 20 Collyer Quay, #23-01, 20 Collyer Quay,").isVisible &&
                page.getByText("Singapore 049319").isVisible
    }

    fun isBengaluruAddressVisible(): Boolean {
        return page.getByText("\uD83C\uDDEE\uD83C\uDDF3 WeWork, 45/1, Magrath Road, Ashok Nagar,").isVisible &&
                page.getByText(" Bengaluru, Karnataka, India 560025").isVisible
    }

    fun isTermsOfServiceVisible(): Boolean {
        return termsOfService.isVisible
    }

    fun isPrivacyPolicyVisible(): Boolean {
        return privacyPolicy.isVisible
    }


    fun isAboutDeeHolisticsVisible(): Boolean {
        return page.getByText(
            "DEEP HOLISTICS IS A TECHNOLOGY PLATFORM DESIGNED TO HELP INDIVIDUALS BETTER UNDERSTAND THEIR HEALTH THROUGH DATA AND INSIGHTS. DEEP HOLISTICS IS NOT A LABORATORY OR A MEDICAL PROVIDER. ALL DIAGNOSTIC, LABORATORY, AND CLINICAL SERVICES ARE PROVIDED BY INDEPENDENT THIRD-PARTY PROVIDERS. DEEP HOLISTICS DOES NOT PROVIDE MEDICAL ADVICE, DIAGNOSIS, TREATMENT, OR LABORATORY SERVICES. THE INFORMATION, DATA, AND CONTENT MADE AVAILABLE THROUGH DEEP HOLISTICS IS INTENDED SOLELY FOR GENERAL INFORMATIONAL PURPOSES AND SHOULD NOT BE CONSIDERED A SUBSTITUTE FOR MEDICAL CARE, MEDICAL ADVICE, OR A CONSULTATION WITH YOUR PRIMARY CARE PHYSICIAN OR ANOTHER LICENSED PROVIDER. IF YOU HAVE QUESTIONS ABOUT YOUR RESULTS OR HEALTH CONCERNS, WE ENCOURAGE YOU TO SPEAK WITH A QUALIFIED MEDICAL PROFESSIONAL. BY SHARING YOUR CONTACT DETAILS, YOU CONSENT TO RECEIVE COMMUNICATIONS FROM DH. MESSAGE AND DATA RATES MAY APPLY."
        ).isVisible
    }

    fun isAllRightsReservedVisible(): Boolean {
        return page.getByText("© 2025 Deep Holistics Pte. Ltd. All rights reserved").isVisible
    }

    fun isHowItWorksMenuVisible(): Boolean {
        return getHowItWorksPage().isVisible
    }

    fun isWhatWhatWeTestMenuVisible(): Boolean {
        return getWhatWeTestPage().isVisible
    }

    fun isOurWhyMenuVisible(): Boolean {
        return getOurWhyPage().isVisible
    }

    fun isMyDashBoardVisible(): Boolean {
        return getMyDashBoard().isVisible
    }

    fun isBookNowVisible(): Boolean {
        return getBookNow().isVisible
    }

    fun isCareersVisible(): Boolean {
        return getCareers().isVisible
    }


    fun navigateToHowItWorksPage(): HowItWorksPage {
        val locator = getHowItWorksPage()
        locator.click()
        val howItWorksPage = HowItWorksPage(page)
        howItWorksPage.waitForPageLoad()
        return howItWorksPage
    }

    fun navigateToWhatWeTestPage(): WhatWeTestPage {
        val locator = getWhatWeTestPage()
        locator.click()
        val whatWeTestPage = WhatWeTestPage(page)
        whatWeTestPage.waitForPageLoad()
        return whatWeTestPage
    }

    fun navigateToOurWhyPage(): OurWhyPage {
        val locator = getOurWhyPage()
        locator.click()
        val ourWhyPage = OurWhyPage(page)
        ourWhyPage.waitForPageLoad()
        return ourWhyPage
    }

    fun navigateToMyDashboard(){
        val locator = getMyDashBoard()
        locator.click()
    }

    fun navigateToBookNow(){
        val locator = getBookNow()
        locator.click()
    }

    fun navigateToCareers(){
        val locator = getCareers()
        locator.click()
    }

}