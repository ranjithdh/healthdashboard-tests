package website.page

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import config.TestConfig
import mu.KotlinLogging
import utils.report.StepHelper
import utils.report.StepHelper.CLICK_BIOMARKER
import utils.report.StepHelper.WAIT_WEBSITE_PAGE_LOAD

private val logger = KotlinLogging.logger {}


class WhatWeTestPage(page: Page) : WebSiteBasePage(page) {

    override val pageUrl = TestConfig.Urls.WHAT_WE_TEST

    val stopGuessingStartWithClaritySection = StopGuessingStartWithClaritySection(page,StopGuessingPageType.WHAT_WE_TEST)

    private val header = page.getByRole(
        AriaRole.HEADING,
        Page.GetByRoleOptions().setName("H e r e i s e v e r y t h i n g w e t e s t")
    )

    private val bookNow = page.locator("#join-btn-test")

    fun waitForPageLoad(): WhatWeTestPage {
        StepHelper.step(WAIT_WEBSITE_PAGE_LOAD + "What We Test")
        header.waitFor()
        logger.info { "What We Test page loaded" }
        return this
    }


    fun isPageHeadingVisible(): Boolean {
        return header.isVisible
    }

    fun isWhatIsBiomarkerTestingTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("What Is Biomarker Testing?")).isVisible
    }

    fun isWhatIsBiomarkingTextVisible(): Boolean {
        return page.getByText(
            "A biomarker test measures specific molecules in your body that reflect how your organs and systems are functioning. At Deep Holistics, biomarker testing forms the backbone of our approach to personalised wellness. It’s not about guesswork, generic plans, or one-size-fits-all solutions. Our testing uncovers precise, objective data about your body’s unique biology. This enables a deeply personalised, preventive, and data-driven approach to health, far beyond surface-level insights or symptom-based care."
        ).isVisible
    }

    fun isWhyItMattersTitleVisible(): Boolean {
        return page.getByText("Why It Matters").isVisible
    }


    fun isWhyItMattersSection1Visible(): Boolean {
        return page.getByText(
            "These markers act like signals from inside your body, helping identify early signs of imbalance, disease risk, or progress toward better health. Many health issues develop silently long before symptoms appear, which is why tracking the right biomarkers gives you the power to act early and precisely."
        ).isVisible &&
                page.getByText(" By monitoring these indicators over time, you can:").isVisible
    }

    fun isWhyItMattersSection2Visible(): Boolean {
        return page.getByText(
            "Detect early shifts linked to conditions like diabetes, heart disease, and inflammation, long before they escalate."
        ).isVisible &&
                page.getByText(
                    "Establish your personal health baseline to understand what “normal” looks like for you and track how it evolves."
                ).isVisible &&
                page.getByText(
                    "Measure progress objectively as you adjust your nutrition, exercise, or supplement routines."
                ).isVisible &&
                page.getByText(
                    "Make data-backed decisions that reflect your unique biology, not population averages or guesswork."
                ).isVisible
    }


    fun whyItMattersSection3Visible(): Boolean {
        return page.getByText(
            "This preventive approach transforms health management from reactive care into continuous optimisation, helping you understand and influence your well-being at every stage."
        ).isVisible
    }


    fun isBaseLineBloodPanelVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Baseline Blood Panel")).isVisible
    }

    fun isBaseLineBloodPanelDescriptionVisible(): Boolean {
        return page.getByText(
            "The following 100+ biomarkers are included with your first blood test."
        ).isVisible
    }

    fun isBookNowVisible() = bookNow.isVisible

    fun clickBookNowButton() {
        StepHelper.step(StepHelper.CLICK_HERO_BOOK_NOW)
        bookNow.click()
    }

    fun isHearHealthSectionTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Heart Health")).isVisible
    }

    fun isMetabolicHeathSectionTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Metabolic Health")).isVisible
    }

    fun isInflammationTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Inflammation")).isVisible
    }

    fun isMitochondrialHealthTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Mitochondrial Health")).isVisible
    }

    fun isHormoneHealthTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Hormone Health")).isVisible
    }

    fun isImmuneHealthTitleVisible(): Boolean {
        return page.getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName("Immune Health")).isVisible
    }


    fun isBiomarkerNameAndDescriptionVisible(name: String, description: String): Boolean {
        StepHelper.step(CLICK_BIOMARKER + name)
        val biomarkerName = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(name).setExact(true))
        val biomarkerDescription = page.getByLabel(name, Page.GetByLabelOptions().setExact(true)).getByText(description)

        biomarkerName.waitFor()
        biomarkerName.click()

        return biomarkerName.isVisible && biomarkerDescription.isVisible
    }


    //----------------------------- Heart Health ------------------------------

    fun isApolipoproteinA1APOA1Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Apolipoprotein A1 (APO-A1)",
            "Primary protein in HDL that removes excess cholesterol from arteries, supporting plaque prevention and long-term heart protection."
        )
    }

    fun isApolipoproteinBAPOBVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Apolipoprotein B (APO-B)",
            "Measures the number of artery-damaging cholesterol particles circulating in blood, strongly linked to plaque buildup and heart disease."
        )
    }

    fun isHDLToLDLRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "HDL / LDL Ratio",
            "Shows balance between protective and harmful cholesterol, offering clearer insight into cardiovascular risk than individual values."
        )
    }

    fun isHDLCholesterolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "HDL Cholesterol",
            "Protective cholesterol that helps clear excess fats from arteries and supports overall cardiovascular health."
        )
    }

    fun isHomocysteineVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Homocysteine",
            "Elevated levels damage blood vessels, increase inflammation, and raise risk of heart disease, stroke, and clot formation."
        )
    }

    fun isLDLToHDLRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "LDL / HDL Ratio",
            "Indicates dominance of artery-clogging cholesterol compared to protective cholesterol, refining cardiovascular risk assessment."
        )
    }

    fun isLDLCholesterolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "LDL Cholesterol",
            "Transports cholesterol to tissues but contributes to arterial plaque when levels or particle quality are unfavourable."
        )
    }

    fun isLipoproteinAVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Lipoprotein A [LP(A)]",
            "Genetically determined cholesterol particle linked to early, aggressive cardiovascular disease and increased clot risk."
        )
    }

    fun isNonHDLCholesterolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Non-HDL Cholesterol",
            "Captures all harmful cholesterol particles together, offering broader cardiovascular risk insight than LDL alone."
        )
    }

    fun isSmallDenseLDLCholesterolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Small Dense Low-Density Lipoprotein Cholesterol (sdLDL-C)",
            "Smaller LDL particles that penetrate artery walls easily and oxidise faster, significantly increasing heart disease risk."
        )
    }

    fun isTotalCholesterolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Cholesterol",
            "Measures overall cholesterol levels but lacks detail on particle type, balance, and true cardiovascular risk."
        )
    }

    fun isTotalCholesterolToHDLRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Cholesterol to HDL Ratio (TC/HDL)",
            "Summarises overall cholesterol burden relative to protective HDL, helping assess heart disease risk."
        )
    }

    fun isTriglyceridesVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Triglycerides (TGL)",
            "Circulating fats reflecting sugar intake, metabolic efficiency, and insulin resistance risk."
        )
    }

    fun isTriglyceridesToHDLRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Triglycerides / HDL Ratio (TRIG/HDL)",
            "Strong marker of insulin resistance and hidden metabolic dysfunction linked to cardiovascular risk."
        )
    }

    fun isVLDLCholesterolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Very Low-Density Lipoprotein (VLDL) Cholesterol",
            "Transports triglycerides from liver to tissues, often elevated in insulin resistance and fatty liver conditions."
        )
    }


    //----------------------------- Metabolic Health ------------------------------

    fun isAverageBloodGlucoseVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Average Blood Glucose",
            "Represents overall blood sugar exposure over time, influencing metabolic health and long-term risk of diabetes and complications across the body."
        )
    }

    fun isFastingBloodGlucoseVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Fasting Blood Glucose",
            "Indicates baseline blood sugar control without recent food influence, revealing early glucose regulation problems before insulin resistance develops silently further."
        )
    }

    fun isFastingInsulinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Fasting Insulin",
            "Shows how hard the body works to keep blood sugar normal, identifying insulin resistance before glucose rises over time significantly."
        )
    }

    fun isHOMAIRVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "HOMA-IR",
            "Calculates insulin resistance using fasting glucose and insulin, helping detect metabolic dysfunction early before symptoms appear, enabling preventive lifestyle action."
        )
    }

    fun isHaemoglobinA1CVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Haemoglobin A1C (HbA1C)",
            "Reflects average blood sugar levels over the past three months, indicating long-term glucose control and cumulative metabolic stress exposure risk."
        )
    }

    fun isPostprandialBloodGlucoseVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Postprandial Blood Glucose",
            "Measures insulin response after meals, revealing metabolic flexibility and early insulin resistance patterns linked to energy crashes, weight gain, risk."
        )
    }

    fun isPostprandialInsulinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Postprandial Insulin",
            "Measures insulin response after meals, revealing metabolic flexibility and early insulin resistance patterns linked to energy crashes, weight gain, risk."
        )
    }




    //----------------------------- Inflammation ------------------------------

    fun isFerritinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Ferritin",
            "Indicates iron storage levels but also rises with inflammation, helping identify deficiency, overload, or chronic inflammatory stress affecting energy levels."
        )
    }

    fun isHighSensitivityCRPVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "High Sensitivity C-Reactive Protein (hs-CRP)",
            "Sensitive marker of low-grade chronic inflammation, strongly linked to cardiovascular disease, insulin resistance, metabolic dysfunction, and accelerated biological ageing processes."
        )
    }




    //----------------------------- Mitochondrial Health ------------------------------

    fun isInterleukin6Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Interleukin 6 (IL-6)",
            "Inflammatory signalling molecule that rises with cellular stress, impairing mitochondrial energy production, recovery capacity, and long-term metabolic resilience."
        )
    }



    //----------------------------- Hormone Health ------------------------------

    fun isCortisolVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Cortisol",
            "Primary stress hormone influencing energy, sleep, metabolism, immune balance, and long-term resilience when chronically elevated or suppressed over time consistently."
        )
    }

    fun isDHEASSulphateVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "DHEA-Sulphate (DHEAS)",
            "Supports resilience, recovery, and hormonal balance, often declining with chronic stress, ageing, and reduced adaptive capacity over long term periods."
        )
    }

    fun isFreeTestosteroneVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Free Testosterone",
            "Represents biologically active testosterone affecting energy, muscle mass, mood, libido, motivation, metabolic health, and physical performance across daily life functions."
        )
    }

    fun isSHBGVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Sex Hormone Binding Globulin (SHBG)",
            "Regulates availability of sex hormones by binding testosterone and oestrogen, influencing energy, fertility, body composition, and metabolic health outcomes overall."
        )
    }

    fun isTotalTestosteroneVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Testosterone",
            "Measures total circulating testosterone without indicating how much hormone is biologically active or available to tissues throughout the body daily."
        )
    }



    //----------------------------- Immune Health ------------------------------

    fun isBasophilsPercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Basophils %",
            "Percentage of white blood cells that are basophils which release histamine and play a role in allergic or inflammatory responses."
        )
    }

    fun isBasophilsAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Basophils – Absolute Count",
            "Absolute basophil count clarifies allergy driven inflammation beyond percentage shifts, improving immune assessment accuracy during chronic exposure stress responses states."
        )
    }

    fun isEosinophilsPercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Eosinophils %",
            "Indicates allergic responses and parasite defence activity, signalling airway inflammation or immune activation during recurrent exposures, asthma, and tissue irritation."
        )
    }

    fun isEosinophilsAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Eosinophils – Absolute Count",
            "Absolute eosinophil count quantifies severity of allergic or inflammatory activity more reliably than percentages alone during monitoring treatment response trends."
        )
    }

    fun isImmatureGranulocytePercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Immature Granulocyte % (IG)",
            "Signals early bone marrow response to infection, inflammation, or physiological stress before symptoms appear, indicating acute immune activation states developing."
        )
    }

    fun isImmatureGranulocyteAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Immature Granulocyte (IG)",
            "Absolute immature granulocytes indicate heightened immune activation during infections, inflammation, or marrow stress reflecting early defence mobilisation demands systemwide responses."
        )
    }

    fun isLymphocytesPercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Lymphocytes %",
            "Reflects balance of adaptive immunity and viral defence, influenced by stress, infections, and recovery status across time, training, sleep, nutrition."
        )
    }

    fun isLymphocytesAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Lymphocytes – Absolute Count",
            "Measures immune competence and sustained defence capacity, useful for assessing chronic stress or immune suppression during illness, recovery, training, ageing."
        )
    }

    fun isMonocytesPercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Monocytes %",
            "Indicates chronic inflammation and tissue repair activity, often elevated during prolonged immune activation infections, metabolic stress, autoimmunity, and healing phases."
        )
    }

    fun isMonocytesAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Monocytes – Absolute Count",
            "Assesses inflammatory burden and macrophage driven repair activity within immune system during chronic disease, injury, recovery, metabolic stress, states overall."
        )
    }

    fun isNeutrophilsPercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Neutrophils %",
            "Represents first line immune response to acute infection, inflammation, or physiological stress signalling immediate defence readiness and inflammatory load changes."
        )
    }

    fun isNeutrophilsAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Neutrophils – Absolute Count",
            "Measures acute immune readiness and bacterial defence capacity more accurately than percentage values during infection, stress, inflammation, training, recovery cycles."
        )
    }

    fun isTotalWBCVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total WBC",
            "Overall indicator of immune system activity, balance, and response to infection or inflammation across acute, chronic, stress, illness, and recovery."
        )
    }


    //----------------------------- Blood Health ------------------------------

    fun isHaemoglobinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Haemoglobin",
            "Measures blood’s oxygen carrying capacity, essential for energy, endurance, cognitive function, and detecting anaemia or poor iron status levels overall."
        )
    }

    fun isHematocritVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Hematocrit",
            "Indicates proportion of blood made up of red cells, reflecting hydration status, oxygen delivery, and blood thickness levels overall balance."
        )
    }

    fun isMeanCorpuscularHaemoglobinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Mean Corpuscular Haemoglobin (MCH)",
            "Average haemoglobin amount per red blood cell, helping classify anaemia types and assess oxygen carrying efficiency across the body tissues."
        )
    }

    fun isMeanCorpuscularHaemoglobinConcentrationVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Mean Corpuscular Haemoglobin Concentration (MCHC)",
            "Concentration of haemoglobin within red cells, indicating cell quality and supporting diagnosis of anaemia patterns and oxygen transport efficiency overall."
        )
    }

    fun isMeanCorpuscularVolumeVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Mean Corpuscular Volume (MCV)",
            "Average size of red blood cells, useful for identifying nutrient deficiencies and different anaemia categories affecting energy levels daily function."
        )
    }

    fun isMeanPlateletVolumeVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Mean Platelet Volume (MPV)",
            "Reflects average platelet size, indicating platelet activity, clotting potential, and inflammatory or cardiovascular risk during stress, illness, recovery periods overall."
        )
    }

    fun isNRBCPercentageVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Nucleated Red Blood Cells % (NRBC)",
            "Percentage of immature red cells in blood, signalling bone marrow stress, hypoxia, or severe illness and impaired oxygen delivery states."
        )
    }

    fun isNRBCAbsoluteCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Nucleated Red Blood Cells (NRBC)",
            "Absolute count of immature red blood cells, indicating significant marrow stress or abnormal blood cell production during illness, hypoxia, recovery."
        )
    }

    fun isPlateletCountVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Platelet Count",
            "Measures number of platelets, essential for clot formation, bleeding control, and wound healing capacity during injury, surgery, illness, recovery phases."
        )
    }

    fun isPlateletDistributionWidthVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Platelet Distribution Width (PDW)",
            "Shows variation in platelet size, reflecting platelet activation, turnover, and clotting behaviour during inflammation, stress, illness, recovery periods overall balance."
        )
    }

    fun isPlateletToLargeCellRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Platelet To Large Cell Ratio (P-LCR)",
            "Proportion of large platelets, indicating platelet activation and increased clotting or cardiovascular risk during inflammation, stress, metabolic dysfunction states overall."
        )
    }

    fun isPlateletcritVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Plateletcrit (PCT)",
            "Total platelet volume in blood, reflecting overall clotting capacity and platelet mass during bleeding, inflammation, illness, recovery, stress conditions overall."
        )
    }

    fun isRDWCVVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Red Cell Distribution Width – Coefficient Of Variation (RDW-CV)",
            "Measures variation in red blood cell size, helping detect anaemia, nutrient deficiencies, and chronic disease affecting energy, endurance, recovery, health."
        )
    }

    fun isRDWSDVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Red Cell Distribution Width – Standard Deviation (RDW-SD)",
            "Assesses spread of red blood cell sizes, offering deeper insight into anaemia and marrow function during deficiency, stress, illness, recovery."
        )
    }

    fun isTotalRBCVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total RBC",
            "Counts total red blood cells, reflecting oxygen transport capacity, endurance, and overall blood health status during training, illness, recovery, ageing."
        )
    }


    //----------------------------- Nutrients, Vitamins, and Minerals ------------------------------

    fun isCalciumVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Calcium",
            "Essential mineral for bone strength, muscle contraction, nerve signalling, and maintaining stable heart rhythm and structural integrity."
        )
    }

    fun isFolateVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Folate",
            "Critical vitamin for DNA synthesis, red blood cell production, cellular repair, and supporting healthy pregnancy and cardiovascular function."
        )
    }

    fun isIronVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Iron",
            "Required for oxygen transport, energy production, cognitive function, and preventing fatigue, weakness, and iron deficiency anaemia"
        )
    }

    fun isMagnesiumVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Magnesium",
            "Regulates energy production, muscle relaxation, nerve function, glucose control, and supports sleep quality and stress resilience."
        )
    }

    fun isSeleniumVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Selenium",
            "Antioxidant mineral supporting immune defence, thyroid hormone metabolism, and protection against oxidative cellular damage."
        )
    }

    fun isTIBCVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Iron Binding Capacity (TIBC)",
            "Measures blood’s ability to bind and transport iron, helping assess iron deficiency or overload states accurately."
        )
    }

    fun isTransferrinSaturationVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Transferrin Saturation % (TS)",
            "Indicates how much circulating iron is actually available for use by tissues and red blood cell production."
        )
    }

    fun isUIBCVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Unsaturated Iron Binding Capacity (UIBC)",
            "Shows remaining capacity of transferrin to bind iron, useful for understanding iron balance and absorption efficiency."
        )
    }

    fun isVitaminAVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin A (Retinol)",
            "Supports vision, immune defence, skin integrity, and cellular growth and differentiation across multiple body systems."
        )
    }

    fun isVitaminB1Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B1 (Thiamin)",
            "Essential for carbohydrate metabolism, nerve signalling, and converting food into usable cellular energy."
        )
    }

    fun isVitaminB12Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B12 (Cobalamin)",
            "Required for nerve health, DNA synthesis, red blood cell formation, and maintaining cognitive and neurological function."
        )
    }

    fun isVitaminB2Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B2 (Riboflavin)",
            "Supports cellular energy production, antioxidant activity, and metabolism of fats, proteins, and carbohydrates."
        )
    }

    fun isVitaminB3Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B3 (Niacin)",
            "Regulates energy metabolism, DNA repair, and supports healthy cholesterol balance and cardiovascular function."
        )
    }

    fun isVitaminB5Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B5 (Pantothenic Acid)",
            "Essential for hormone synthesis, energy metabolism, and production of neurotransmitters and stress response molecules."
        )
    }

    fun isVitaminB6Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B6 (Pyridoxine)",
            "Required for neurotransmitter synthesis, immune function, and metabolism of amino acids and glycogen."
        )
    }

    fun isVitaminB7Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B7 (Biotin)",
            "Supports fat, carbohydrate, and protein metabolism, contributing to energy balance, skin, hair, and nail health."
        )
    }

    fun isVitaminB9Visible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin B9 (Folic Acid)",
            "Essential for cell division, DNA synthesis, and prevention of anaemia and developmental abnormalities."
        )
    }

    fun isVitaminDVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin D",
            "Hormone-like vitamin supporting bone health, immune regulation, muscle strength, and metabolic and inflammatory balance."
        )
    }

    fun isVitaminEVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Vitamin E",
            "Protects cell membranes from oxidative damage, supporting immune function and cardiovascular health."
        )
    }

    fun isZincVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Zinc",
            "Essential for immune defence, wound healing, hormone production, taste perception, and cellular growth and repair."
        )
    }


    //----------------------------- Thyroid Health ------------------------------

    fun isFreeT3ToFreeT4RatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Free T3 / Free T4 Ratio",
            "Indicates how efficiently inactive thyroid hormone is converted into active hormone driving metabolism, energy, and body temperature regulation."
        )
    }

    fun isFreeT3ToT4RiskVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Free T3 / T4 Risk",
            "Compares active hormone levels to evaluate how well T4 is converting to T3 and whether cellular thyroid function is optimal."
        )
    }

    fun isFreeThyroxineVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Free Thyroxine (fT4)",
            "Represents circulating inactive thyroid hormone available for conversion into active T3 as the body requires energy regulation."
        )
    }

    fun isFreeTriiodothyronineVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Free Triiodothyronine (fT3)",
            "Active thyroid hormone directly controlling metabolism, energy levels, heart rate, body temperature, and overall metabolic pace."
        )
    }

    fun isT3ToT4RatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "T3 / T4 Ratio",
            "Reflects efficiency of thyroid hormone conversion, helping identify functional hypothyroidism despite normal individual hormone levels."
        )
    }

    fun isTSHVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Thyroid Stimulating Hormone (TSH)",
            "Brain-derived signal telling the thyroid how much hormone to produce, reflecting overall thyroid demand and feedback balance."
        )
    }

    fun isTotalT3ToT4RiskVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total T3 / T4 Risk",
            "Looks at total hormone output and balance, helping assess if your thyroid is underactive or overactive over time."
        )
    }

    fun isTotalThyroxineVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Thyroxine (T4)",
            "Measures total circulating thyroxine, including bound and unbound hormone, offering context to thyroid production capacity."
        )
    }

    fun isTotalTriiodothyronineVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Triiodothyronine (T3)",
            "Measures total circulating active thyroid hormone, influenced by binding proteins and overall thyroid output levels."
        )
    }


    //----------------------------- Liver Health ------------------------------

    fun isAlanineTransaminaseVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Alanine Transaminase (SGPT)",
            "Enzyme released during liver cell damage, helping detect liver stress from fat accumulation, alcohol, medications, or metabolic dysfunction."
        )
    }

    fun isAlbuminGlobulinRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Albumin / Globulin (A/G) Ratio",
            "Reflects balance between liver-produced proteins and immune proteins, indicating liver function, inflammation, and nutritional status."
        )
    }

    fun isAlkalinePhosphataseVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Alkaline Phosphatase (ALP)",
            "Marker of bile flow and liver function, also influenced by bone activity and metabolic health conditions."
        )
    }

    fun isAspartateAminotransferaseVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Aspartate Aminotransferase (SGOT)",
            "Enzyme indicating liver or muscle tissue damage, interpreted alongside SGPT to identify injury patterns."
        )
    }

    fun isDirectBilirubinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Bilirubin – Direct",
            "Reflects liver’s ability to process and excrete waste from red blood cell breakdown efficiently."
        )
    }

    fun isIndirectBilirubinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Bilirubin – Indirect",
            "Indicates breakdown of red blood cells before liver processing, useful for detecting haemolysis or liver dysfunction."
        )
    }

    fun isFattyLiverIndexVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Fatty Liver Index (FLI)",
            "Composite score estimating risk of liver fat accumulation linked to insulin resistance and metabolic syndrome."
        )
    }

    fun isGGTVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Gamma-Glutamyl Transferase (GGT)",
            "Sensitive marker of liver stress, alcohol exposure, oxidative stress, and early metabolic dysfunction."
        )
    }

    fun isSGOTToSGPTRatioVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "SGOT / SGPT Ratio",
            "Helps differentiate causes of liver injury, including fatty liver, alcohol-related damage, or muscle involvement."
        )
    }

    fun isSerumAlbuminVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Serum Albumin",
            "Major liver-produced protein reflecting nutritional status, liver synthetic capacity, and fluid balance in the body."
        )
    }

    fun isTotalBilirubinVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Total Bilirubin",
            "Measures overall waste clearance efficiency of the liver from red blood cell breakdown products."
        )
    }


    //----------------------------- Kidney Health ------------------------------

    fun isBloodUreaVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Blood Urea",
            "Indicates how effectively kidneys remove protein waste from blood, influenced by hydration, diet, liver function, and kidney health."
        )
    }

    fun isCreatinineVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Creatinine",
            "Waste product filtered by kidneys, used to assess kidney filtration efficiency and overall renal function stability."
        )
    }

    fun iseGFRVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Estimated Glomerular Filtration Rate (eGFR)",
            "Calculates kidney filtering capacity, helping stage kidney health and detect early or progressive renal dysfunction accurately."
        )
    }

    fun isUricAcidVisible(): Boolean {
        return isBiomarkerNameAndDescriptionVisible(
            "Uric Acid",
            "Metabolic waste that rises with impaired kidney clearance, dehydration, insulin resistance, or inflammation, increasing gout and kidney stone risk."
        )
    }

    private val addOnTestPageType = AddOnTestPageType.WHAT_WE_TEST
    val addOnTestCards = AddOnTestCards(page, addOnTestPageType)

}
