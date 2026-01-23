package login.test.fullflow

import com.microsoft.playwright.*
import config.TestConfig
import login.page.LoginPage
import org.junit.jupiter.api.*
import utils.TestUtils
import java.nio.file.Paths



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginFlowTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var page: Page

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
    }

    @AfterAll
    fun tearDown() {
        browser.close()
        playwright.close()
    }

    @BeforeEach
    fun createContext() {
        val viewport = TestConfig.Viewports.ANDROID
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)
            .setRecordVideoDir(Paths.get(TestConfig.Artifacts.VIDEO_DIR))
            .setRecordVideoSize(390, 844)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        val videoPath = page.video()?.path()
        println("📹 Video saved to: $videoPath")

    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    @Test
    fun `login flow`() {
        val tesUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage
      loginPage
            .enterMobileAndContinue(tesUser.mobileNumber)
            .enterOtpAndContinueToMobileHomePage(tesUser.otp)

    }
}

