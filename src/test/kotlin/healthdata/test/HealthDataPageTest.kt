package healthdata.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import config.BaseTest
import config.TestConfig
import healthdata.page.HealthDataPage
import login.page.LoginPage
import login.page.OtpPage
import model.healthdata.Biomarker
import org.junit.jupiter.api.*
import utils.BiomarkerCsvParser
import utils.logger.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HealthDataPageTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var healthDataPage: HealthDataPage

    private val csvPath = "/Users/apple/Downloads/Deep-Holistics-Biomarkers-export.csv"

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
        val viewport = TestConfig.Viewports.DESKTOP_HD
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        loginAndNavigateToHealthData()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }

    private fun loginAndNavigateToHealthData() {
        val testUser = TestConfig.TestUsers.EXISTING_USER

        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(testUser)

        val otpPage = OtpPage(page)
        healthDataPage = otpPage.enterOtpAndContinueToHealthData(testUser)
    }

    @Test
    @Order(1)
    fun `should download biomarker report`() {
        healthDataPage.waitForPageLoad()

        val download = healthDataPage.downloadReport()

        val suggestedFilename = download.suggestedFilename()
        println("Suggested filename: $suggestedFilename")

        assertTrue(
            suggestedFilename.contains("Deep-Holistics-Biomarkers-export"),
            "Filename should contain 'Biomarkers-export'"
        )
        assertTrue(suggestedFilename.endsWith(".csv"), "File should be a CSV")

        val downloadPath = Paths.get(csvPath)
        Files.deleteIfExists(downloadPath)

        download.saveAs(downloadPath)

        assertTrue(Files.exists(downloadPath), "Downloaded file should exist at $csvPath")
        assertTrue(Files.size(downloadPath) > 0, "Downloaded file should not be empty")

        println("Download successful: $downloadPath")
    }

    @Test
    @Order(2)
    fun `should verify all biomarkers from CSV`() {
        if (!File(csvPath).exists()) {
            println("CSV file not found at $csvPath. Skipping verification test.")
            return
        }

        val biomarkers = BiomarkerCsvParser.parse(csvPath)

        logger.info {
            "BiomarkerCsvParser...${biomarkers.joinToString("\n")}"
        }

        println("Verifying ${biomarkers.size} biomarkers...")

        val failures = mutableListOf<String>()
        var processedCount = 0

        biomarkers.forEach { biomarker ->
            processedCount++
            try {
                verifyBiomarker(biomarker)
                if (processedCount % 10 == 0) {
                    println("Processed $processedCount/${biomarkers.size} biomarkers...")
                }
            } catch (e: AssertionError) {
                val msg = "Biomarker '${biomarker.name}' FAILED: ${e.message}"
                println(msg)
                failures.add(msg)
                if (failures.size <= 5) {
                    healthDataPage.takeScreenshot("failure-${biomarker.name.replace(" ", "_").replace("/", "-")}")
                }
            } catch (e: Exception) {
                val msg = "Biomarker '${biomarker.name}' ERROR: ${e.message}"
                println(msg)
                failures.add(msg)
                if (failures.size <= 5) {
                    healthDataPage.takeScreenshot("error-${biomarker.name.replace(" ", "_").replace("/", "-")}")
                }
            }
        }

        if (failures.isNotEmpty()) {
            val failureMessage = "Found ${failures.size} biomarker mismatches:\n${failures.joinToString("\n")}"
            Assertions.fail<String>(failureMessage)
        } else {
            println("All ${biomarkers.size} biomarkers verified successfully!")
        }
    }

    private fun verifyBiomarker(biomarker: Biomarker) {
        if (biomarker.systemName.isNotBlank()) {
            try {
                healthDataPage.clickSystemTab(biomarker.systemName)
            } catch (_: Exception) {
            }
        }

        healthDataPage.scrollToBiomarker(biomarker.name)

        val isVisible = healthDataPage.isBiomarkerVisible(biomarker.name)
        Assertions.assertTrue(isVisible, "Biomarker should be visible")

        val matches = healthDataPage.verifyBiomarkerData(
            name = biomarker.name,
            expectedValue = biomarker.formattedValue(),
            expectedStatus = biomarker.status,
            expectedRange = biomarker.idealRange
        )

        Assertions.assertTrue(matches, "Data mismatch")
    }
}