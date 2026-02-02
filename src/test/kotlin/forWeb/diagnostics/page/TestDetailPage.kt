package forWeb.diagnostics.page

import com.microsoft.playwright.*
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

/**
 * Test Detail Page - handles interactions with the test detail page
 * URL pattern: test-detail/{type}/{id}
 * where type can be: packages, test_profiles, or tests
 */
class TestDetailPage(page: Page) : BasePage(page) {

    override val pageUrl = "/test-detail" // Base path, actual URL includes type and id

    /**
     * Wait for page to load and verify URL pattern
     */
    fun waitForPageLoad(): TestDetailPage {
        // Wait for URL to match pattern: test-detail/{type}/{id}
//        page.waitForURL(Regex(".*test-detail/(packages|test_profiles|tests)/\\d+"), Page.WaitForURLOptions().setTimeout(30000.0))

        page.waitForURL(
            Pattern.compile(".*/test-detail/(packages|test_profiles|tests)/\\d+"),
            Page.WaitForURLOptions().setTimeout(30_000.0)
        )
        logger.info { "Test Detail page loaded: ${page.url()}" }
        return this
    }

    /**
     * Verify "How it Works?" heading is present
     */
    fun verifyHowItWorksHeading(): TestDetailPage {
        logger.info { "Verifying 'How it Works?' heading" }
        val heading = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it Works?"))
        Assertions.assertTrue(heading.isVisible, "How it Works? heading should be visible")
        return this
    }

    /**
     * Verify step 01 heading and content
     */
    fun verifyStep01(): TestDetailPage {
        logger.info { "Verifying Step 01 content" }
        
        // Verify heading "01"
        val heading01 = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("01"))
        Assertions.assertTrue(heading01.isVisible, "Step 01 heading should be visible")
        
