package config

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import io.qameta.allure.Allure
import io.qameta.allure.Step
import mu.KotlinLogging
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

/**
 * Base page class with common functionality for all page objects.
 */
abstract class BasePage(protected val page: Page) {
    
    abstract val pageUrl: String

    /**
     * Navigate to this page
     */
    @Step("Navigate to {pageUrl}")
    open fun navigate(): BasePage {
        val fullUrl = pageUrl
        logger.info { "Navigating to: $fullUrl" }
        page.navigate(fullUrl)
        return this
    }
    
    /**
     * Wait for page to fully load
     */


    
    /**
     * Get element with auto-waiting
     */
    protected fun element(selector: String): Locator {
        return page.locator(selector)
    }
    
    /**
     * Get element by test ID
     */
    protected fun byTestId(testId: String): Locator {
        return page.getByTestId(testId)
    }
    
    /**
     * Get element by role
     */
    protected fun byRole(role: AriaRole, options: Page.GetByRoleOptions? = null): Locator {
        return page.getByRole(role, options)
    }
    
    /**
     * Get element by label text
     */
    protected fun byLabel(text: String): Locator {
        return page.getByLabel(text)
    }
    
    /**
     * Get element by placeholder
     */
    protected fun byPlaceholder(text: String): Locator {
        return page.getByPlaceholder(text)
    }
    
    /**
     * Get element by text content
     */
    protected fun byText(text: String): Locator {
        return page.getByText(text)
    }
    
    /**
     * Wait for element to be visible
     */
    protected fun waitForVisible(selector: String, timeout: Long = TestConfig.Timeouts.ELEMENT_TIMEOUT) {
        page.waitForSelector(selector,
            Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeout.toDouble())
        )
    }
    
    /**
     * Wait for element to be hidden
     */
    protected fun waitForHidden(selector: String, timeout: Long = TestConfig.Timeouts.ELEMENT_TIMEOUT) {
        page.waitForSelector(selector,
            Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(timeout.toDouble())
        )
    }
    

    
    /**
     * Take screenshot of current page state
     */
    @Step("Take screenshot: {name}")
    fun takeScreenshot(name: String): String {
        val path = "${TestConfig.Artifacts.SCREENSHOT_DIR}/${name}_${System.currentTimeMillis()}.png"
        page.screenshot(Page.ScreenshotOptions()
            .setPath(Paths.get(path))
            .setFullPage(true))
        logger.info { "Screenshot saved: $path" }
        
        // Attach screenshot to Allure report
        Allure.addAttachment(name, "image/png", java.io.FileInputStream(path), "png")
        return path
    }
    
    /**
     * Get current URL
     */
    fun currentUrl(): String = page.url()
    
    /**
     * Get page title
     */
    fun title(): String = page.title()
    
    /**
     * Check if element is visible
     */
    protected fun isVisible(selector: String): Boolean {
        return element(selector).isVisible
    }
    
    /**
     * Check if element exists in DOM
     */
    protected fun exists(selector: String): Boolean {
        return element(selector).count() > 0
    }
    
    /**
     * Get text content of element
     */
    protected fun getText(selector: String): String? {
        return element(selector).textContent()
    }
}
