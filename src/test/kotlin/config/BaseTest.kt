package config

import com.microsoft.playwright.Page
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.RegisterExtension
import utils.report.StepHelper
import utils.screenshots.ScreenshotOnTestCompletionExtension

private val logger = KotlinLogging.logger {}

abstract class BaseTest {
    protected lateinit var page: Page

    @RegisterExtension
    val screenshotExtension = ScreenshotOnTestCompletionExtension { page }

    @BeforeEach
    fun beforeEachTest(testInfo: TestInfo) {
        logger.info { "[SET UP] ${testInfo.displayName}" }
        StepHelper.step("Test: ${testInfo.displayName}")
    }

    @AfterEach
    fun afterEachTest(testInfo: TestInfo) {
        logger.info { "[TEAR DOWN] ${testInfo.displayName}" }
        StepHelper.step("Completed: ${testInfo.displayName}")
    }
}