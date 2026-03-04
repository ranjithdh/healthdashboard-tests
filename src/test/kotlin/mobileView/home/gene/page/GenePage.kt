package mobileView.home.gene.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mobileView.diagnostics.TestDetailPage

class GenePage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.BIOMARKERS_URL


    fun emptyView() {
        val dnaHelixImg =
            page.getByRole(AriaRole.IMG, Page.GetByRoleOptions().setName("DNA helix"))

        val geneticInsightsHeading =
            page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Unlock your Genetic insights"))

        val discoverGenesText =
            page.getByText("Discover how your genes")

        val geneEmptyStateButton =
            page.getByTestId("gene-empty-state-view-test-button")

        listOf(
            dnaHelixImg,
            geneticInsightsHeading,
            discoverGenesText,
            geneEmptyStateButton
        ).forEach { it.waitFor() }

        geneEmptyStateButton.click()

        TestDetailPage(page)
            .waitGeneTabLoad()
            .clickBackButtonToHome()
    }

}