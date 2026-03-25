package model.flipboard

import kotlinx.serialization.Serializable

@Serializable
data class FlipBoardArticles(
    val flipboards: List<Flipboard>?=null,
    val page: Int?=null,
    val per_page: Int?=null,
    val total: Int?=null
)

@Serializable
data class Flipboard(
    val citations: List<Citation>?=null,
    val content: String?=null,
    val created_at: String?=null,
    val expires_at: String?=null,
    val id: Int?=null,
    val image: Image?=null,
    val is_shown: Boolean?=null,
    val keywords: List<String>?=null,
    val priority_score: Double?=null,
    val shown_at: String?=null,
    val source_type: String?=null,
    val sources: List<Source>?=null,
    val tag: String?=null,
    val title: String?=null,
    val user_id: Int?=null
)


@Serializable
data class Image(
    val alt: String?=null,
    val id: String?=null,
    val url: String?=null
)


@Serializable
data class Citation(
    val count: Int?=null,
    val sources: List<Source>?=null,
    val tag: String?=null
)


@Serializable
data class Source(
    val domain: String?=null,
    val favicon_url: String?=null,
    val url: String?=null
)