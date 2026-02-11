package healthdata.test

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Tracing.StartOptions
import com.microsoft.playwright.Tracing.StopOptions
import com.microsoft.playwright.options.AriaRole
import config.BaseTest
import config.TestConfig
import healthdata.page.HealthDataPage
import onboard.page.LoginPage
import model.healthdata.Biomarker
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import utils.BiomarkerCsvParser
import utils.logger.logger
import utils.waitUntilDetached
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

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

        val viewport = TestConfig.Viewports.DESKTOP_FHD
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        loginAndNavigateToHealthData()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    private fun loginAndNavigateToHealthData() {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue(testUser)
        healthDataPage = otpPage.enterOtpAndContinueToWebViewHealthData(testUser)
    }


    @Test
    @Order(1)
    fun `show empty state if the health data is empty`() {
        if (healthDataPage.healthData?.data?.blood?.data?.isEmpty() == true) {
            assertTrue(healthDataPage.shouldShowEmptyState(), "Empty state")
            assertTrue(healthDataPage.shouldShowTrackResult(), "show track result")

            healthDataPage.clickTrackResult()

            page.waitForURL {
                page.url().contains(TestConfig.Urls.TRACK_RESULT)
            }

            println("checkEmptyState...............${page.url()}")
            assertTrue(
                page.url().contains(TestConfig.Urls.TRACK_RESULT),
                "Should navigate to track result, but was ${page.url()}"
            )
        } else {
            assertTrue(true, "Health data found")
        }
    }

    @Test
    @Order(2)
    fun `should download biomarker report`() {

        if (healthDataPage.healthData?.data?.blood?.data?.isNotEmpty() == true) {
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
        } else {
            assertTrue(true, "Health data not found")
        }

    }

    @Test
    @Order(3)
    fun `should verify all biomarkers from CSV`() {

        if (healthDataPage.healthData?.data?.blood?.data?.isNotEmpty() == true) {

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
        } else {
            assertTrue(true, "Health data not found")
        }
    }


    @Test
    @Order(4)
    fun `should show update waist circumference description and update dialog for fatty liver index`() {

        val description = page.getByText("Add waist circumference to calculate FLI")

        if (description.isVisible){
            val fattyLiverLocator = healthDataPage.getBiomarkerRow("Fatty Liver Index (FLI)")
            if (fattyLiverLocator != null) {

                if (
                    fattyLiverLocator.textContent()
                        .contains(page.getByText("—").first().innerText()) && fattyLiverLocator.textContent()
                        .contains(page.getByText("—").nth(1).innerText())
                ) {
                    assertTrue(description.isVisible) { "add waist circumference to calculate FLI should visible" }

                    description.click()

                    assertTrue(
                        page.getByRole(
                            AriaRole.HEADING,
                            Page.GetByRoleOptions().setName("Waist Circumference")
                        ).isVisible
                    )
                    assertTrue(page.getByText("What is your waist circumference at its narrowest point (just above your hips)?").isVisible)
                    assertTrue(page.getByText("Value in inches (20-54)").isVisible)

                    val textBox = page.getByRole(AriaRole.TEXTBOX).nth(1)
                    val saveButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save"))
                    val closePopup = page.locator(".text-muted-foreground.hover\\:text-foreground")

                    textBox.fill("19")
                    assertTrue(page.getByText("Enter value between 20-54").isVisible)
                    assertTrue(saveButton.isDisabled)

                    textBox.clear()

                    textBox.fill("55")
                    assertTrue(page.getByText("Enter value between 20-54").isVisible)
                    assertTrue(saveButton.isDisabled)

                    textBox.fill("30")
                    assertTrue(saveButton.isEnabled)

                    closePopup.click()
                    description.click()

                    assertTrue(textBox.inputValue() == "30","30")
                    assertTrue(saveButton.isEnabled)

                    saveButton.click()

                }

                description.waitUntilDetached()
                assertTrue(!description.isVisible) { "add waist circumference to calculate FLI should not be visible" }
            }
        }


    }

    private fun verifyBiomarker(biomarker: Biomarker) {
        if (biomarker.systemName.isNotBlank()) {
            try {
                healthDataPage.clickSystemTab(biomarker.systemName)
            } catch (_: Exception) {
                println("Failed to click system biomarker: ${biomarker.name}")
            }
        }

        healthDataPage.scrollToBiomarker(biomarker.name)

        val isVisible = healthDataPage.isBiomarkerVisible(biomarker.name)
        assertTrue(isVisible, "Biomarker should be visible")

        val matches = healthDataPage.verifyBiomarkerData(
            name = biomarker.name,
            expectedValue = biomarker.formattedValue(),
            expectedStatus = biomarker.status,
            expectedRange = biomarker.idealRange
        )

        assertTrue(matches, "Data mismatch")
    }
}



