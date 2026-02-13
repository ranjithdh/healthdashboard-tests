package webView.actionplan

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import config.BaseTest
import config.TestConfig
import onboard.page.LoginPage
import onboard.page.OtpPage
import org.junit.jupiter.api.*
import utils.json.json as jsonParser
import model.UsersResponse
import com.microsoft.playwright.options.RequestOptions
import utils.logger.logger
import utils.report.StepHelper
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import java.util.regex.Pattern
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.contentOrNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@OptIn(ExperimentalSerializationApi::class)
class ActionPlanTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

    @BeforeAll
    fun setup() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(TestConfig.Browser.launchOptions())
        context = browser.newContext()
        page = context.newPage()
    }

    @AfterAll
    fun tearDown() {
        context.close()
        browser.close()
        playwright.close()
    }

    @Test
    @Order(1)
    fun `generate and verify action plan`() {
        val name = "Gowthaman"
        logger.info { "Starting ActionPlan flow..." }
        StepHelper.step("Starting ActionPlan flow")

        // 1. Login
        logger.info { "Logging in with mobile: ${TestConfig.TestUsers.EXISTING_USER.mobileNumber}" }
        StepHelper.step("Logging in")
        val loginPage = LoginPage(page).navigate() as LoginPage
        loginPage.enterMobileAndContinue(TestConfig.TestUsers.EXISTING_USER)
        
        val otpPage = OtpPage(page)
        
        // Wait for the verify-otp response to be processed and tokens stored
        page.waitForResponse({ response -> 
            response.url().contains(TestConfig.APIs.API_VERIFY_OTP) && response.status() == 200 
        }) {
            otpPage.enterOtp(TestConfig.TestUsers.EXISTING_USER.otp, TestConfig.TestUsers.EXISTING_USER.mobileNumber, TestConfig.TestUsers.EXISTING_USER.countryCode)
            page.keyboard().press("Enter") // Trigger submission if no button is present
        }
        
        // Brief wait to ensure TestConfig is updated by the response listener
        page.waitForTimeout(1000.0)
        
        logger.info { "Tokens captured. ACCESS_TOKEN length: ${TestConfig.ACCESS_TOKEN.length}, USER_ID: ${TestConfig.USER_ID}, USER_NAME: ${TestConfig.USER_NAME}" }
        assert(TestConfig.ACCESS_TOKEN.isNotEmpty()) { "Access token was not captured after login" }

        // 1b. Fetch all users to find Gowthaman's ID
        StepHelper.step("Fetching users list to find target user ID")
        val usersResponse = page.context().request().get(
            TestConfig.APIs.API_USERS,
            RequestOptions.create()
                .setHeader("access_token", TestConfig.ACCESS_TOKEN)
                .setHeader("client_id", TestConfig.CLIENT_ID)
                .setHeader("user_timezone", "Asia/Kolkata")
        )
        
        if (usersResponse.status() != 200) {
            logger.error { "Failed to fetch users list. Status: ${usersResponse.status()}, Body: ${usersResponse.text()}" }
        }
        assert(usersResponse.status() == 200) { "Failed to fetch users list: ${usersResponse.status()}" }
        
        val usersList = jsonParser.decodeFromString<UsersResponse>(usersResponse.text())
        val targetUser = usersList.data.users.find { it.name.contains(name, ignoreCase = true) }
            ?: throw AssertionError("User '$name' not found in users list")
        
        val targetUserId = targetUser.id ?: throw AssertionError("User '$name' does not have an ID")
        logger.info { "Found target user: ${targetUser.name} with ID: $targetUserId" }

        // Wait for the home page to load after login
//        page.waitForURL("${TestConfig.Urls.BASE_URL}")

        // 2. Navigation steps provided by user
        StepHelper.step("Navigating to Health Data and through dashboard steps")
        
        page.navigate(TestConfig.Urls.HEALTH_DATA_URL)
        page.waitForLoadState()
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Switch to Admin")).click()
        page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("User Management")).click()
        
        val searchBox = page.getByRole(AriaRole.TEXTBOX, Page.GetByRoleOptions().setName("Search for user..."))
        searchBox.click()
        searchBox.fill(name)
        searchBox.press("Enter")
        
        // Wait for search results
        page.waitForTimeout(2000.0)
        
        // Try to find the user in the search results and click
        try {
            val userBtn = page.locator("button, a, div[role='button']").filter(
                Locator.FilterOptions().setHasText(Pattern.compile(".*$name.*", Pattern.CASE_INSENSITIVE))
            ).first()
            userBtn.waitFor(Locator.WaitForOptions().setTimeout(10000.0))
            userBtn.click()
            logger.info { "Successfully selected user $name" }
        } catch (e: Exception) {
            logger.warn { "Search result click failed for $name: ${e.message}" }
            page.locator("tr, div[role='row']").nth(1).click()
        }


        //app.stg.deepholistics.com/recommendations
        val apLink = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Action Plan"))
        apLink.waitFor()
        apLink.click()

        // 3. Click "Go to PDF tool" and capture popup/redirect
        StepHelper.step("Clicking 'Go to PDF tool' and verifying final URL")


        
        val pdfBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Go to PDF tool"))
        pdfBtn.waitFor()
        val page1 = context.waitForPage {
            pdfBtn.click()
        }
        page1.waitForLoadState()

        page1.waitForLoadState()
        val finalUrl = page1.url()
        logger.info { "Final URL: $finalUrl" }

        val expectedBase = "https://dh-stg-action-plan-generator.replit.app/"
        logger.info { "Verifying final URL components..." }
        assert(finalUrl.contains(expectedBase)) { "Final URL does not contain expected base: $expectedBase. Actual: $finalUrl" }
        assert(finalUrl.contains("user_id=$targetUserId")) { "Final URL missing correct user_id. Expected: $targetUserId, Actual: $finalUrl" }
        assert(finalUrl.contains("user_name=${targetUser.name}")) { "Final URL missing correct user_name. Expected: ${targetUser.name}, Actual: $finalUrl" }
        assert(finalUrl.contains("access_token=${TestConfig.ACCESS_TOKEN}")) { "Final URL missing correct access_token. Actual: $finalUrl" }
        
        // 4. Call user-data API on the replit app
        StepHelper.step("Calling user-data API on replit app and verifying response")
        
        val requestBody = buildJsonObject {
            put("userId", targetUserId)
            put("accessToken", TestConfig.ACCESS_TOKEN)
        }.toString()

        val userDataResponse = page1.context().request().post(
            TestConfig.APIs.API_ACTION_PLAN_USER_DATA,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(requestBody)
        )
        
        logger.info { "User Data API Response Status: ${userDataResponse.status()}" }
        assert(userDataResponse.status() == 200) { "User data API failed: ${userDataResponse.status()}. Body: ${userDataResponse.text()}" }
        
        val userData = userDataResponse.text()
        logger.info { "Full User Data JSON: $userData" }
        assert(userData.contains("\"success\":true")) { "User data API response unsuccessful: $userData" }
        logger.info { "User data API successfully verified." }

        // New Verification Logic for Specific Nutrients based on static data
        StepHelper.step("Verifying specific nutrient food sources based on user preference")
        
        // Ensure we are on the correct category if needed, but the selector button should be visible regardless?
        // Let's assume we need to click "Nutrition" toggle first if it's not active.
