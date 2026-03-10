package mobileView.home.gut.model

data class ParsedSection(
    val mainTitle: String?,
    val subSections: List<ParsedSubSection>?,
    val plainContent: String?
)

data class ParsedSubSection(
    val title: String?,
    val description: String?
)