/*
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
        val viewport = TestConfig.Viewports.DESKTOP_FHD
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setIsMobile(viewport.isMobile)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()

        context.tracing().start(
            StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
        )

        loginAndNavigateToHealthData()
    }

    @AfterEach
    fun closeContext() {
        val path = "build/traceView/trace_${System.currentTimeMillis()}.zip"
        context.tracing().stop(
            StopOptions()
                .setPath(Paths.get(path))
        )

        context.close()
    }

    private fun loginAndNavigateToHealthData() {
        val testUser = TestConfig.TestUsers.EXISTING_USER
        val loginPage = LoginPage(page).navigate() as LoginPage
        val otpPage = loginPage.enterMobileAndContinue(testUser)
        healthDataPage = otpPage.enterOtpAndContinueToWebViewHealthData(testUser)
    }

    @Test
    @Order(1)
    fun `show empty state if the health data is empty`() {
        if (healthDataPage.healthData?.data?.blood?.data?.isEmpty() == true) {
            assertTrue(healthDataPage.shouldShowEmptyState(), "Empty state")
            assertTrue(healthDataPage.shouldShowTrackResult(), "show track result")

            healthDataPage.clickTrackResult()

            page.waitForURL {
                page.url().contains(TestConfig.Urls.TRACK_RESULT)
            }

            println("checkEmptyState...............${page.url()}")
            assertTrue(
                page.url().contains(TestConfig.Urls.TRACK_RESULT),
                "Should navigate to track result, but was ${page.url()}"
            )
        } else {
            assertTrue(true, "Health data found")
        }
    }

    @Test
    @Order(2)
    fun `should download biomarker report`() {

        if (healthDataPage.healthData?.data?.blood?.data?.isNotEmpty() == true) {
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
        } else {
            assertTrue(true, "Health data not found")
        }

    }

    @Test
    @Order(3)
    fun `should verify all biomarkers from CSV`() {

        if (healthDataPage.healthData?.data?.blood?.data?.isNotEmpty() == true) {

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
        } else {
            assertTrue(true, "Health data not found")
        }
    }


    @Test
    @Order(4)
    fun `should show update waist circumference description and update dialog for fatty liver index`() {

        val description = page.getByText("Add waist circumference to calculate FLI")

        if (description.isVisible){
            val fattyLiverLocator = healthDataPage.getBiomarkerRow("Fatty Liver Index (FLI)")
            if (fattyLiverLocator != null) {

                if (
                    fattyLiverLocator.textContent()
                        .contains(page.getByText("—").first().innerText()) && fattyLiverLocator.textContent()
                        .contains(page.getByText("—").nth(1).innerText())
                ) {
                    assertTrue(description.isVisible) { "add waist circumference to calculate FLI should visible" }

                    description.click()

                    assertTrue(
                        page.getByRole(
                            AriaRole.HEADING,
                            Page.GetByRoleOptions().setName("Waist Circumference")
                        ).isVisible
                    )
                    assertTrue(page.getByText("What is your waist circumference at its narrowest point (just above your hips)?").isVisible)
                    assertTrue(page.getByText("Value in inches (20-54)").isVisible)

                    val textBox = page.getByRole(AriaRole.TEXTBOX).nth(1)
                    val saveButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Save"))
                    val closePopup = page.locator(".text-muted-foreground.hover\\:text-foreground")

                    textBox.fill("19")
                    assertTrue(page.getByText("Enter value between 20-54").isVisible)
                    assertTrue(saveButton.isDisabled)

                    textBox.clear()

                    textBox.fill("55")
                    assertTrue(page.getByText("Enter value between 20-54").isVisible)
                    assertTrue(saveButton.isDisabled)

                    textBox.fill("30")
                    assertTrue(saveButton.isEnabled)

                    closePopup.click()
                    description.click()

                    assertTrue(textBox.inputValue() == "30","30")
                    assertTrue(saveButton.isEnabled)

                    saveButton.click()

                }

                description.waitUntilDetached()
                assertTrue(!description.isVisible) { "add waist circumference to calculate FLI should not be visible" }
            }
        }


    }

    private fun verifyBiomarker(biomarker: Biomarker) {
        if (biomarker.systemName.isNotBlank()) {
            try {
                healthDataPage.clickSystemTab(biomarker.systemName)
            } catch (_: Exception) {
                println("Failed to click system biomarker: ${biomarker.name}")
            }
        }

        healthDataPage.scrollToBiomarker(biomarker.name)

        val isVisible = healthDataPage.isBiomarkerVisible(biomarker.name)
        assertTrue(isVisible, "Biomarker should be visible")

        val matches = healthDataPage.verifyBiomarkerData(
            name = biomarker.name,
            expectedValue = biomarker.formattedValue(),
            expectedStatus = biomarker.status,
            expectedRange = biomarker.idealRange
        )

        assertTrue(matches, "Data mismatch")
    }

}*/
