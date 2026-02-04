package utils.screenshots

import com.microsoft.playwright.Page
import io.qameta.allure.Allure
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ScreenshotOnFailureExtension( //only for fail case
    private val pageProvider: () -> Page
) : AfterTestExecutionCallback {

    override fun afterTestExecution(context: ExtensionContext) {
        val throwable = context.executionException.orElse(null) ?: return  // ✅ only on failure

        val page = pageProvider()
        if (page.isClosed) return

        // Save screenshot directly in default allure-results folder
        val resultsDir: Path = Paths.get("build/allure-results")
        Files.createDirectories(resultsDir)

        val fileName = context.displayName.replace(" ", "_") + ".png"
        val screenshotPath = resultsDir.resolve(fileName)

        page.screenshot(Page.ScreenshotOptions().setPath(screenshotPath))

        // Attach screenshot to Allure report
        Allure.addAttachment(
            "Failure Screenshot",
            "image/png",
            Files.newInputStream(screenshotPath),
            ".png"
        )
    }
}


