package utils.screenshots

import com.microsoft.playwright.Page
import io.qameta.allure.Allure
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ScreenshotOnTestCompletionExtension(//for both fail and pass case
    private val pageProvider: () -> Page
) : AfterTestExecutionCallback {

    override fun afterTestExecution(context: ExtensionContext) {
        val page = pageProvider()
        if (page.isClosed) return

        // Create Allure results directory
        val resultsDir: Path = Paths.get("build/allure-results")
        Files.createDirectories(resultsDir)

        val status = if (context.executionException.isPresent) "FAIL" else "PASS"
        val fileName = "${context.displayName.replace(" ", "_")}_$status.png"
        val screenshotPath = resultsDir.resolve(fileName)

        // Take screenshot
        page.screenshot(Page.ScreenshotOptions().setPath(screenshotPath))

        // Attach to Allure
        Allure.addAttachment(
            "Screenshot ($status)",
            "image/png",
            Files.newInputStream(screenshotPath),
            ".png"
        )
    }
}