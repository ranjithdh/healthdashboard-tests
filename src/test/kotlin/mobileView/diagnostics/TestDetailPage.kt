package mobileView.diagnostics

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import utils.report.StepHelper

class TestDetailPage(page: Page) : BasePage(page) {

    override val pageUrl = "" // URL might be dynamic, so leaving empty or generic

    fun verifyHowItWorksSection() {
        StepHelper.step("${StepHelper.VERIFY_SECTION} How it Works?")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it Works?")).click()
        
        // Step 01
        StepHelper.step("${StepHelper.VERIFY_STEP} 01: At-Home Sample Collection")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("01")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("At-Home Sample Collection")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("At-Home Sample Collection")).click()
        page.getByText("Schedule the blood sample").click()
        
        // Step 02
        StepHelper.step("${StepHelper.VERIFY_STEP} 02: Get results in 72 hrs")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("02")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Get results in 72 hrs")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Get results in 72 hrs")).click()
        page.getByText("Your sample is processed at a").click()
        
        // Step 03
        StepHelper.step("${StepHelper.VERIFY_STEP} 03: 1-on-1 Expert Consultation")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("03")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("-on-1 Expert Consultation")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("-on-1 Expert Consultation")).click()
        page.getByText("See how your antibody levels").click()
        
        // Step 04
        StepHelper.step("${StepHelper.VERIFY_STEP} 04: Track Progress Overtime")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("04")).click()
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Track Progress Overtime")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Track Progress Overtime")).click()
        page.getByText("Monitor these markers over").click()
    }

    fun verifyCertifiedLabsSection() {
        StepHelper.step("${StepHelper.VERIFY_SECTION} Certified Labs, Secure Data")
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Certified Labs, Secure Data")).click()
        
        // Certified Labs
        StepHelper.step(StepHelper.VERIFY_LABS_INFO)
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("NABL and CAP Certified")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("NABL and CAP Certified")).click()
        page.getByText("Each partner lab we work with").click()
        
        // Privacy
        StepHelper.step(StepHelper.VERIFY_PRIVACY_INFO)
        page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Your privacy matters")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Your privacy matters")).click()
        page.getByText("Your health data is always").click()
    }
}
