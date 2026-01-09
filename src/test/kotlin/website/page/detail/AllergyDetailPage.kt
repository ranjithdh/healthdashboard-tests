package website.page.detail

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import utils.logger.logger
import website.page.WebSiteBasePage


class AllergyDetailPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.ALLERGY_DETAIL

    private val header = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Allergies Test Panel"))

    val certifiedLabsSection = CertifiedLabsSection(page)

    fun waitForPageLoad(): AllergyDetailPage {
        header.waitFor()
        logger.info { "AllergyDetailPage loaded" }
        return this
    }

    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isDescription1Visible(): Boolean {
        return page.getByText("A detailed IgE-based allergy screen measuring reactions to common foods, inhalants, contact triggers and medications.").isVisible
    }

    fun isDescription2Visible(): Boolean {
        return page.getByText(
            "This test measures total IgE levels and screens for multiple allergen categories including foods, environmental inhalants, contact materials, and medications. Results are classified as mild, moderate (mid-high), or high allergy, giving a clear view of potential triggers to avoid or manage in daily life."
        ).isVisible
    }

    fun testingStepsVisible(): Boolean {
        return page.getByText("Blood sample", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("No fasting required").isVisible &&
                page.getByText("At-home sample collection", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Results within 72–120 hours").first().isVisible &&
                page.getByText("1-on-1 Expert guidance included", Page.GetByTextOptions().setExact(true)).isVisible
    }

    fun testAmountVisible(): Boolean {
        return page.getByText("₹").isVisible && page.getByText("12,999").isVisible
    }

    private val whatIsMeasured = page.getByText("What’s measured?")
    private val whoShouldTakeThisTest = page.getByText("Who should take this test?")
    private val whatToExpect = page.getByText("What to expect?")

    fun isWhatIsMeasuredSectionVisible(): Boolean {
        return whatIsMeasured.isVisible
    }

    fun clickWhatIsMeasuredButton() {
        whatIsMeasured.click()
    }

    fun isFoodAllergenVisible(): Boolean {
        return page.getByText("Food Allergens (88)").isVisible &&
                return page.getByText(
                    "Alcohol, Almond, Aniseed, Apple, Areca Nut, Bajra, Banana, Barley, Bay Leaves, Black Pepper, Brinjal, Cabbage, Capsicum, Cardamom, Carrot, Cashewnut, Cauliflower, Cheese, Chilly, Cinnamon, Clove, Cocoa, Coconut, Coffee, Coriander, Cotton Seed, Cucumber, Cumin, Curd, Dal Chana, Dal Masoor, Dal Moong, Dal Rajma, Dal Urad, Dates, Eggs, Fenugreek, Fig, Garlic, Ginger, Grapes, Groundnut, Guava, Hazel Nut, Honey, Jaggery, Katha, Kiwi, Lactose, Lady’s Finger, Lemon, Maize, Mango, Melon, Milk, Mushroom, Mustard, MSG (Ajinomoto), Nutmeg, Oat, Olive, Onion, Orange, Papaya, Peas, Pineapple, Pista, Potato, Radish, Ragi, Rice, Salt, Sesame, Soyabean, Spinach, Squash, Strawberry, Sunflower, Tamarind, Tea, Tomatoes, Toor Dal, Turmeric, Vinegar, Walnut, Wheat, Yeast"
                ).isVisible
    }

    fun isNonVegAllergenVisible(): Boolean {
        return page.getByText("Non-veg Allergens (8)").isVisible &&
                page.getByText("Beef, Chicken, Crab, Fish, Mutton, Pork, Prawn, Shrimp").isVisible
    }

    fun isInhalentAllergenVisible(): Boolean {
        return page.getByText("Inhalant Allergens (79)").isVisible &&
                page.getByText(
                    "Acacia Arabica, Adhatoda Vasica, Alternaria Tenuis, Amaranthus Spinosis, Argemone Mexicana, Artemisia Scoparia, Aspergillus Fumigatus, Aspergillus Niger, Aspergillus Tamarii, Azadirachta Indica, Bajra Dust, Bermuda Grass, Brassica Campestris, Buffalo Dander, Candida Albicans, Cannabis Sativa, Carica Papaya, Cassia Fistula, Cat Dander, Cedrus Deodara, Cenchrus Ciliaris, Chalk Powder, Chenopodium Album, Cladosporium Herb., Cockroach Dust, Cocus Nucifera, Cotton Dust, Cotton Fibers, Cow Dander, Cricket, Cynodon Dactylon, Cyperus Rotundus, Daisy, Dandelion, Dog Dander, Eucalyptus, Feather Dust, Flour Mill Dust, Goat Dander, Hay Dust, Holoptelea Integrifolia, Honey Bee, Horse Dander, House Dust, House Fly, Imperata Cylindrica, Jeera Dust, Johnson Grass, Jowar Dust, Juliflora, Juniperus Communis, Jute Dust, Kigelia Pinnata, Lawsonia Inermis, Mosquito, Mugwort, Neurospora spp., Paper Dust, Parthenium, Penicillium spp., Phoenix Dactylifera, Pine, Ragweed, Rice Dust, Ricinus Communis, Rhizopus Nigricans, Rye Grass, Sheep Wool, Sorghum Vulgare, Straw Dust, Suaeda Fructicosa, Tea Leaves, Typha Angustata, Wheat Dust, Wood Dust, Xanthium Strumarium, Zea Mays"
                ).isVisible
    }

    fun isContactAllergenVisible(): Boolean {
        return page.getByText("Contact Allergens (15)").isVisible &&
                page.getByText("Detergent, Hair Dye, Latex, Leather, Lime Stone, Nylon Fibers, Paints, Perfume, Plastic, Polyester, Silk, Smoking (Tobacco)").isVisible
    }

    fun isDrugAllergenVisible(): Boolean {
        return page.getByText("Drug Allergens (22)").isVisible &&
                page.getByText("Amoxycillin, Ampicillin, Aspirin, Brufen (Ibuprofen), Cefixime, Cefuroxime, Ciprofloxacin, Cloxacillin, Diclofenac, Doxycyclin, Erythromycin, Metamizole, Nimesulide, Norfloxacin, Ofloxacin, Oxacillin, Paracetamol, Penicillin, Sulpha, Tetracycline, Tinidazole, Xylocain").isVisible
    }


    fun isWhoShouldTakeThisTestVisible(): Boolean {
        return whoShouldTakeThisTest.isVisible
    }

    fun clickWhoShouldTakeThisTest() {
        whoShouldTakeThisTest.scrollIntoViewIfNeeded()
        return whoShouldTakeThisTest.click()
    }

    fun isWhoShouldTakeThisDescriptionVisible(): Boolean {
        return page.getByText("This test is valuable for anyone who wants clarity on hidden or known allergy triggers that impact daily health and quality of life.").isVisible
    }


    fun isFoodAndDietConcernVisible(): Boolean{
        return page.getByText("Food & Diet Concerns").isVisible &&
                page.getByText("Frequent digestive discomfort after meals").isVisible &&
                page.getByText("Suspected reactions to common foods or spices").isVisible &&
                page.getByText("Wanting to identify hidden intolerances").isVisible
    }

    fun isEnvironmentalAndSeasonalAllergensVisible(): Boolean {
        return page.getByText("Environmental & Seasonal Allergies").isVisible &&
                page.getByText("Sneezing, runny nose, or itchy eyes").isVisible &&
                page.getByText("Reactions to dust, pets, or pollen").isVisible &&
                page.getByText("Chronic sinus or breathing issues").isVisible
    }

    fun isDrugAndContactSensitivitiesVisible(): Boolean {
        return page.getByText("Drug & Contact Sensitivities").isVisible &&
                page.getByText("Unexplained rashes or skin irritation").isVisible &&
                page.getByText("Reactions after taking certain medications").isVisible &&
                page.getByText("Sensitivity to fabrics, fibers, or chemicals").isVisible
    }

    fun isPreventiveHealthVisible(): Boolean {
        return page.getByText("Preventive Health").isVisible &&
                page.getByText("Family history of asthma, eczema, or allergies").isVisible &&
                page.getByText("Children with recurrent respiratory or skin issues").isVisible &&
                page.getByText("Adults with unexplained fatigue or inflammation").isVisible
    }

    fun isWhatToExpectVisible(): Boolean {
        return whatToExpect.isVisible
    }

    fun clickWhatToExpect() {
        return whatToExpect.click()
    }

    fun isWhatToExpectDescriptionVisible(): Boolean {
        return page.getByText(
            "With a simple at-home blood draw, our certified labs measure IgE responses to common foods, environmental triggers, contact materials, and medications. Results are physician-reviewed and delivered in a clear, easy-to-read report within days, along with practical guidance on managing triggers and supporting your long-term allergy health."
        ).isVisible
    }

    fun isSampleCollectionVisible(): Boolean {
        return page.getByText("Sample Collection", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Book a convenient at-home blood draw with our certified phlebotomist.").isVisible
    }

    fun isYourResultsAreUpdatedInYourDashboardVisible(): Boolean {
        return page.getByText("Your Results Are Updated in Your Dashboard").isVisible &&
                page.getByText("Your sample is processed at a certified lab and your report is ready online within 72 hours.").isVisible
    }

    fun isCorrelateDataOnYourDashboardVisible(): Boolean {
        return page.getByText("Correlate Data On Your Dashboard").isVisible &&
                page.getByText("See how your hormone levels relate to cycle regularity and reproductive health.").isVisible
    }

    fun isGeta1on1ConsultWithOurLongevityExpertVisible(): Boolean {
        return page.getByText("Get a 1-on-1 Consult with our Longevity Expert").isVisible &&
                page.getByText("Receive personalised guidance based on your results and goals.").isVisible
    }

    fun isHowItWorksHeadingVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("How it works")).isVisible
    }

    fun isStep1ContentVisible(): Boolean {
        return page.getByText("01").first().isVisible &&
                page.getByText("At-Home Sample Collection", Page.GetByTextOptions().setExact(true)).isVisible &&
                page.getByText("Schedule the blood sample collection from the comfort of your home.").isVisible
    }

    fun isStep2ContentVisible(): Boolean {
        return page.getByText("02").first().isVisible &&
                page.getByText("Get Results in 72 Hours").isVisible &&
                page.getByText("Your sample is processed at a certified lab, and your report is ready online in 72 hours.").isVisible
    }

    fun isStep3ContentVisible(): Boolean {
        return page.getByText("03").first().isVisible &&
                page.getByText("-on-1 Expert Consultation").first().isVisible &&
                page.getByText("See how your antibody levels connect with your symptoms by talking to our experts.").isVisible
    }

    fun isStep4ContentVisible(): Boolean {
        return page.getByText("04").first().isVisible &&
                page.getByText("Track Progress Overtime").first().isVisible &&
                page.getByText("Monitor these markers over time to understand changes and treatment response.").isVisible
    }

}
