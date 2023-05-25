package xyz.shoaky.sourcedownloader.external.anilist

import com.fasterxml.jackson.annotation.JsonProperty

data class Media(
    val id: Long,
    val title: Title,
)

data class Title(
    val romaji: String? = null,
    val native: String? = null,
)

data class Page(
    @JsonProperty("media")
    val medias: List<Media> = emptyList()
)

data class PageResponse(
    @JsonProperty("Page")
    val page: Page,
)

data class GraphQLResponse<T>(
    val data: T,
    val errors: List<Map<String, Any>> = emptyList(),
)