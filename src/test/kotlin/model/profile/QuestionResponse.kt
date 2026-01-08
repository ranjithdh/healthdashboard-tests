package model.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@kotlinx.serialization.Serializable
data class QuestionResponse(
    @SerialName("questions")
    val questions: List<Question>
)

@Serializable
data class Question(
    @SerialName("id")
    val id: Int,

    @SerialName("heading")
    val heading: String,

    @SerialName("input_type")
    val inputType: String,

    @SerialName("type")
    val type: String,

    @SerialName("sub_type")
    val subType: String,

    @SerialName("question")
    val question: String,

    @SerialName("question_desc")
    val questionDesc: String? = null,

    @SerialName("options")
    val options: List<Option> = emptyList(),

    @SerialName("data")
    val data: AnswerData? = null,

    @SerialName("is_required")
    val isRequired: Boolean = false,

    @SerialName("is_score")
    val isScore: Boolean = false,

    @SerialName("parent_key")
    val parentKey: Int? = null,

    @SerialName("allowedValues")
    val allowedValues: List<String>? = null,

    @SerialName("option_layout")
    val optionLayout: String? = null,

    @SerialName("is_none")
    val isNone: Boolean? = null,

    @SerialName("has_others")
    val hasOthers: Boolean? = null
)


@kotlinx.serialization.Serializable
data class Option(
    @SerialName("label")
    val label: String,

    @SerialName("value")
    val value: String,

    @SerialName("icon")
    val icon: String? = null,

    @SerialName("allowedValues")
    val allowedValues: List<String>? = null,

    @SerialName("is_gender")
    val isGender: Boolean? = null,

    @SerialName("allowedGender")
    val allowedGender: String? = null
)

@kotlinx.serialization.Serializable
data class AnswerData(
    @SerialName("value")
    val value: JsonElement? = null,

    @SerialName("others_text")
    val othersText: String? = null
)


