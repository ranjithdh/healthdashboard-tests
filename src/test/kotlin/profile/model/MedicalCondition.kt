package profile.model

/**
 * Enum representing medical conditions for question 37
 */
enum class MedicalCondition(val buttonName: String) {
    DERMATOLOGICAL("Dermatological Conditions"),
    BONE_OR_JOINT("Bone or Joint Conditions"),
    GASTROINTESTINAL("Gastrointestinal Conditions"),
    NEUROLOGICAL("Neurological Conditions"),
    DIABETES("Type 2 - Diabetes"),
    THYROID("Thyroid-related disorders"),
    LIVER("Liver Disorders"),
    KIDNEY("Kidney Conditions"),
    CARDIOVASCULAR("Cardiovascular Conditions"),
    PCOS("Polycystic Ovary Syndrome (PCOS)"),
    GALL_BLADDER("Gall bladder issues"),
    CANCER("Cancer"),
    RESPIRATORY("Respiratory conditions"),
    AUTO_IMMUNE("Auto-immune condition"),
    NOT_SURE("I'm not sure"),
    NONE("None of the above")
}
