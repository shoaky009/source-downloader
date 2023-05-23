package xyz.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class PageResult(
    val id: Long,
    val name: String,
    @JsonProperty("original_name")
    val originalName: String
)

data class PageResultV2<T>(
    val page: Int,
    val results: List<T>,
    @JsonProperty("total_pages")
    val totalPages: Int,
    @JsonProperty("total_results")
    val totalResults: Int,
)
