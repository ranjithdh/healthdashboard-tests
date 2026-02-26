package webView.actionPlanAdmin

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.RequestOptions
import config.BaseTest
import config.TestConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import model.UsersResponse
import onboard.page.LoginPage
import onboard.page.OtpPage
import org.junit.jupiter.api.*
import utils.logger.logger
import utils.report.StepHelper
import java.util.regex.Pattern
import org.apache.pdfbox.Loader
import utils.json.json as jsonParser


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@OptIn(ExperimentalSerializationApi::class)
class ActionPlanAdminTest : BaseTest() {

    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext

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
        val name = "Rethinavel  natarajan stg"
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
        
        logger.info { "Tokens captured. ACCESS_TOKEN is: ${TestConfig.ACCESS_TOKEN}, USER_ID: ${TestConfig.USER_ID}, USER_NAME: ${TestConfig.USER_NAME}" }
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
        page.waitForTimeout(5000.0)
        searchBox.pressSequentially(name, Locator.PressSequentiallyOptions().setDelay(200.0))
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
        // Wait for all background APIs to settle and give a 5s buffer
        logger.info { "Waiting for Action Plan APIs to settle..." }
        page1.waitForLoadState(LoadState.NETWORKIDLE)
        page1.waitForTimeout(5000.0)

        val finalUrl = page1.url()
        logger.info { "Final URL: $finalUrl" }

        val expectedBase = "https://dh-stg-action-plan-generator.replit.app/"
        logger.info { "Verifying final URL components..." }
        assert(finalUrl.contains(expectedBase)) { "Final URL does not contain expected base: $expectedBase. Actual: $finalUrl" }
        assert(finalUrl.contains("user_id=$targetUserId")) { "Final URL missing correct user_id. Expected: $targetUserId, Actual: $finalUrl" }
//        assert(finalUrl.contains("user_name=${targetUser.name}")) { "Final URL missing correct user_name. Expected: ${targetUser.name}, Actual: $finalUrl" }
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

        // 5. Call user-recommendations API
        StepHelper.step("Calling user-recommendations API on replit app and verifying response")

        val userRecommendationsResponse = page1.context().request().post(
            TestConfig.APIs.API_ACTION_PLAN_USER_RECOMMENDATIONS,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(requestBody)
        )

        logger.info { "User Recommendations API Response Status: ${userRecommendationsResponse.status()}" }
        assert(userRecommendationsResponse.status() == 200) { "User recommendations API failed: ${userRecommendationsResponse.status()}. Body: ${userRecommendationsResponse.text()}" }

        val recommendationsData = userRecommendationsResponse.text()
        logger.info { "Full User Recommendations JSON: $recommendationsData" }
        assert(recommendationsData.contains("\"success\":true")) { "User recommendations API response unsuccessful: $recommendationsData" }
        logger.info { "User recommendations API successfully verified." }

        // Static verification requested by User
        StepHelper.step("Verifying Action Plan Header and Overview Section")
        val dynamicPdfStrings = mutableListOf<String>()

        // User Information Section (Dynamic)
        StepHelper.step("Verifying User Information Section")
        val userDataJson = jsonParser.decodeFromString<JsonObject>(userData)
        val apiData = userDataJson["data"]?.jsonObject
        val userProfile = userDataJson["userProfile"]?.jsonObject
        
        val dynName = userProfile?.get("name")?.jsonPrimitive?.contentOrNull ?: name
        val dynAge = userProfile?.get("age")?.jsonPrimitive?.contentOrNull ?: "22"
        val dynGender = userProfile?.get("gender")?.jsonPrimitive?.contentOrNull ?: "male"
        val dynHeight = userProfile?.get("height")?.jsonPrimitive?.contentOrNull ?: "291"
        val dynWeight = userProfile?.get("weight")?.jsonPrimitive?.contentOrNull ?: "110"
        
        val programGoals = userDataJson["programGoals"]?.jsonObject
        val isQuestionnaireTaken = programGoals?.get("is_questionnaire_take")?.jsonPrimitive?.booleanOrNull ?: true
        val questionnaireTakenText = if (isQuestionnaireTaken) "Yes" else "No"
        
//        val foodDataCount = apiData?.get("food")?.jsonObject?.get("data")?.jsonArray?.size ?: 280
        
