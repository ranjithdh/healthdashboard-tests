package healthdata.test

import com.microsoft.playwright.*
import config.TestConfig
import config.TestUser
import healthdata.page.HealthDataPage
import login.page.LoginPage
import model.healthdata.Biomarker
import org.junit.jupiter.api.*
import utils.BiomarkerCsvParser
import utils.logger.logger
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthDataBiomarkerTest {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var page: Page
    private lateinit var healthDataPage: HealthDataPage
    
    // Path to the CSV file
    private val csvPath = "/Users/apple/Downloads/Deep-Holistics-Biomarkers-export.csv"

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
    }

    @AfterAll
    fun tearDown() {
        if (::browser.isInitialized) {
            browser.close()
        }
        if (::playwright.isInitialized) {
            playwright.close()
        }
    }

    @BeforeEach
    fun createContext() {
        val viewport = TestConfig.Viewports.DESKTOP_FHD
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
        
        context = browser.newContext(contextOptions)
        page = context.newPage()
        
        // Log in once and navigate to health data page
        loginAndNavigateToHealthData()
    }

    @AfterEach
    fun closeContext() {
        if (::context.isInitialized) {
            context.close()
        }
    }

    private fun loginAndNavigateToHealthData() {
        val testUser = TestUser(
            mobileNumber = "9159439327",
            otp = "678901",
            country = "India"
        )
        
        println("Logging in with user: ${testUser.mobileNumber}")
        
        val loginPage = LoginPage(page).navigate() as LoginPage
        
        // Navigate to login URL specifically if needed, but navigate() handles it
        loginPage.enterMobileAndContinue(testUser)
        
        // Enter OTP and go to Health Data
        val otpPage = login.page.OtpPage(page)
        healthDataPage = otpPage.enterOtpAndContinueToHealthData(testUser)
    }

    @Test
    fun `should verify all biomarkers from CSV`() {
        val biomarkers = BiomarkerCsvParser.parse(csvPath)

        logger.info{
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
                // Take screenshot only for the first few failures to avoid filling disk
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
        // Ensure we are on the correct tab/system
        if (biomarker.systemName.isNotBlank()) {
            try {
                healthDataPage.clickSystemTab(biomarker.systemName)
            } catch (e: Exception) {
                // Ignore tab click failure, try strict verification
            }
        }

        // Scroll to the biomarker
        healthDataPage.scrollToBiomarker(biomarker.name)
        
        val isVisible = healthDataPage.isBiomarkerVisible(biomarker.name)
        Assertions.assertTrue(isVisible, "Biomarker should be visible")
        
        // Verify data
        val matches = healthDataPage.verifyBiomarkerData(
            name = biomarker.name,
            expectedValue = biomarker.formattedValue(),
            expectedStatus = biomarker.status,
            expectedRange = biomarker.idealRange
        )
        
        Assertions.assertTrue(matches, "Data mismatch")
    }
}
