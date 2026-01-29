package utils.screenshot


import com.microsoft.playwright.Page
import io.qameta.allure.Allure
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Files
import java.nio.file.Paths

class ScreenshotOnFailureExtension(
    private val pageProvider: () -> Page
) : AfterTestExecutionCallback {

    override fun afterTestExecution(context: ExtensionContext) {
        val throwable = context.executionException.orElse(null)
        if (throwable == null) return   // ✅ only on failure

        val page = pageProvider()

        if (page.isClosed) return

        val version = System.getProperty("app.version", "local")

        val dir = Paths.get("build/allure-results", version)
        Files.createDirectories(dir)

        val path = dir.resolve(
            "${context.displayName.replace(" ", "_")}.png"
        )

        page.screenshot(Page.ScreenshotOptions().setPath(path))

        Allure.addAttachment(
            "Failure Screenshot",
            "image/png",
            Files.newInputStream(path),
            ".png"
        )
    }
}