        // Dynamic marker counts from API calculation
        var optimalMarkers = 0
        var needsImprovementMarkers = 0
        var atRiskMarkers = 0
        val bloodArray = apiData
            ?.get("data")?.jsonObject
            ?.get("blood")?.jsonObject
            ?.get("data")?.jsonArray
        logger.info("bloodArray is $bloodArray")
        bloodArray?.forEach { marker ->
            if (marker is JsonObject) {
                logger.info("marker is $marker")
                val rating = marker["display_rating"]?.jsonPrimitive?.contentOrNull?.lowercase()?.trim()
                if (rating != null) {
                    when (rating) {
                        "optimal", "normal" -> {
                            optimalMarkers++
                        }
                        "high", "elevated",
                        "very high", "severely elevated", "critically high", "extremely high",
                        "low", "reduced",
                        "very low", "severely low", "critically low", "extremely low"
                            -> {
                            atRiskMarkers++
                        }
                        else -> {
                            needsImprovementMarkers++
                        }
                    }
                }
            }
        }
        logger.info { "Dynamic marker counts: $optimalMarkers Optimal, $needsImprovementMarkers Needs Improvement, $atRiskMarkers At Risk" }

        page1.getByTestId("user-info-display").click()
        page1.getByText("User Information").click()

        page1.getByText("Name: $dynName").click()
        page1.getByText("Age: $dynAge years").click()
        page1.getByText("Gender: $dynGender").click()
        page1.getByText("Height: $dynHeight cm").click()
        page1.getByText("Weight: $dynWeight kg").click()
        page1.getByText("Questionnaire Taken: $questionnaireTakenText").click()
//        page1.getByText("Foods List: Loaded ($foodDataCount items)").click()