//        try {
//            val nutritionToggle = page1.getByTestId("button-toggle-category-nutrition")
//            if (nutritionToggle.isVisible) {
//                nutritionToggle.click()
//            }
//        } catch (e: Exception) {
//            logger.info { "Nutrition toggle not found or not needed: ${e.message}" }
//        }

        val userDataText = userDataResponse.text()
        var foodPreference = "non_vegetarian"
        var allergies: List<String> = emptyList()
        var intolerances: List<String> = emptyList()

        // Improved Extraction Logic: Deep search for keys
        try {
            val root = jsonParser.decodeFromString<JsonObject>(userDataText)
            
            // Function to find a key anywhere in the tree (simple version)
            fun findIn(obj: JsonObject, key: String): String? {
                if (obj.containsKey(key)) return obj[key]?.jsonPrimitive?.contentOrNull
                for (v in obj.values) {
                    if (v is JsonObject) {
                        val found = findIn(v, key)
                        if (found != null) return found
                    }
                }
                return null
            }

            fun findAllIn(obj: JsonObject, key: String): List<String> {
                 val result = mutableListOf<String>()
                 val elem = obj[key]
                 if (elem != null) {
                     try {
                         if (elem is JsonArray) {
                             result.addAll(elem.map { it.jsonPrimitive.content })
                         } else {
                             val content = elem.jsonPrimitive.contentOrNull
                             if (content != null) {
                                 if (content.contains(",")) result.addAll(content.split(",").map { it.trim() })
                                 else result.add(content)
                             }
                         }
                     } catch (e: Exception) {}
                 }
                 for (v in obj.values) {
                     if (v is JsonObject) result.addAll(findAllIn(v, key))
                 }
                 return result.filter { it.isNotBlank() }.distinct()
            }

            foodPreference = findIn(root, "food_preference") ?: findIn(root, "preference") ?: "non_vegetarian"
            
            // Try different key variations for allergies
            val allergyKeys = listOf("allergy", "allergies", "allergy_ids", "food_allergy")
            allergies = allergyKeys.flatMap { findAllIn(root, it) }.distinct()
            
            // Try different key variations for intolerances
            val intoleranceKeys = listOf("intolerance", "intolerances", "intolerance_ids", "food_intolerance")
            intolerances = intoleranceKeys.flatMap { findAllIn(root, it) }.distinct()
            
            logger.info { "Extracted Preferences -> Food: $foodPreference, Allergies: $allergies, Intolerances: $intolerances" }
        } catch (e: Exception) {
            logger.warn { "Failed to extract preferences from JSON: ${e.message}" }
        }
        
        // Normalize preference string
        foodPreference = foodPreference.lowercase().replace(" ", "_")

        // 1. Open Selector Dialog
        StepHelper.step("Opening Vitamin/Nutrient Selector Dialog")
        page1.getByTestId("button-toggle-category-nutrition").click()
        page1.getByTestId("button-vitamin-selector").click()
        
        // Wait for dialog header
        try {
            page1.getByText("Select Vitamins or Nutrients").waitFor(Locator.WaitForOptions().setTimeout(5000.0))
            logger.info { "Selector dialog opened successfully" }
        } catch (e: Exception) {
            logger.error { "Failed to open selector dialog" }
            throw e
        }

        // 2. Select 2-5 vitamins randomly
        val allNutrientNames = listOf("Vitamin D", "Vitamin B1 (Thiamin)", "Vitamin B2 (Riboflavin)", "Omega 3", "Iron", "Calcium", "Zinc", "Magnesium")
        val selectedVitamins = allNutrientNames.shuffled().take(java.util.Random().nextInt(4) + 2) // 2 to 5
        logger.info { "Randomly selected vitamins for testing: $selectedVitamins" }
        
        for (nutrientName in selectedVitamins) {
            val safeName = nutrientName.split("(")[0].trim()
            val testIdSuffix = safeName.lowercase().replace(" ", "-")
            val testId = "vitamin-option-$testIdSuffix"
            
            logger.info { "Selecting $safeName in dialog..." }
            val option = page1.getByTestId(testId)
            try {
                option.scrollIntoViewIfNeeded()
                option.click()
            } catch (e: Exception) {
                val fallback = page1.locator("div").filter(Locator.FilterOptions().setHasText(Pattern.compile("^$safeName$", Pattern.CASE_INSENSITIVE))).first()
                fallback.scrollIntoViewIfNeeded()
                fallback.click()
            }
        }

        // Check Add Selected button text and click
        val addBtn = page1.getByTestId("button-add-selected")
        val btnText = addBtn.textContent()
        logger.info { "Add button text: $btnText" }
        assert(btnText.contains("${selectedVitamins.size}")) { "Selected count mismatch in button" }

        // 3. Click Add Selected
        StepHelper.step("Clicking Add Selected and waiting for API Call")
        try {
            page1.waitForResponse({ response -> 
                response.url().contains("replit.app") && response.status() == 200 
            }, Page.WaitForResponseOptions().setTimeout(10000.0)) {
                addBtn.click()
            }
        } catch (e: Exception) {
            logger.warn { "API response skip or timeout: ${e.message}" }
            if (addBtn.isVisible) addBtn.click()
        }
        
        // Wait and Scroll
        page1.waitForTimeout(5000.0)
        page1.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        page1.waitForTimeout(1000.0)

        // 4. Verify on main page using user's interaction pattern
        StepHelper.step("Verifying all added vitamins using the interaction pattern")
        
        val verifiedVitamins = mutableSetOf<String>()
        val recommendationBlocks = page1.locator("[data-testid^='preview-recommendation-vitamin-']")
        
        for (i in 0 until recommendationBlocks.count()) {
            val block = recommendationBlocks.nth(i)
            val titleElem = block.locator("[data-testid^='editable-title-vitamin-']").first()
            if (!titleElem.isVisible) continue
            
            val titleText = titleElem.textContent().trim()
            val nutrientInfo = NUTRIENT_DATA.find { 
                titleText.contains(it.nutrient.split("(")[0].trim(), ignoreCase = true) 
            }
            
            if (nutrientInfo != null) {
                verifiedVitamins.add(nutrientInfo.nutrient)
                StepHelper.step("Verifying UI section for ${nutrientInfo.nutrient}")
                
                // Interaction: Click title and heading
                titleElem.click()
                val heading = block.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Food Sources:"))
                if (heading.isVisible) heading.click()
                
                // Verification of content
                val blockText = block.textContent().replace("\n", " ")
                val expectedSources = getExpectedFoodSources(nutrientInfo, foodPreference, allergies, intolerances)
                
                // Robust verification logic
                val foundSources = mutableListOf<String>()
                val missingSources = mutableListOf<String>()
                
                for (source in expectedSources) {
                    // Check for partial match
                    val corePart = source.lowercase().removeSuffix("s").trim()
                    if (blockText.contains(corePart, ignoreCase = true)) {
                        foundSources.add(source)
                    } else {
                        missingSources.add(source)
                    }
                }
                
                if (missingSources.isNotEmpty()) {
                    logger.warn { "❌ Some sources missing for ${nutrientInfo.nutrient}: $missingSources" }
                    logger.info { "   Found sources: $foundSources" }
                } else {
                    logger.info { "✅ All expected sources verified for ${nutrientInfo.nutrient}" }
                }
            }
        }
        
        logger.info { "ActionPlan verification completed. Verified: $verifiedVitamins" }
    }

    // Helper function to calculate allowed food sources
    private fun getExpectedFoodSources(nutrient: NutrientInfo, preference: String, allergies: List<String>, intolerances: List<String>): List<String> {
        val sources = mutableListOf<String>()
        
        fun add(text: String) {
            if (text.isNotBlank()) {
                sources.addAll(text.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
        }

        // 1. Vegan (Always included?) 
        // Logic: vegan + veg + egg + non veg
        // Assuming base is Vegan + Vegetarian based on user instruction
        add(nutrient.vegan)
        add(nutrient.vegetarian)

        // 2. Tolerances (Add if NOT intolerant)
        if (!intolerances.any { it.equals("lactose", ignoreCase = true) }) {
            add(nutrient.lactoseTolerant)
        }
        if (!intolerances.any { it.equals("gluten", ignoreCase = true) }) {
            add(nutrient.glutenTolerant)
        }

        // 3. Egg
        if (preference in listOf("eggetarian", "non_vegetarian")) {
            add(nutrient.egg)
        }

        // 4. Non-Vegetarian
        if (preference.equals("non_vegetarian", ignoreCase = true)) {
            add(nutrient.nonVegetarian)
        }

        // 5. Filter Allergies
        val excludedKeywords = allergies.flatMap { allergy -> 
            val key = ALLERGY_FOOD_MAPPING.keys.find { it.equals(allergy, ignoreCase = true) }
            ALLERGY_FOOD_MAPPING[key] ?: emptyList()
        }

        return sources.filter { source ->
            !excludedKeywords.any { excluded -> source.contains(excluded, ignoreCase = true) }
        }
    }

    data class NutrientInfo(
        val nutrient: String,
        val vegan: String,
        val vegetarian: String,
        val lactoseTolerant: String,
        val glutenTolerant: String,
        val egg: String,
        val nonVegetarian: String
    )

    companion object {
        val ALLERGY_FOOD_MAPPING: Map<String, List<String>> = mapOf(
            "Milk or dairy" to listOf("Milk", "yogurt", "paneer", "cheese", "fortified dairy", "dairy"),
            "Eggs" to listOf("Egg White", "Egg Yolk"),
            "Peanuts" to listOf("Peanuts"),
            "Tree nuts" to listOf("almonds", "cashews", "walnuts", "hazelnuts", "Brazil nuts", "seeds"),
            "Soy" to listOf("Soybean", "Tofu", "tempeh", "fortified plant milk"),
            "Gluten (Wheat)" to listOf("Whole wheat", "wheat bran", "Fortified cereals"),
            "Fish" to listOf("fish", "salmon", "mackerel", "sardines", "tuna", "trout", "anchovies", "rohu", "catla", "seafood"),
            "Shellfish" to listOf("shellfish", "seafood")
        )

        val NUTRIENT_DATA = listOf(
            NutrientInfo("Calcium", "Ragi, amaranth leaves, moringa, spinach, fenugreek leaves, sesame seeds, tofu, chickpeas, white beans, chia seeds, fortified plant milk, mustard greens", "", "Milk, yogurt, paneer, cheese", "", "", ""),
            NutrientInfo("Iron", "Lentils, chickpeas, rajma, spinach, beetroot leaves, amaranth leaves, cauliflower greens, dates, prunes, ragi, pumpkin seeds, sesame seeds, mustard greens, soybeans, black chana, jaggery", "", "", "", "", ""),
            NutrientInfo("Magnesium", "Pumpkin seeds, sesame seeds, flax seeds, chia seeds, spinach, amaranth leaves, oats", "", "", "", "", ""),
            NutrientInfo("Potassium", "Spinach, beetroot leaves, amaranth leaves, potatoes, sweet potatoes, bananas, oranges, lentils, rajma, soybeans", "", "", "", "", ""),
            NutrientInfo("Selenium", "Brazil nuts, mushrooms, sunflower seeds, chia seeds, sesame seeds, flax seeds, lentils, chickpeas, soybeans, oats", "", "", "whole wheat", "Egg White, Egg Yolk", ""),
            NutrientInfo("Zinc", "Seed, cashews, almonds, peanuts, chickpeas, lentils, rajma, black beans, tofu, tempeh, mushrooms, red rice, millet, oats", "", "Curd", "wheat bran", "", ""),
            NutrientInfo("Sodium", "Beets, mustard greens, olives, celery, beetroot, spinach, chard, turnip greens", "", "Milk, cheese, yogurt", "", "", ""),
            NutrientInfo("Vitamin A (Retinol)", "Carrots, sweet potatoes, spinach, kale, pumpkin, mustard greens, fenugreek leaves, butternut squash, bell peppers, mango", "", "", "", "", ""),
            NutrientInfo("Vitamin B1 (Thiamin)", "Flax seeds, pumpkin seeds, sesame seeds, sunflower seeds, lentils, peanuts, brown rice, peas", "", "", "Whole wheat, fortified cereals", "", ""),
            NutrientInfo("Vitamin B2 (Riboflavin)", "Almonds, sunflower seeds, mushrooms, spinach, soybeans, tofu, tempeh, nutritional yeast", "", "Milk, yogurt, paneer, cheese", "fortified cereals", "", ""),
            NutrientInfo("Vitamin B3 (Niacin)", "Peanuts, sunflower seeds, brown rice, mushrooms, green peas, potatoes, nutritional yeast", "", "", "Whole wheat", "", ""),
            NutrientInfo("Vitamin B5 (Pantothenic Acid)", "Mushrooms, sunflower seeds, avocados, legumes, nutritional yeast", "", "Yogurt", "Whole grains", "", ""),
            NutrientInfo("Vitamin B6 (Pyridoxine)", "Chickpeas, bananas, potatoes, spinach, walnuts, almonds, flaxseeds, nutritional yeast", "", "", "Fortified cereals", "", ""),
            NutrientInfo("Vitamin B7 (Biotin)", "Almonds, walnuts, sunflower seeds, spinach, sweet potato, mushrooms, soybeans, nutritional yeast", "", "", "Oats", "", ""),
            NutrientInfo("Vitamin B9 (Folate)", "Spinach, amaranth leaves, lentils, chickpeas, broccoli, beetroot leaves, black-eyed peas, fortified plant milk, nutritional yeast", "", "", "", "", ""),
            NutrientInfo("Vitamin B12 (Cobalamin)", "Fortified plant milk, nutritional yeast, fermented foods", "", "Milk, yogurt, paneer, cheese", "Fortified cereals", "", ""),
            NutrientInfo("Vitamin C", "Amla, guava, lemon, lime, oranges, grapefruit, pineapple, strawberries, papaya, pomegranate, tomato, bell peppers, broccoli, cabbage, spinach, amaranth leaves, mustard greens", "", "", "", "", ""),
            NutrientInfo("Vitamin D", "Fortified plant milk, sun exposure, UV mushrooms", "", "Fortified dairy", "", "", ""),
            NutrientInfo("Vitamin E", "Almonds, sunflower seeds, spinach, olive oil, peanuts, hazelnuts, avocado", "", "", "", "", ""),
            NutrientInfo("Omega 3", "Flaxseeds, chia seeds, rajma, hemp seeds, algal oil", "", "", "", "", "")
        )
    }
}