        // Verify image "At-Home Sample Collection"
        val image01 = byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("At-Home Sample Collection"))
        Assertions.assertTrue(image01.isVisible, "At-Home Sample Collection image should be visible")
        
        // Verify heading "At-Home Sample Collection"
        val headingCollection = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("At-Home Sample Collection"))
        Assertions.assertTrue(headingCollection.isVisible, "At-Home Sample Collection heading should be visible")
        
        // Verify text "Schedule the blood sample"
        val text01 = byText("Schedule the blood sample")
        Assertions.assertTrue(text01.isVisible, "Schedule the blood sample text should be visible")
        
        return this
    }

    /**
     * Verify step 02 heading and content
     */
    fun verifyStep02(): TestDetailPage {
        logger.info { "Verifying Step 02 content" }
        
        // Verify heading "02"
        val heading02 = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("02"))
        Assertions.assertTrue(heading02.isVisible, "Step 02 heading should be visible")
        
        // Verify image "Get results in 72 hrs"
        val image02 = byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Get results in 72 hrs"))
        Assertions.assertTrue(image02.isVisible, "Get results in 72 hrs image should be visible")
        
        // Verify heading "Get results in 72 hrs"
        val headingResults = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Get results in 72 hrs"))
        Assertions.assertTrue(headingResults.isVisible, "Get results in 72 hrs heading should be visible")
        
        // Verify text "Your sample is processed at a"
        val text02 = byText("Your sample is processed at a")
        Assertions.assertTrue(text02.isVisible, "Your sample is processed at a text should be visible")
        
        return this
    }

    /**
     * Verify step 03 heading and content
     */
    fun verifyStep03(): TestDetailPage {
        logger.info { "Verifying Step 03 content" }
        
        // Verify heading "03"
        val heading03 = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("03"))
        Assertions.assertTrue(heading03.isVisible, "Step 03 heading should be visible")
        
        // Verify image "-on-1 Expert Consultation"
        val image03 = byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("-on-1 Expert Consultation"))
        Assertions.assertTrue(image03.isVisible, "-on-1 Expert Consultation image should be visible")
        
        // Verify heading "-on-1 Expert Consultation"
        val headingConsultation = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("-on-1 Expert Consultation"))
        Assertions.assertTrue(headingConsultation.isVisible, "-on-1 Expert Consultation heading should be visible")
        
        // Verify text "See how your antibody levels"
        val text03 = byText("See how your antibody levels")
        Assertions.assertTrue(text03.isVisible, "See how your antibody levels text should be visible")
        
        return this
    }

    /**
     * Verify step 04 heading and content
     */
    fun verifyStep04(): TestDetailPage {
        logger.info { "Verifying Step 04 content" }
        
        // Verify heading "04"
        val heading04 = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("04"))
        Assertions.assertTrue(heading04.isVisible, "Step 04 heading should be visible")
        
        // Verify image "Track Progress Overtime"
        val image04 = byRole(AriaRole.IMG, Page.GetByRoleOptions().setName("Track Progress Overtime"))
        Assertions.assertTrue(image04.isVisible, "Track Progress Overtime image should be visible")
        
        // Verify heading "Track Progress Overtime"
        val headingProgress = byRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Track Progress Overtime"))
        Assertions.assertTrue(headingProgress.isVisible, "Track Progress Overtime heading should be visible")
        
        // Verify text "Monitor these markers over"
        val text04 = byText("Monitor these markers over")
        Assertions.assertTrue(text04.isVisible, "Monitor these markers over text should be visible")
        
        return this
    }

    /**
     * Verify "How it Works?" section dynamically
     */
    fun verifyHowItWorks(steps: List<Map<String, String>>): TestDetailPage {
        logger.info { "Verifying 'How it Works?' section dynamically with ${steps.size} steps" }

        steps.forEachIndexed { index, step ->
            val number = String.format("%02d", index + 1)
            val title = step["title"] ?: ""
            val description = step["description"] ?: ""

            logger.info { "Verifying step $number: $title" }

            // Verify Number Heading
            val numberHeading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(number).setExact(true))
            numberHeading.scrollIntoViewIfNeeded()
            Assertions.assertTrue(numberHeading.isVisible, "Step number $number should be visible")

            // Verify Title Heading
            val titleHeading = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(title))
            titleHeading.scrollIntoViewIfNeeded()
            Assertions.assertTrue(titleHeading.isVisible, "Step title '$title' should be visible")

            // Verify Description
            // Description might be long, so we check if visible by text
            val descElement = page.getByText(description)
            descElement.scrollIntoViewIfNeeded()
            Assertions.assertTrue(descElement.isVisible, "Description for step $number should be visible")
        }
        return this
    }

    /**
     * Verify "Certified Labs, Secure Data" section
     */
    fun verifyCertifiedLabsSection(): TestDetailPage {
        logger.info { "Verifying Certified Labs, Secure Data section" }
        
        // Verify Main Heading
        val headingCertified = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Certified Labs, Secure Data"))
        headingCertified.scrollIntoViewIfNeeded()
        Assertions.assertTrue(headingCertified.isVisible, "Certified Labs, Secure Data heading should be visible")
        
        // Step 1: NABL and CAP Certified Laboratories
        val title1 = "NABL and CAP Certified Laboratories"
        val desc1 = "Each partner lab we work with is CAP-accredited and NABL-certified. This means they follow rigorous international and national quality standards, maintain state-of-the-art testing protocols, and undergo regular audits to ensure accuracy and reliability."
        
        val title1El = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(title1))
        title1El.scrollIntoViewIfNeeded()
        Assertions.assertTrue(title1El.isVisible, "Title '$title1' should be visible")
        
        val desc1El = page.getByText(desc1)
        Assertions.assertTrue(desc1El.isVisible, "Description for Certified Labs should be visible")

        // Step 2: Your privacy matters
        val title2 = "Your privacy matters"
        val desc2 = "Your health data is always protected with strict privacy safeguards. We use advanced encryption and secure servers to keep your results confidential and accessible only to you. Every step of the process is designed with your privacy in mind."
        
        val title2El = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(title2))
        title2El.scrollIntoViewIfNeeded()
        Assertions.assertTrue(title2El.isVisible, "Title '$title2' should be visible")
        
        val desc2El = page.getByText(desc2)
        Assertions.assertTrue(desc2El.isVisible, "Description for Privacy should be visible")
        
        return this
    }

    /**
     * Verify all static content on the page
     */
    fun verifyAllStaticContent(): TestDetailPage {
        logger.info { "Verifying all static content on test detail page" }
        
        waitForPageLoad()
        verifyHowItWorksHeading()
        verifyStep01()
        verifyStep02()
        verifyStep03()
        verifyStep04()
        verifyCertifiedLabsSection()
        
        logger.info { "All static content verified successfully" }
        return this
    }

    /**
     * Verify complete "How it Works?" section (wrapper for all steps)
     */
    fun verifyHowItWorksSection(): TestDetailPage {
        logger.info { "Verifying complete How it Works section" }
        verifyHowItWorksHeading()
        verifyStep01()
        verifyStep02()
        verifyStep03()
        verifyStep04()
        return this
    }

    /**
     * Verify "What's measured?" section dynamically
     */
    fun verifyWhatsMeasured(description: String, expectedData: Map<String, List<String>>): TestDetailPage {
        logger.info { "Verifying 'What's measured?' section dynamically" }

        // Click "What's measured?" button
        val whatsMeasuredBtn = byRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("What’s measured?"))
        whatsMeasuredBtn.click()
        
        // Verify description
        if (description.isNotEmpty()) {
            logger.info { "Verifying description: $description" }
            val descriptionElement = page.getByText(description)
            Assertions.assertTrue(descriptionElement.isVisible, "Description '$description' should be visible")
        }
        