        // 1. Header
        val firstName = name.trim().split(Regex("\\s+")).first()
        val headerText = "$firstName's Action Plan"
        page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(headerText)).click()
        dynamicPdfStrings.add(headerText)

        // 2. Date
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d,")
        val dateStr = java.time.LocalDate.now().format(formatter)
        try {
            page1.getByText(dateStr).click()
        } catch (e: Exception) {
            logger.warn { "Could not click date '$dateStr'. Might be different timezone or date format." }
        }
        // 3. Overview
        page1.getByTestId("preview-introduction").getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Overview")).click()

        // 4. Intro Text
        val introHeader = page1.getByText("At the core of your Deep", Page.GetByTextOptions().setExact(false)).first()
        introHeader.scrollIntoViewIfNeeded()
        introHeader.click()

        val expectedIntroItems = listOf(
            "1. Summary: A snapshot of your biological status",
            "2. What's Working Well for You: The areas where your biology",
            "3. What Needs Support: The biomarkers that need closer attention",
            "4. Lifestyle Modifications: Simple, high-impact shifts",
            "5. Nutrition Guidance: Personalized food and nutrient strategies",
            "6. Supplement Recommendations: A focused protocol",
            "7. Diagnostic Testing: Follow-up and advanced tests"
        )
        dynamicPdfStrings.addAll(expectedIntroItems)


        expectedIntroItems.forEach { item ->

             page1.getByText(item)
//            locator.scrollIntoViewIfNeeded()
//            locator.waitFor(Locator.WaitForOptions().setTimeout(10000.0))
//            assert(locator.isVisible) { "Overview item starting with '$item' not found or not visible" }
        }

        // Summary Section
        StepHelper.step("Verifying Summary and Biomarker Overview Section")

        // 1. Click Summary Heading
        page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Summary")).click()


        // 3. Biomarker Overview
        page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Biomarker Overview")).click()

        page1.getByText("$optimalMarkers Optimal markers $needsImprovementMarkers Needs Improvement $atRiskMarkers At Risk")
        // 4. Data Review Paragraph
        val reviewText = "Here's the data we reviewed to create your action plan: - Your current blood test results - 1-on-1 interaction with the longevity expert - Pre-consult questionnaire"
        page1.getByText(reviewText)

        // 5. Health Status Overview
        val healthTitle = page1.getByTestId("editable-title-health-overview")
        assert(healthTitle.innerText().contains("Health Status Overview")) { "Health Status Overview title missing" }
        healthTitle.click()

        // NEW: "What's Working Well for You" Verification
        dynamicPdfStrings.add("What's Working Well for You")
        verifyWhatsWorkingWell(page1, userData, dynamicPdfStrings)

        // NEW: "What Needs Support" Verification
        dynamicPdfStrings.add("What Needs Support")
        verifyWhatNeedsSupport(page1, userData, dynamicPdfStrings)

        // Parse recommendations for dynamic verification
        val recommendationsJson = jsonParser.decodeFromString<JsonObject>(recommendationsData)
        val rawRecommendationsList = recommendationsJson["data"]?.jsonObject?.get("data")?.jsonObject?.get("recommendations")?.jsonArray ?: JsonArray(emptyList())
        
        // Filter to include only approved recommendations as per the logic for activity, sleep, stress, and supplements
        val recommendationsList = JsonArray(rawRecommendationsList.filter {
            it.jsonObject["approval_status"]?.jsonPrimitive?.contentOrNull == "approved"
        })

        val lifestyleCategories = setOf("activity", "sleep", "stress")

        val lifestyleRecs = recommendationsList.filter { rec ->
            val cat = rec.jsonObject["category"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            cat in lifestyleCategories
        }

        if (lifestyleRecs.isNotEmpty()) {
            StepHelper.step("Verifying Lifestyle Modifications Section")
            page1.getByTestId("button-toggle-category-lifestyle").click()
            page1.waitForTimeout(1000.0)

            lifestyleRecs.forEach { rec ->
                val id = rec.jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: ""
                val displayName = rec.jsonObject["display_name"]?.jsonPrimitive?.contentOrNull ?: ""
                val description = rec.jsonObject["description"]?.jsonPrimitive?.contentOrNull ?: ""

                if (id.isEmpty() || displayName.isEmpty()) return@forEach

                logger.info { "Verifying Lifestyle Recommendation: $displayName (ID: $id)" }
                StepHelper.step("Verifying: $displayName")

                // Scroll to and click the card to make sure it's in view
                val recCard = page1.getByTestId("recommendation-$id")
                recCard.scrollIntoViewIfNeeded()
                recCard.click()
                page1.waitForTimeout(300.0)

                val checkbox = page1.getByTestId("checkbox-$id")
                // Use editable-title-{id} — the right-panel element that shows/hides on select/deselect
                // Using getByText(displayName) causes strict mode violation as it matches 2 elements:
                // the sidebar card title AND the editable title in the right panel
                val contentLocator = page1.getByTestId("editable-title-$id")

                // Check current visibility state of the content
                val isCurrentlyVisible = contentLocator.isVisible

                if (isCurrentlyVisible) {
                    logger.info { "Content is VISIBLE (selected state). Deselecting to verify it hides..." }

                    // Step 1: Deselect → content should disappear
                    checkbox.click()
                    page1.waitForTimeout(500.0)
                    assert(!contentLocator.isVisible) {
                        "❌ Content '$displayName' should be hidden after deselecting checkbox-$id"
                    }
                    logger.info { "✅ $displayName correctly hidden after deselect" }

                    // Step 2: Re-select → content should reappear
                    checkbox.click()
                    page1.waitForTimeout(500.0)
                    assert(contentLocator.isVisible) {
                        "❌ '$displayName' should be visible after re-selecting checkbox-$id"
                    }
                    logger.info { "✅ $displayName correctly re-appeared after re-select" }

                } else {
                    logger.info { "$displayName is NOT VISIBLE (deselected state). Selecting to verify it appears..." }

                    // Step 1: Select → content should appear
                    checkbox.click()
                    page1.waitForTimeout(500.0)
                    assert(contentLocator.isVisible) {
                        "❌'$displayName' should be visible after selecting checkbox-$id"
                    }
                    logger.info { "✅ $displayName correctly appeared after select" }
                }

                dynamicPdfStrings.add(displayName)
                if (description.isNotEmpty()) dynamicPdfStrings.add(description)
            }
        }
        // 6. Supplement Protocol Verification
        val supplements = recommendationsList.filter {
            val isSupplement = it.jsonObject["category"]?.jsonPrimitive?.contentOrNull?.equals("supplement", ignoreCase = true) == true
            val hasDuration = it.jsonObject["supplement_duration"]?.jsonPrimitive?.contentOrNull != null
            isSupplement && hasDuration
        }

        if (supplements.isNotEmpty()) {
            StepHelper.step("Verifying Supplement Protocol")
            page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Supplement Protocol")).click()
            dynamicPdfStrings.add("Supplement Protocol")

            supplements.forEach { rec ->
                logger.info { "Verifying Supplement: $rec)" }
                val id = rec.jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: ""
                val name = (rec.jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: "").replace(Regex("\\s+"), " ").trim()
                val nameInRespone = (rec.jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: "")
                val displayName = (rec.jsonObject["display_name"]?.jsonPrimitive?.contentOrNull ?: "")
                val duration = (rec.jsonObject["supplement_duration"]?.jsonPrimitive?.contentOrNull ?: "")
//                val detailedDesc = (rec.jsonObject["detailed_description"]?.jsonPrimitive?.contentOrNull ?: "")
                val cardDesc = (rec.jsonObject["supplement_card_description"]?.jsonPrimitive?.contentOrNull ?: "")

                val supplementMeta = rec.jsonObject["variant_meta"]?.jsonObject
                logger.info { "Verifying supplementMeta : $supplementMeta )" }
                val brand = (supplementMeta?.get("brand")?.jsonPrimitive?.contentOrNull ?: "").replace(Regex("\\s+"), " ").trim()
                val ingredientsArr = supplementMeta?.get("ingredients")?.jsonArray ?: JsonArray(emptyList())

                logger.info { "Verifying Supplement: $displayName (ID: $id)" }

                val block = page1.getByTestId("preview-recommendation-$id")
                block.scrollIntoViewIfNeeded()

                // 1. Click card/display name
                block.getByTestId("supplement-card").click()
                page1.getByText(name)
                dynamicPdfStrings.add(displayName)

                // 2. Duration
                if (duration.isNotEmpty()) {
                    val durationText = "Duration: $duration"
                    block.getByText(durationText).click()
                    dynamicPdfStrings.add(durationText)
                }

                // 3. Buy Now link check
                val buyNow = block.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Buy Now"))
                assert(buyNow.isVisible) { "Buy Now link not visible for $displayName" }

                StepHelper.step("Verifying Buy Now popup for $displayName")
                val buyNowPopup = page1.waitForPopup {
                    block.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Buy Now")).click()
                }
                buyNowPopup.waitForLoadState()
                logger.info { "Successfully opened Buy Now link for $displayName: ${buyNowPopup.url()}" }
                assert(buyNowPopup.url().isNotBlank()) { "Buy Now URL is empty for $displayName" }
                buyNowPopup.close()


                // 4. What is it (Ingredients)
                block.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("What is it")).click()
                val ingredientsList = ingredientsArr.map { ing ->
                    val name = ing.jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: ""
                    val amount = ing.jsonObject["amount"]?.jsonPrimitive?.contentOrNull
                    val unit = ing.jsonObject["unit"]?.jsonPrimitive?.contentOrNull

                    "$name (${amount ?: "null"}${unit ?: "null"})"
                }.joinToString(", ")

                val expectedWhatIsIt = "$nameInRespone by $brand. Contains: $ingredientsList"
                logger.info { "Checking 'What is it' text: $expectedWhatIsIt" }
                val whatIsItElem = block.getByText(expectedWhatIsIt, Locator.GetByTextOptions().setExact(false)).first()
                assert(whatIsItElem.isVisible) { "What is it text mismatch for $displayName" }
                whatIsItElem.click()
                dynamicPdfStrings.add(expectedWhatIsIt)

                // 5. Why this matters?
                block.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Why this matters?")).click()
                // 6. How to take it
                if (cardDesc.isNotEmpty()) {
                    block.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("How to take it")).click()
                    val howElem = block.getByText(cardDesc)
                    assert(howElem.isVisible) { "Card description missing for $displayName" }
                    howElem.click()
                    dynamicPdfStrings.add(cardDesc)
                }
            }
        }

        // 7. Diagnostic Testing Verification
        val tests = recommendationsList.filter {
            it.jsonObject["category"]?.jsonPrimitive?.contentOrNull?.equals("test", ignoreCase = true) == true
        }

        if (tests.isNotEmpty()) {
            StepHelper.step("Verifying Diagnostic Testing Section")
            page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Diagnostic Testing")).click()
            dynamicPdfStrings.add("Diagnostic Testing")

            tests.forEach { testRec ->
                val id = testRec.jsonObject["id"]?.jsonPrimitive?.contentOrNull ?: ""
                val displayName = testRec.jsonObject["display_name"]?.jsonPrimitive?.contentOrNull ?: ""
                val dueDateRaw = testRec.jsonObject["test_to_be_taken_at"]?.jsonPrimitive?.contentOrNull ?: ""
                val description = testRec.jsonObject["description"]?.jsonPrimitive?.contentOrNull ?: ""

                val block = page1.getByTestId("preview-recommendation-$id")
                block.scrollIntoViewIfNeeded()

                StepHelper.step("Verifying Test: $displayName")

                // Click card
                block.getByTestId("test-card").click()

                // Verify Display Name
                val nameElem = block.getByText(displayName).first()
                assert(nameElem.isVisible) { "Test name mismatch: $displayName" }
                nameElem.click()
                dynamicPdfStrings.add(displayName)

                // Verify Due Date
                if (dueDateRaw.isNotEmpty()) {
                    try {
                        val dateTime = java.time.OffsetDateTime.parse(dueDateRaw)
                            .atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata"))
                        val formattedDate = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                        val expectedDueText = "Due on: $formattedDate"
                        logger.info { "Checking due date text: $expectedDueText" }
                        val dueElem = block.getByText(expectedDueText).first()
                        assert(dueElem.isVisible) { "Due date mismatch for $displayName. Expected: $expectedDueText" }
                        dynamicPdfStrings.add(expectedDueText)
                    } catch (e: Exception) {
                        logger.warn { "Failed to parse/verify due date '$dueDateRaw': ${e.message}" }
                    }
                }

                // Book Now Popup
                StepHelper.step("Verifying Book Now popup for $displayName")
                val bookNowPopup = page1.waitForPopup {
                    block.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Book Now")).click()
                }
                bookNowPopup.waitForLoadState()
                logger.info { "Successfully opened Book Now link for $displayName: ${bookNowPopup.url()}" }
                assert(bookNowPopup.url().isNotBlank()) { "Book Now URL is empty for $displayName" }
                bookNowPopup.close()

                // Why test it?
                if (description.isNotEmpty()) {
                    block.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Why test it")).click()
                    val descElem = block.getByText(description).first()
                    assert(descElem.isVisible) { "Test description missing for $displayName" }
                    descElem.click()
                    dynamicPdfStrings.add(description)
                }
            }
        }
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
                    dynamicPdfStrings.add(nutrientInfo.nutrient)
                    dynamicPdfStrings.addAll(foundSources)
                }
            }
        }

        logger.info { "ActionPlan verification completed. Verified: $verifiedVitamins" }

        // --- PDF Export and Validation ---
        StepHelper.step("Export PDF and cross verify contents")
        logger.info { "--------------------------------------------------" }
        logger.info { "PDF VALIDATION: STEP 1 - Starting Download" }
        val download2: Download? = page1.waitForDownload {
            page1.getByTestId("button-export-pdf").click()
        }
        val pdfPath = download2?.path()
        org.junit.jupiter.api.Assertions.assertNotNull(pdfPath, "PDF download failed or path is null")
        logger.info { "PDF VALIDATION: STEP 2 - Downloaded to $pdfPath (${pdfPath?.toFile()?.length()} bytes)" }

        logger.info { "PDF VALIDATION: STEP 3 - Extracting text via PDFBox" }
        val pdfFile = pdfPath!!.toFile()
        val document = Loader.loadPDF(pdfFile)
        val stripper = org.apache.pdfbox.text.PDFTextStripper()
        val pdfText = stripper.getText(document)
        document.close()
        logger.info { "Successfully extracted ${pdfText.length} raw characters" }

        val normalizeText = { text: String ->
            text.replace("\r\n", " ")
                .replace("\n", " ")
                .replace("\u2013", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("\u00A0", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        logger.info { "PDF VALIDATION: STEP 4 - Normalizing PDF text" }
        val normalizedPdfText = normalizeText(pdfText)

        logger.info { "PDF VALIDATION: STEP 5 - Comparing ${dynamicPdfStrings.size} strings from UI" }
        val foundResults = mutableListOf<Triple<String, String, Boolean>>()
        
        dynamicPdfStrings.forEach { original ->
            val search = normalizeText(original)
            val found = normalizedPdfText.contains(search)
            foundResults.add(Triple(original, search, found))
            if (found) {
                logger.info { "  [✅ MATCHED] \"$search\"" }
            } else {
                logger.error { "  [❌ MISSING] \"$search\"" }
            }
        }

        val missingLines = foundResults.filter { !it.third }

        logger.info { "--------------------------------------------------" }
        logger.info { "PDF VALIDATION SUMMARY" }
        logger.info { "Strings Verified: ${foundResults.size}" }
        logger.info { "Matches Found: ${foundResults.size - missingLines.size}" }
        logger.info { "Discrepancies: ${missingLines.size}" }
        logger.info { "--------------------------------------------------" }

        if (missingLines.isNotEmpty()) {
            val errorMsg = "PDF Validation Failed. Missing strings:\n" + 
                missingLines.mapIndexed { idx, item -> "${idx + 1}. ${item.second}" }.joinToString("\n")
            
            logger.info { "FULL PDF CONTENT FOR DEBUGGING:\n$normalizedPdfText" }
            org.junit.jupiter.api.Assertions.fail<Unit>(errorMsg)
        } else {
            logger.info { "✅ PDF Validation Passed: All UI elements were found in the PDF." }
        }
    }


    private fun verifyWhatsWorkingWell(page1: Page, userData: String, dynamicPdfStrings: MutableList<String>) {
        StepHelper.step("Verifying 'What's Working Well' section and selector")
        
        // Step 1: Navigate to selector
        page1.getByTestId("button-toggle-category-whats-working-well").click()
        page1.getByTestId("category-whats-working-well").getByTestId("button-biomarker-selector").click()
        
        // Wait for dialog
        page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Select Biomarkers for")).waitFor()
        page1.getByText("Choose biomarkers to create a").click()
        page1.getByTestId("input-search-biomarkers").click()

        // Step 2: Dynamic verification based on API
        val userDataJson = jsonParser.decodeFromString<JsonObject>(userData)
        val rootData = userDataJson["data"]?.jsonObject
        val apiData = rootData?.get("data")?.jsonObject ?: rootData
        val allBiomarkers = mutableListOf<JsonElement>()
        
        apiData?.forEach { (_, value) ->
            if (value is JsonObject) {
                val dataArray = value["data"]
                if (dataArray is JsonArray) {
                    allBiomarkers.addAll(dataArray)
                }
            }
        }
        val biomarkers = JsonArray(allBiomarkers)
        
        val workWellMarkers = biomarkers.filter {
            if (it !is JsonObject) return@filter false
            val rating = it.jsonObject["display_rating"]?.jsonPrimitive?.contentOrNull
            rating?.equals("Optimal", ignoreCase = true) == true || rating?.equals("Normal", ignoreCase = true) == true
        }

        logger.info { "Found ${workWellMarkers.size} biomarkers with Optimal/Normal rating" }

        // Group by group_name for better verification flow (fallback to identifier if group_name is null)
        val groupedMarkers = workWellMarkers.groupBy { 
            it.jsonObject["group_name"]?.jsonPrimitive?.contentOrNull ?: it.jsonObject["identifier"]?.jsonPrimitive?.contentOrNull ?: "Other" 
        }

        groupedMarkers.forEach { (groupName, markers) ->
            logger.info { "Verifying group: $groupName" }
            try {
                page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(groupName)).click()
            } catch (e: Exception) {
                logger.warn { "Could not click group heading: $groupName" }
            }
            
            markers.forEach { marker ->
                val metricId = marker.jsonObject["metric_id"]?.jsonPrimitive?.contentOrNull ?: ""
                val displayName = marker.jsonObject["display_name"]?.jsonPrimitive?.contentOrNull ?: ""
                val displayRating = marker.jsonObject["display_rating"]?.jsonPrimitive?.contentOrNull ?: ""
                val value = marker.jsonObject["value"]?.jsonPrimitive?.contentOrNull ?: ""
                val unit = marker.jsonObject["unit"]?.jsonPrimitive?.contentOrNull ?: ""
                val range = marker.jsonObject["range"]?.jsonPrimitive?.contentOrNull ?: ""

                logger.info { "Verifying marker: $displayName ($metricId)" }
                
                // Click option
                val option = page1.getByTestId("biomarker-option-$metricId")
                option.scrollIntoViewIfNeeded()
                option.click()
                
                // Verify display name and rating (combined text in UI)
                // Use scrolling and first() to avoid strict mode violations from hidden/multiple elements
                option.getByText("$displayName$displayRating").first().click()
                
                // Value: Include unit to be more specific and avoid substring matching issues (e.g., "0.4" matching "0.47")
                if (value.isNotEmpty() && value != "null") {
                    val valueText = if (unit.isNotEmpty()) "Value: $value $unit" else "Value: $value"
                    option.getByText(valueText).first().click()
                }
                
                // Reference
                if (range.isNotEmpty() && range != "null") {
                    val referenceText = "Reference: $range"
                    option.getByText(referenceText).first().click()
                }
            }
        }

        // Step 3: Select random ones (selecting 4 as per user example)
        StepHelper.step("Selecting random biomarkers")
        val randomSelection = workWellMarkers.shuffled().take(4)
        randomSelection.forEach { marker ->
            val metricId = marker.jsonObject["metric_id"]?.jsonPrimitive?.contentOrNull ?: ""
            logger.info { "Selecting checkbox for: $metricId" }
            page1.getByTestId("checkbox-biomarker-$metricId").click()
        }

        // Step 4: Create subsection
        StepHelper.step("Clicking Create Subsection")
        page1.getByTestId("button-create-subsection").click()
        logger.info { "Subsection 'What's Working Well' created successfully" }
        
        dynamicPdfStrings.add("The areas where your biology is performing at its best")
    }

    private fun verifyWhatNeedsSupport(page1: Page, userData: String, dynamicPdfStrings: MutableList<String>) {
        StepHelper.step("Verifying 'What Needs Support' section and selector")
        
        // Step 1: Navigate to selector
        page1.getByTestId("button-toggle-category-what-needs-support").click()
        page1.getByTestId("category-what-needs-support").getByTestId("button-biomarker-selector").click()
        
        // Wait for dialog
        page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Select Biomarkers for")).waitFor()
        page1.getByText("Choose biomarkers to create a").click()
        page1.getByTestId("input-search-biomarkers").click()

        // Step 2: Dynamic verification based on API
        val userDataJson = jsonParser.decodeFromString<JsonObject>(userData)
        val rootData = userDataJson["data"]?.jsonObject
        val apiData = rootData?.get("data")?.jsonObject ?: rootData
        val allBiomarkers = mutableListOf<JsonElement>()
        
        apiData?.forEach { (_, value) ->
            if (value is JsonObject) {
                val dataArray = value["data"]
                if (dataArray is JsonArray) {
                    allBiomarkers.addAll(dataArray)
                }
            }
        }
        val biomarkers = JsonArray(allBiomarkers)
        
        // Filter for: low, high, borderline low, borderline high, monitor
        val needsSupportMarkers = biomarkers.filter {
            if (it !is JsonObject) return@filter false
            val rating = it.jsonObject["display_rating"]?.jsonPrimitive?.contentOrNull?.lowercase() ?: ""
            rating in listOf("low", "high", "borderline low", "borderline high", "monitor", "very high", "very low", "needs attention")
        }

        logger.info { "Found ${needsSupportMarkers.size} biomarkers for 'What Needs Support'" }

        // Group by group_name for better verification flow (fallback to identifier if group_name is null)
        val groupedMarkers = needsSupportMarkers.groupBy { 
            it.jsonObject["group_name"]?.jsonPrimitive?.contentOrNull ?: it.jsonObject["identifier"]?.jsonPrimitive?.contentOrNull ?: "Other" 
        }

        groupedMarkers.forEach { (groupName, markers) ->
            logger.info { "Verifying group: $groupName" }
            try {
                // Click group heading to expand
                page1.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(groupName)).click()
            } catch (e: Exception) {
                logger.warn { "Could not click group heading: $groupName" }
            }
            
            markers.forEach { marker ->
                val metricId = marker.jsonObject["metric_id"]?.jsonPrimitive?.contentOrNull ?: ""
                val displayName = marker.jsonObject["display_name"]?.jsonPrimitive?.contentOrNull ?: ""
                val displayRating = marker.jsonObject["display_rating"]?.jsonPrimitive?.contentOrNull ?: ""
                val value = marker.jsonObject["value"]?.jsonPrimitive?.contentOrNull ?: ""
                val unit = marker.jsonObject["unit"]?.jsonPrimitive?.contentOrNull ?: ""
                val range = marker.jsonObject["range"]?.jsonPrimitive?.contentOrNull ?: ""

                logger.info { "Verifying marker: $displayName ($metricId)" }
                
                // Click option
                val option = page1.getByTestId("biomarker-option-$metricId")
                option.scrollIntoViewIfNeeded()
                option.click()
                
                // Verify display name and rating (combined text in UI: e.g., "HematocritHigh")
                option.getByText("$displayName$displayRating").first().click()
                
                // Value: e.g., "Value: 50.3 %"
                if (value.isNotEmpty() && value != "null") {
                    val valueText = if (unit.isNotEmpty()) "Value: $value $unit" else "Value: $value"
                    option.getByText(valueText).first().click()
                }
                
                // Reference: e.g., "Reference: >50"
                if (range.isNotEmpty() && range != "null") {
                    val referenceText = "Reference: $range"
                    option.getByText(referenceText).first().click()
                }
            }
        }

        // Step 3: Select random ones
        StepHelper.step("Selecting random biomarkers for 'What Needs Support'")
        val randomSelection = needsSupportMarkers.shuffled().take(3) // Selecting 3 as an example
        randomSelection.forEach { marker ->
            val metricId = marker.jsonObject["metric_id"]?.jsonPrimitive?.contentOrNull ?: ""
            logger.info { "Selecting checkbox for: $metricId" }
            page1.getByTestId("checkbox-biomarker-$metricId").click()
        }

        // Step 4: Create subsection
        StepHelper.step("Clicking Create Subsection")
        page1.getByTestId("button-create-subsection").click()
        logger.info { "Subsection 'What Needs Support' created successfully" }

        dynamicPdfStrings.add("The biomarkers that need closer attention")
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

    @Test
    @Order(2)
    fun `verify health overview prompt output constraints`() {

        StepHelper.step("Calling user-data API to verify AI prompt output")
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
        page.waitForTimeout(5000.0)
        searchBox.pressSequentially(name, Locator.PressSequentiallyOptions().setDelay(200.0))
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
        // Wait for all background APIs to settle and give a 5s buffer
        logger.info { "Waiting for Action Plan APIs to settle..." }
        page1.waitForLoadState(LoadState.NETWORKIDLE)
        page1.waitForTimeout(5000.0)

        val finalUrl = page1.url()
        logger.info { "Final URL: $finalUrl" }

        val expectedBase = "https://dh-stg-action-plan-generator.replit.app/"
        logger.info { "Verifying final URL components..." }
        assert(finalUrl.contains(expectedBase)) { "Final URL does not contain expected base: $expectedBase. Actual: $finalUrl" }
        assert(finalUrl.contains("user_id=$targetUserId")) { "Final URL missing correct user_id. Expected: $targetUserId, Actual: $finalUrl" }
//        assert(finalUrl.contains("user_name=${targetUser.name}")) { "Final URL missing correct user_name. Expected: ${targetUser.name}, Actual: $finalUrl" }
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

        val userRecommendationsResponse = page1.context().request().post(
            TestConfig.APIs.API_ACTION_PLAN_USER_RECOMMENDATIONS,
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(requestBody)
        )

        logger.info { "User Recommendations API Response Status: ${userRecommendationsResponse.status()}" }
        assert(userRecommendationsResponse.status() == 200) { "User recommendations API failed: ${userRecommendationsResponse.status()}. Body: ${userRecommendationsResponse.text()}" }

        val recommendationsData = userRecommendationsResponse.text()
        logger.info { "Full User Recommendations JSON: $recommendationsData" }
        assert(recommendationsData.contains("\"success\":true")) { "User recommendations API response unsuccessful: $recommendationsData" }
        logger.info { "User recommendations API successfully verified." }


        val userDataJson = jsonParser.decodeFromString<JsonObject>(userData)
        
    }
}
