package symptoms.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.BasePage
import config.TestConfig
import utils.logger.logger
import java.util.regex.Pattern
import kotlin.random.Random
import kotlin.test.assertEquals

class SymptomsPage(page: Page) : BasePage(page) {
    override val pageUrl = TestConfig.Urls.SYMPTOMS_PAGE_URL

    val selectionSymptoms = mutableMapOf<String, List<String>>()

    val symptoms = mapOf(
        "Head" to listOf(
            "Headaches", "Faintness", "Insomnia"
        ), "Eyes" to listOf(
            "Bags, dark circles", "Light Sensitivity"
        ), "Ears" to listOf(
            "Ringing / hearing loss"
        ), "Nose" to listOf(
            "Sinus problems", "Hay fever", "Sneezing attacks"
        ), "Mouth / Throat" to listOf(
            "Chronic coughing", "Canker sores", "Sore Tongue / Glossitis", "Cracks at Mouth Corners", "Metallic Taste"
        ), "Skin" to listOf(
            "Acne",
            "Hives / rashes / dry skin",
            "Hair loss",
            "Flushing / hot flashes",
            "Excessive sweating",
            "Easy Bruising",
            "Slow Wound Healing",
            "Skin Pigmentation Changes",
            "Brittle Nails"
        ), "Heart" to listOf(
            "Rapid/pounding beats", "Frequent Chest pain", "Palpitations"
        ), "Lungs" to listOf(
            "Chest congestion", "Asthma / bronchitis", "Shortness of breath", "Difficulty breathing"
        ), "Digestive Tract" to listOf(
            "Frequent Diarrhea", "Constipation", "Bloating / gas", "Belching / passing gas"
        ), "Joint / Muscles" to listOf(
            "Pain in joints",
            "Arthritis",
            "Stiffness / limited movement",
            "Pain in muscles",
            "Feeling of weakness",
            "Bone Pain/Tenderness",
            "Muscle Cramps/Spasms",
            "Muscle Weakness"
        ), "Weight" to listOf(
            "Binge eating / drinking",
            "Craving certain foods",
            "Difficulty in losing weight",
            "Underweight",
            "Persistent weight gain",
            "Unexplained Weight Gain",
            "Unexplained Weight Loss"
        ), "Energy / Activity" to listOf(
            "Fatigue / sluggishness", "Apathy / lethargy", "Hyperactivity", "Restless leg"
        ), "Mind" to listOf(
            "Poor memory", "Poor concentration"
        ), "Mood" to listOf(
            "Mood swings", "Anxiety / fear / nervousness", "Anger / irritability", "Depression"
        ), "Other" to listOf(
            "Cold intolerance",
            "Cold extremities (feeling",
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
        ), "Lungs / Respiratory" to listOf(
            "Wheezing", "Chronic Cough with Phlegm"
        ), "Urinary" to listOf(
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
    }

    fun dialogValidation() {
        val title = page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Report Symptoms"))
        val subTitle = page.getByText("Select any symptoms you're")
        val symptomsCount = page.getByText("symptoms selected")
        val closeButton =
            page.getByRole(AriaRole.BUTTON).filter(Locator.FilterOptions().setHasText(Pattern.compile("^$")))
        val submitSymptoms = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit Symptoms"))
        val components = listOf(title, subTitle, symptomsCount, closeButton, submitSymptoms)
        components.forEach { it.waitFor() }
    }

    fun onReportSymptomsButtonClick() {
        val reportButton = page.getByTestId("create-symptoms-button")
        reportButton.click()
    }


    fun reportOptionsValidations() {
        symptoms.forEach { (section, symptomList) ->
            expandSection(section)
            symptomList.forEach { symptom ->
                selectSymptom(symptom)
            }
        }
    }

    fun expandSection(section: String) {
        val heading = page.getByRole(AriaRole.HEADING)
            .filter(
                Locator.FilterOptions().setHasText(section)
            )
            .first() // pick the first matching element
        heading.scrollIntoViewIfNeeded()
        heading.waitFor()
    }

    fun selectSymptom(symptomName: String) {
        page.getByRole(
            AriaRole.BUTTON, Page.GetByRoleOptions().setName(symptomName)
        ).waitFor()
    }


    private fun validationSection(title: String) {
        val button = page.getByRole(
            AriaRole.HEADING, Page.GetByRoleOptions().setName(title)
        )
        button.scrollIntoViewIfNeeded()
        button.waitFor()
    }

    private fun validationSymptoms(symptoms: List<String>) {
        symptoms.forEach { name ->
            val button = page.getByRole(
                AriaRole.BUTTON, Page.GetByRoleOptions().setName(name)
            )
            button.waitFor()
        }
    }


    private fun clickSymptoms(symptoms: List<String>) {
        symptoms.forEach { name ->
            val button = page.getByRole(
                AriaRole.BUTTON, Page.GetByRoleOptions().setName(name)
            )

            button.scrollIntoViewIfNeeded()
            button.click()
        }
    }


    fun selectAllSymptoms() {
        symptoms.forEach { (section, symptoms) ->
            val selectedSymptoms = randomSubList(symptoms, 1, 3)
            selectionSymptoms[section] = selectedSymptoms
            clickSymptoms(selectedSymptoms)
            symptomsSelectedCount(selectionSymptoms)
        }
    }

    fun cancelButtonClick() {
        val cancelButton = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel"))
        cancelButton.click()
    }


    fun symptomsSelectedCount(selectionSymptoms: MutableMap<String, List<String>>) {
        val symptomsCount = page.getByText("symptoms selected")
        var count = 0
        selectionSymptoms.forEach { (string, symptomsList) ->
            count = count.plus(symptomsList.size)
        }
        assertEquals("$count symptoms selected", symptomsCount.innerText())
    }

    fun submitSymptoms() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit Symptoms")).click()
    }

    fun resetAllSymptoms() {
        val resetAllSymptoms = page.getByRole(
            AriaRole.BUTTON,
            Page.GetByRoleOptions().setName("Reset All Symptoms")
        )
        resetAllSymptoms.waitFor()
        resetAllSymptoms.click()
    }

    fun resetConfirmationDialog() {
        val dialog = page.getByRole(
            AriaRole.ALERTDIALOG,
            Page.GetByRoleOptions().setName("Are you absolutely sure?")
        )

        dialog.waitFor()


    }

    fun <T> randomSubList(list: List<T>, min: Int = 1, max: Int = 3): List<T> {
        if (list.isEmpty()) return emptyList()
        val count = Random.nextInt(min, minOf(max, list.size) + 1)
        return list.shuffled().take(count)
    }


}