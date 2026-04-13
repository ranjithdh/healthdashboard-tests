package config

import com.microsoft.playwright.Page
import org.junit.jupiter.api.extension.RegisterExtension
import utils.screenshots.ScreenshotOnTestCompletionExtension

abstract class BaseTest {
    // Each test class must override this
    protected lateinit var page: Page

    @RegisterExtension
    val screenshotExtension = ScreenshotOnTestCompletionExtension { page }
}