package login.test.fullflow

import com.microsoft.playwright.*
import config.TestConfig
import home.test.checkBloodTestBookedCardStatus
import login.page.LoginPage
import org.junit.jupiter.api.*
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SignUpFlowTest {

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
        context.setDefaultTimeout(TestConfig.Browser.TIMEOUT)
        page = context.newPage()

        val videoPath = page.video()?.path()
        println("📹 Video saved to: $videoPath")
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    @Test
    fun `should complete full signup flow`() {
        val loginPage = LoginPage(page).navigate() as LoginPage

        val homePage = loginPage
            .clickSignUp()
            .enterMobileAndContinue("726408303")
            .enterOtpAndContinueToAccountCreation("")
            .fillAndContinue("ranjith", "test", "ranjithkumar.m@mysmitch.com")
            .fillAndContinue("Male", "170", "60")
            .fillAndContinue("Flat 101","456 Main Road", "Delhi", "Delhi", "110001")
            .selectSlotsAndContinue()
            .clickContinue()
            .waitForMobileHomePageConfirmation()


        checkBloodTestBookedCardStatus(homePage)

        homePage.takeScreenshot("signup-order-placed")
    }

}
