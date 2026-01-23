package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole


class CertifiedLabsSection(val page: Page) {

    fun isHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Certified Labs. Secure Data.")).isVisible
    }

    fun isNABLAndCAPCertifiedLaboratoriesVisible(): Boolean {
        return page.getByText("NABL and CAP Certified Laboratories").isVisible
    }

    fun isNABLAndCAPCertifiedLaboratoriesDescriptionVisible(): Boolean {
        return page.getByText(
            "Each partner lab we work with is CAP-accredited and NABL-certified. This means they follow rigorous international and national quality standards, maintain state-of-the-art testing protocols, and undergo regular audits to ensure accuracy and reliability."
        ).isVisible
    }

    fun isYourPrivacyMattersVisible(): Boolean {
        return page.getByText("Your privacy matters").isVisible
    }

     fun isYourPrivacyMattersDescriptionVisible(): Boolean {
        return page.getByText(
            "Your health data is always protected with strict privacy safeguards. We use advanced encryption and secure servers to keep your results confidential and accessible only to you. Every step of the process is designed with your privacy in mind."
        ).isVisible
    }

}