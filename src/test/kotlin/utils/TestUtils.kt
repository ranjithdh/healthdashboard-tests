package utils

import com.microsoft.playwright.Page
import config.TestConfig
import mu.KotlinLogging
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

object TestUtils {

    fun saveVideo(page: Page, testName: String) {
        if (TestConfig.Artifacts.RECORD_VIDEO) {
            try {
                // Ensure context or page is partially closed or just save? 
                // Documentation says: "The video is available after the page or browser context is closed."
                // But we can call `video.saveAs()` to save it immediately if it works, or we need to close context FIRST.
                // However, `video.saveAs` usually waits or we assume we call this BEFORE context close, but it might block?
                // actually doc says: "Video is saved upon browserContext.close()."
                // AND "You can access the video file... path = page.video().path()"
                // "Note that the video is only available after the page or browser context is closed."
                
                // So the flow must be:
                // 1. Run Test
                // 2. Context.close() (Video is saved to random name)
                // 3. Rename random name to test name? 
                
                // WAIT. If we close context, `page` object might be unusable? 
                // "page.video()" returns the video object.
                // Let's rely on valid Video object existence.
                
                // Actually, `page.video().saveAs(path)` documentation says: 
                // "Saves the video to the specified path. The video is available only after the page or browser context is closed."
                // This implies `saveAs` might hang until close? OR we call it after close?
                // But we can't call `page.video()` after `context.close()` because page is closed?
                // Actually `page.video()` returns a handle.
                
                // Correct pattern often is:
                // Video video = page.video();
                // context.close();
                // video.saveAs(path);
                
                val video = page.video()
                if (video != null) {
                    logger.info { "Video recording enabled for $testName" }
                    // We need to return the video object or handle saving outside?
                    // Or we pass `context` to this method and close it here?
                    // Let's assume the caller will close the context AFTER this method if we just get the handle?
                    // No, cleaner is to pass context here? 
                    // Or simpler: The user wants a common function.
                    
                    // Let's try: 
                    // val video = page.video()
                    // video.saveAs(path) -> blocks until close? No, usually allows setting path.
                    // doc says "Saves the video... available only after... closed".
                    // It implies `saveAs` will wait or move it once available.
                    
                    val videoPath = Paths.get(TestConfig.Artifacts.VIDEO_DIR, "$testName.webm")
                    // video.saveAs(videoPath) // Doing this might require context to be closed first if it blocks.
                    
                    // Let's check Playwright Java specific behavior if possible.
                    // Generally in JS: await video.saveAs() waits for close.
                    // In Java: video.saveAs() likely waits.
                    
                    // So we should:
                    // 1. Get video
                    // 2. Close context
                    // 3. Save video
                    
                    // But we can't close context inside `saveVideo` easily if we want to keep `AfterEach` clean?
                    // Ideally `saveVideo` takes `context` and `page`.
                    // But `AfterEach` in tests has `context.close()`.
                    
                    // Let's do this: 
                    // The test calls `TestUtils.saveVideo(page, context, "testName")`
                    // This method gets video, closes context, sets path/deletes old.
                }
            } catch (e: Exception) {
                logger.error { "Failed to save video: ${e.message}" }
            }
        }
    }
    
    fun closeContextAndSaveVideo(page: Page, context: com.microsoft.playwright.BrowserContext, testName: String) {
        if (TestConfig.Artifacts.RECORD_VIDEO) {
            try {
                val video = page.video()
                context.close() // Close first so video is saved/finishes
                if (video != null) {
                     val videoPath = Paths.get(TestConfig.Artifacts.VIDEO_DIR, "$testName.webm")
                     // Ensure dir exists
                     videoPath.parent.toFile().mkdirs()
                     video.saveAs(videoPath)
                     logger.info { "Saved video to $videoPath" }
                }
            } catch (e: Exception) {
                logger.error { "Failed to save video: ${e.message}" }
                // Ensure context is closed if error
                try { context.close() } catch (ignored: Exception) {}
            }
        } else {
            context.close()
        }
    }
}
