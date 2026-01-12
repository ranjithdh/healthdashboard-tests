package symptoms.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import utils.logger.logger
import java.util.regex.Pattern

class SymptomsPage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.SYMPTOMS_PAGE_URL


    private val symptomMap = mapOf(
        "Head" to listOf(
            "Headaches",
            "Faintness",
            "Insomnia"
        ),

        "Eyes" to listOf(
            "Bags, dark circles",
            "Light Sensitivity"
        ),

        "Ears" to listOf(
            "Ringing / hearing loss"
        ),

        "Nose" to listOf(
            "Sinus problems",
            "Hay fever",
            "Sneezing attacks"
        ),

        "Mouth / Throat" to listOf(
            "Chronic coughing",
            "Canker sores",
            "Sore Tongue / Glossitis",
            "Cracks at Mouth Corners",
            "Metallic Taste"
        ),

        "Skin" to listOf(
            "Acne",
            "Hives / rashes / dry skin",
            "Hair loss",
            "Flushing / hot flashes",
            "Excessive sweating",
            "Easy Bruising",
            "Slow Wound Healing",
            "Skin Pigmentation Changes",
            "Brittle Nails"
        ),

        "Heart" to listOf(
            "Rapid/pounding beats",
            "Frequent Chest pain",
            "Palpitations"
        ),

        "Lungs" to listOf(
            "Chest congestion",
            "Asthma / bronchitis",
            "Shortness of breath",
            "Difficulty breathing"
        ),

        "Digestive Tract" to listOf(
            "Frequent Diarrhea",
            "Constipation",
            "Bloating / gas",
            "Belching / passing gas"
        ),

        "Joint / Muscles" to listOf(
            "Pain in joints",
            "Arthritis",
            "Stiffness / limited movement",
            "Pain in muscles",
            "Feeling of weakness",
            "Bone Pain/Tenderness",
            "Muscle Cramps/Spasms",
            "Muscle Weakness"
        ),

        "Weight" to listOf(
            "Binge eating / drinking",
            "Craving certain foods",
            "Difficulty in losing weight",
            "Underweight",
            "Persistent weight gain",
            "Unexplained Weight Gain",
            "Unexplained Weight Loss"
        ),

        "Energy / Activity" to listOf(
            "Fatigue / sluggishness",
            "Apathy / lethargy",
            "Hyperactivity",
            "Restless leg"
        ),

        "Mind" to listOf(
            "Poor memory",
            "Poor concentration"
        ),

        "Mood" to listOf(
            "Mood swings",
            "Anxiety / fear / nervousness",
            "Anger / irritability",
            "Depression"
        ),

        "Other" to listOf(
            "Cold intolerance",
            "Cold extremities",
            "Low libido",
            "Persistent low-grade fever",
            "Frequent illness",
            "Frequent/urgent urination",
            "Burning Sensation in Feet",
            "Poor Coordination / Unsteady",
            "Cold Hands/Feet",
            "Swelling in Legs/Ankles",
            "Night Sweats",
            "Fever/Chills",
            "Frequent Infections",
            "Increased Thirst"
        ),

        "Lungs / Respiratory" to listOf(
            "Wheezing",
            "Chronic Cough with Phlegm"
        ),

        "Urinary" to listOf(
            "UTI"
        )
    )


    fun waitForSymptomsPageConfirmation(): SymptomsPage {
        logger.info("Waiting for mobileView.home page confirmation...")
        page.waitForURL(TestConfig.Urls.SYMPTOMS_PAGE_URL)
        return this
    }

    fun headerValidation() {
        val title = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Correlations Management"))
        val subTitle = page.getByText("Manage relationships between")
        val reportButtong = page.getByTestId("create-symptoms-button")
        val components = listOf(title, subTitle, reportButtong)
        components.forEach { it.waitFor() }
        // dialogValidation()

    }

    fun dialogValidation() {
        val title = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Report Symptoms"))
        val subTitle = page.getByText("Select any symptoms you're")
        val closeButton =
            page.getByRole(AriaRole.BUTTON).filter(Locator.FilterOptions().setHasText(Pattern.compile("^$")))
        val components = listOf(title, subTitle, closeButton)
        components.forEach { it.waitFor() }
    }

    fun onReportSymptomsButtonClick() {
        val reportButton = page.getByTestId("create-symptoms-button")
        reportButton.click()
    }


    fun reportOptionsValidations(){
        symptomMap.forEach { (section, symptoms) ->
            validationSection(section)
            validationSymptoms(symptoms)
        }
    }

    private fun validationSection(title: String) {
        val button = page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName(title)
        )
        button.scrollIntoViewIfNeeded()
        button.click()
    }

    private fun validationSymptoms(symptoms: List<String>) {
        symptoms.forEach { name ->
            val button = page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(name)
            )

            button.waitFor()
        }
    }


    private fun clickSection(title: String) {
        val button = page.getByRole(
            AriaRole.HEADING,
            Page.GetByRoleOptions().setName(title)
        )
        button.scrollIntoViewIfNeeded()
        button.click()
    }

    private fun clickSymptoms(symptoms: List<String>) {
        symptoms.forEach { name ->
            val button = page.getByRole(
                AriaRole.BUTTON,
                Page.GetByRoleOptions().setName(name)
            )

            button.scrollIntoViewIfNeeded()
            button.click()
        }
    }


    fun selectAllSymptoms() {
        symptomMap.forEach { (section, symptoms) ->
            clickSection(section)
            clickSymptoms(symptoms)
        }
    }


}