package profile.model

/**
 * Enum representing medical conditions for question 37
 */
enum class MedicalCondition(val buttonName: String, val label: String) {
    DERMATOLOGICAL("Dermatological Conditions", "Dermatological Conditions (e.g., eczema, acne, psoriasis)"),
    BONE_OR_JOINT("Bone or Joint Conditions", "Bone or Joint Conditions (e.g., arthritis, osteoporosis, ankylosing spondylitis)"),
    GASTROINTESTINAL("Gastrointestinal Conditions", "Gastrointestinal Conditions (e.g., IBS, GERD, IBD)"),
    NEUROLOGICAL("Neurological Conditions", "Neurological Conditions (e.g., migraine, epilepsy, Parkinson's)"),
    DIABETES("Type 2 - Diabetes", "Type 2 - Diabetes"), // Note: JSON Q37 Label is just "Type 2 - Diabetes" but option label is "Diabetes" in Q36? Let's check consistency. JSON Q37 has "Type 2 - Diabetes" as value and label. Q36 has "Diabetes". I will use the specific one for Q37 if this is primarily for Q37. 
    // Checking Q37 JSON: option label "Type 2 - Diabetes", value "Type 2 - Diabetes".
    // Checking Q36 JSON: option label "Diabetes", value "Diabetes".
    // The enum is likely used for both or driving Q37 flow primarily. Code uses it for Q37 flow.
    // I will use the Q37 label for now as that's the complex one.
    THYROID("Thyroid-related disorders", "Thyroid-related disorders (Hypothyroidism, Hyperthyroidism)"),
    LIVER("Liver Disorders", "Liver Disorders (e.g., fatty liver, hepatitis, cirrhosis)"),
    KIDNEY("Kidney Conditions", "Kidney Conditions (e.g., kidney stones, CKD, nephritis)"),
    CARDIOVASCULAR("Cardiovascular Conditions", "Cardiovascular Conditions (e.g., hypertension, high cholesterol, heart disease)"),
    PCOS("Polycystic Ovary Syndrome (PCOS)", "Polycystic Ovary Syndrome (PCOS)"),
    GALL_BLADDER("Gall bladder issues", "Gall bladder issues"),
    CANCER("Cancer", "Cancer"),
    RESPIRATORY("Respiratory conditions", "Respiratory conditions (e.g. asthma, COPD etc)"),
    AUTO_IMMUNE("Auto-immune condition", "Auto-immune condition (Lupus, celiac disease etc)"),
    NOT_SURE("I'm not sure", "I'm not sure"),
    NONE("None of the above", "None of the above")
}