//         Iterate through each category
        expectedData.forEach { (category, items) ->
            logger.info { "Verifying category: $category" }

            // Verify category header is visible (it might not be clickable if it's just a list header in this view)
            // Based on the image, it looks like a list with headers.
            val categoryHeader = page.getByText(category).first()
            categoryHeader.scrollIntoViewIfNeeded()
            Assertions.assertTrue(categoryHeader.isVisible, "Category '$category' should be visible")

            // Verify each item in the category
            items.forEach { item ->
                logger.info { "Verifying item: $item" }
                // Items are listed in a paragraph, possibly comma separated.
                // We check if the text is present.
                val itemLocator = page.getByText(item).first()
                itemLocator.scrollIntoViewIfNeeded()
                Assertions.assertTrue(itemLocator.isVisible, "Item '$item' in category '$category' should be visible")
            }
        }

        return this
    }

    fun verifyTestHeaderInfo(code: String): TestDetailPage {
        logger.info { "Verifying header info for code: $code" }
        
        val nameLocator = page.getByTestId("diagnostics-test-name-$code")
        val shortDescLocator = page.getByTestId("diagnostics-test-short-description-$code")
        val aboutDescLocator = page.getByTestId("diagnostics-test-about-description-$code")

        Assertions.assertTrue(nameLocator.isVisible, "Test name for $code should be visible")
        Assertions.assertTrue(shortDescLocator.isVisible, "Short description for $code should be visible")
        Assertions.assertTrue(aboutDescLocator.isVisible, "About description for $code should be visible")
        
        logger.info { "Test Name: ${nameLocator.innerText()}" }
        logger.info { "Short Description: ${shortDescLocator.innerText()}" }
        logger.info { "About Description: ${aboutDescLocator.innerText()}" }
        
        return this
    }

    fun verifyHighlights(highlights: List<String>): TestDetailPage {
        logger.info { "Verifying highlights section" }
        highlights.forEach { highlight ->
            logger.info { "Verifying highlight: $highlight" }
            val highlightLocator = page.getByText(highlight).first()
            highlightLocator.scrollIntoViewIfNeeded()
            Assertions.assertTrue(highlightLocator.isVisible, "Highlight '$highlight' should be visible")
        }
        return this
    }

    fun expandSection(sectionName: String): TestDetailPage {
        logger.info { "Expanding section: $sectionName" }
        val button = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(sectionName))
        button.scrollIntoViewIfNeeded()
        button.click()
        return this
    }

    fun expandAndVerifySection(sectionName: String, expectedText: String): TestDetailPage {
        logger.info { "Expanding and verifying section: $sectionName" }
        val button = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(sectionName))
        button.scrollIntoViewIfNeeded()
        button.click()

        if (expectedText.isNotEmpty()) {
            logger.info { "Verifying text: $expectedText" }
            val textElement = page.getByText(expectedText).first()
            textElement.scrollIntoViewIfNeeded()
            Assertions.assertTrue(textElement.isVisible, "Text '$expectedText' should be visible in section '$sectionName'")
            
            val actualText = textElement.innerText()
            logger.info { "Actual innerText: $actualText" }
            // We verify that the element found by text actually contains the text (sanity check)
            // and that innerText is accessible.
            Assertions.assertTrue(actualText.contains(expectedText) || expectedText.contains(actualText), 
                "innerText '$actualText' should match or contain expected '$expectedText'")
        } else {
            logger.warn { "Expected text for section '$sectionName' is empty" }
        }
        return this
    }

    fun verifyPriceAndBookingButton(code: String, expectedPrice: String): TestDetailPage {
        logger.info { "Verifying price and booking button for code: $code" }

        // Verify Price
        val priceLocator = page.getByTestId("test-price-$code")
        priceLocator.scrollIntoViewIfNeeded()
        Assertions.assertTrue(priceLocator.isVisible, "Price for $code should be visible")
        
        val actualPrice = priceLocator.innerText()
        logger.info { "Actual Price: $actualPrice, Expected: $expectedPrice" }
        Assertions.assertEquals(expectedPrice, actualPrice, "Price should match expected formatted price")

        // Verify Booking Button
        val bookingButton = page.getByTestId("diagnostics-test-booking-button-$code")
        bookingButton.scrollIntoViewIfNeeded()
        Assertions.assertTrue(bookingButton.isVisible, "Booking button for $code should be visible")
        
        val buttonText = bookingButton.innerText()
        logger.info { "Booking Button Text: $buttonText" }
        Assertions.assertTrue(buttonText.contains("Book Now", ignoreCase = true) || buttonText.contains("Add", ignoreCase = true), 
            "Booking button text should contain 'Book Now' or 'Add'")
        
        return this
    }

    fun clickBookNow(code: String): TestSchedulingPage {
        logger.info { "Clicking Book Now for code: $code" }
        val bookingButton = page.getByTestId("diagnostics-test-booking-button-$code")
        bookingButton.scrollIntoViewIfNeeded()
        bookingButton.click()
        return TestSchedulingPage(page)
    }
}
