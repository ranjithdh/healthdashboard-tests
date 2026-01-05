package website.test

import com.microsoft.playwright.*
import config.TestConfig
import org.junit.jupiter.api.*
import website.page.WhatWeTestPage


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WhatWeTestPageTest {

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
        val viewport = TestConfig.Viewports.DESKTOP_FHD
        val contextOptions = Browser.NewContextOptions()
            .setViewportSize(viewport.width, viewport.height)
            .setHasTouch(viewport.hasTouch)
            .setDeviceScaleFactor(viewport.deviceScaleFactor)

        context = browser.newContext(contextOptions)
        page = context.newPage()
    }

    @AfterEach
    fun closeContext() {
        context.close()
    }


    @Test
    fun `should display page heading`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isPageHeadingVisible()) { "Page heading should be visible" }

        whatWeTestPage.takeScreenshot("what-we-test-page-heading")
    }

    @Test
    fun `should display heart health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isHearHealthSectionTitleVisible()) { "Heart Health section title should be visible" }

        assert(whatWeTestPage.isApolipoproteinA1APOA1Visible()) { "Apolipoprotein A1 should be visible" }
        assert(whatWeTestPage.isApolipoproteinBAPOBVisible()) { "Apolipoprotein B should be visible" }
        assert(whatWeTestPage.isHDLToLDLRatioVisible()) { "HDL/LDL Ratio should be visible" }
        assert(whatWeTestPage.isHDLCholesterolVisible()) { "HDL Cholesterol should be visible" }
        assert(whatWeTestPage.isHomocysteineVisible()) { "Homocysteine should be visible" }
        assert(whatWeTestPage.isLDLToHDLRatioVisible()) { "LDL/HDL Ratio should be visible" }
        assert(whatWeTestPage.isLDLCholesterolVisible()) { "LDL Cholesterol should be visible" }
        assert(whatWeTestPage.isLipoproteinAVisible()) { "Lipoprotein A should be visible" }
        assert(whatWeTestPage.isNonHDLCholesterolVisible()) { "Non-HDL Cholesterol should be visible" }
        assert(whatWeTestPage.isSmallDenseLDLCholesterolVisible()) { "Small Dense LDL Cholesterol should be visible" }
        assert(whatWeTestPage.isTotalCholesterolVisible()) { "Total Cholesterol should be visible" }
        assert(whatWeTestPage.isTotalCholesterolToHDLRatioVisible()) { "Total Cholesterol/HDL Ratio should be visible" }
        assert(whatWeTestPage.isTriglyceridesVisible()) { "Triglycerides should be visible" }
        assert(whatWeTestPage.isTriglyceridesToHDLRatioVisible()) { "Triglycerides/HDL Ratio should be visible" }
        assert(whatWeTestPage.isVLDLCholesterolVisible()) { "VLDL Cholesterol should be visible" }
    }

    @Test
    fun `should display metabolic health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isMetabolicHeathSectionTitleVisible()) { "Metabolic Health section title should be visible" }

        assert(whatWeTestPage.isAverageBloodGlucoseVisible()) { "Average Blood Glucose should be visible" }
        assert(whatWeTestPage.isFastingBloodGlucoseVisible()) { "Fasting Blood Glucose should be visible" }
        assert(whatWeTestPage.isFastingInsulinVisible()) { "Fasting Insulin should be visible" }
        assert(whatWeTestPage.isHOMAIRVisible()) { "HOMA-IR should be visible" }
        assert(whatWeTestPage.isHaemoglobinA1CVisible()) { "Haemoglobin A1C should be visible" }
        assert(whatWeTestPage.isPostprandialBloodGlucoseVisible()) { "Postprandial Blood Glucose should be visible" }
        assert(whatWeTestPage.isPostprandialInsulinVisible()) { "Postprandial Insulin should be visible" }
    }

    @Test
    fun `should display inflammation indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isInflammationTitleVisible()) { "Inflammation section title should be visible" }

        assert(whatWeTestPage.isFerritinVisible()) { "Ferritin should be visible" }
        assert(whatWeTestPage.isHighSensitivityCRPVisible()) { "High Sensitivity CRP should be visible" }
    }

    @Test
    fun `should display mitochondrial health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isMitochondrialHealthTitleVisible()) { "Mitochondrial Health section title should be visible" }

        assert(whatWeTestPage.isInterleukin6Visible()) { "Interleukin 6 should be visible" }
    }

    @Test
    fun `should display hormone health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isHormoneHealthTitleVisible()) { "Hormone Health section title should be visible" }

        assert(whatWeTestPage.isCortisolVisible()) { "Cortisol should be visible" }
        assert(whatWeTestPage.isDHEASSulphateVisible()) { "DHEA-Sulphate should be visible" }
        assert(whatWeTestPage.isFreeTestosteroneVisible()) { "Free Testosterone should be visible" }
        assert(whatWeTestPage.isSHBGVisible()) { "SHBG should be visible" }
        assert(whatWeTestPage.isTotalTestosteroneVisible()) { "Total Testosterone should be visible" }
    }

    @Test
    fun `should display immune health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isImmuneHealthTitleVisible()) { "Immune Health section title should be visible" }

        assert(whatWeTestPage.isBasophilsPercentageVisible()) { "Basophils % should be visible" }
        assert(whatWeTestPage.isBasophilsAbsoluteCountVisible()) { "Basophils Absolute Count should be visible" }
        assert(whatWeTestPage.isEosinophilsPercentageVisible()) { "Eosinophils % should be visible" }
        assert(whatWeTestPage.isEosinophilsAbsoluteCountVisible()) { "Eosinophils Absolute Count should be visible" }
        assert(whatWeTestPage.isImmatureGranulocytePercentageVisible()) { "Immature Granulocyte % should be visible" }
        assert(whatWeTestPage.isImmatureGranulocyteAbsoluteCountVisible()) { "Immature Granulocyte Absolute Count should be visible" }
        assert(whatWeTestPage.isLymphocytesPercentageVisible()) { "Lymphocytes % should be visible" }
        assert(whatWeTestPage.isLymphocytesAbsoluteCountVisible()) { "Lymphocytes Absolute Count should be visible" }
        assert(whatWeTestPage.isMonocytesPercentageVisible()) { "Monocytes % should be visible" }
        assert(whatWeTestPage.isMonocytesAbsoluteCountVisible()) { "Monocytes Absolute Count should be visible" }
        assert(whatWeTestPage.isNeutrophilsPercentageVisible()) { "Neutrophils % should be visible" }
        assert(whatWeTestPage.isNeutrophilsAbsoluteCountVisible()) { "Neutrophils Absolute Count should be visible" }
        assert(whatWeTestPage.isTotalWBCVisible()) { "Total WBC should be visible" }
    }

    @Test
    fun `should display blood health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isHaemoglobinVisible()) { "Haemoglobin should be visible" }
        assert(whatWeTestPage.isHematocritVisible()) { "Hematocrit should be visible" }
        assert(whatWeTestPage.isMeanCorpuscularHaemoglobinVisible()) { "Mean Corpuscular Haemoglobin should be visible" }
        assert(whatWeTestPage.isMeanCorpuscularHaemoglobinConcentrationVisible()) { "Mean Corpuscular Haemoglobin Concentration should be visible" }
        assert(whatWeTestPage.isMeanCorpuscularVolumeVisible()) { "Mean Corpuscular Volume should be visible" }
        assert(whatWeTestPage.isMeanPlateletVolumeVisible()) { "Mean Platelet Volume should be visible" }
        assert(whatWeTestPage.isNRBCPercentageVisible()) { "NRBC % should be visible" }
        assert(whatWeTestPage.isNRBCAbsoluteCountVisible()) { "NRBC Absolute Count should be visible" }
        assert(whatWeTestPage.isPlateletCountVisible()) { "Platelet Count should be visible" }
        assert(whatWeTestPage.isPlateletDistributionWidthVisible()) { "Platelet Distribution Width should be visible" }
        assert(whatWeTestPage.isPlateletToLargeCellRatioVisible()) { "Platelet To Large Cell Ratio should be visible" }
        assert(whatWeTestPage.isPlateletcritVisible()) { "Plateletcrit should be visible" }
        assert(whatWeTestPage.isRDWCVVisible()) { "RDW-CV should be visible" }
        assert(whatWeTestPage.isRDWSDVisible()) { "RDW-SD should be visible" }
        assert(whatWeTestPage.isTotalRBCVisible()) { "Total RBC should be visible" }
    }

    @Test
    fun `should display nutrients vitamins and minerals indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isCalciumVisible()) { "Calcium should be visible" }
        assert(whatWeTestPage.isFolateVisible()) { "Folate should be visible" }
        assert(whatWeTestPage.isIronVisible()) { "Iron should be visible" }
        assert(whatWeTestPage.isMagnesiumVisible()) { "Magnesium should be visible" }
        assert(whatWeTestPage.isSeleniumVisible()) { "Selenium should be visible" }
        assert(whatWeTestPage.isTIBCVisible()) { "TIBC should be visible" }
        assert(whatWeTestPage.isTransferrinSaturationVisible()) { "Transferrin Saturation should be visible" }
        assert(whatWeTestPage.isUIBCVisible()) { "UIBC should be visible" }
        assert(whatWeTestPage.isVitaminAVisible()) { "Vitamin A should be visible" }
        assert(whatWeTestPage.isVitaminB1Visible()) { "Vitamin B1 should be visible" }
        assert(whatWeTestPage.isVitaminB12Visible()) { "Vitamin B12 should be visible" }
        assert(whatWeTestPage.isVitaminB2Visible()) { "Vitamin B2 should be visible" }
        assert(whatWeTestPage.isVitaminB3Visible()) { "Vitamin B3 should be visible" }
        assert(whatWeTestPage.isVitaminB5Visible()) { "Vitamin B5 should be visible" }
        assert(whatWeTestPage.isVitaminB6Visible()) { "Vitamin B6 should be visible" }
        assert(whatWeTestPage.isVitaminB7Visible()) { "Vitamin B7 should be visible" }
        assert(whatWeTestPage.isVitaminB9Visible()) { "Vitamin B9 should be visible" }
        assert(whatWeTestPage.isVitaminDVisible()) { "Vitamin D should be visible" }
        assert(whatWeTestPage.isVitaminEVisible()) { "Vitamin E should be visible" }
        assert(whatWeTestPage.isZincVisible()) { "Zinc should be visible" }
    }

    @Test
    fun `should display thyroid health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isFreeT3ToFreeT4RatioVisible()) { "Free T3 / Free T4 Ratio should be visible" }
        assert(whatWeTestPage.isFreeT3ToT4RiskVisible()) { "Free T3 / T4 Risk should be visible" }
        assert(whatWeTestPage.isFreeThyroxineVisible()) { "Free Thyroxine should be visible" }
        assert(whatWeTestPage.isFreeTriiodothyronineVisible()) { "Free Triiodothyronine should be visible" }
        assert(whatWeTestPage.isT3ToT4RatioVisible()) { "T3 / T4 Ratio should be visible" }
        assert(whatWeTestPage.isTSHVisible()) { "TSH should be visible" }
        assert(whatWeTestPage.isTotalT3ToT4RiskVisible()) { "Total T3 / T4 Risk should be visible" }
        assert(whatWeTestPage.isTotalThyroxineVisible()) { "Total Thyroxine should be visible" }
        assert(whatWeTestPage.isTotalTriiodothyronineVisible()) { "Total Triiodothyronine should be visible" }
    }

    @Test
    fun `should display liver health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isAlanineTransaminaseVisible()) { "Alanine Transaminase should be visible" }
        assert(whatWeTestPage.isAlbuminGlobulinRatioVisible()) { "Albumin / Globulin Ratio should be visible" }
        assert(whatWeTestPage.isAlkalinePhosphataseVisible()) { "Alkaline Phosphatase should be visible" }
        assert(whatWeTestPage.isAspartateAminotransferaseVisible()) { "Aspartate Aminotransferase should be visible" }
        assert(whatWeTestPage.isDirectBilirubinVisible()) { "Direct Bilirubin should be visible" }
        assert(whatWeTestPage.isIndirectBilirubinVisible()) { "Indirect Bilirubin should be visible" }
        assert(whatWeTestPage.isFattyLiverIndexVisible()) { "Fatty Liver Index should be visible" }
        assert(whatWeTestPage.isGGTVisible()) { "GGT should be visible" }
        assert(whatWeTestPage.isSGOTToSGPTRatioVisible()) { "SGOT / SGPT Ratio should be visible" }
        assert(whatWeTestPage.isSerumAlbuminVisible()) { "Serum Albumin should be visible" }
        assert(whatWeTestPage.isTotalBilirubinVisible()) { "Total Bilirubin should be visible" }
    }

    @Test
    fun `should display kidney health indicators`() {
        val whatWeTestPage = WhatWeTestPage(page).navigate() as WhatWeTestPage
        whatWeTestPage.waitForPageLoad()

        assert(whatWeTestPage.isBloodUreaVisible()) { "Blood Urea should be visible" }
        assert(whatWeTestPage.isCreatinineVisible()) { "Creatinine should be visible" }
        assert(whatWeTestPage.iseGFRVisible()) { "eGFR should be visible" }
        assert(whatWeTestPage.isUricAcidVisible()) { "Uric Acid should be visible" }
    }

}
