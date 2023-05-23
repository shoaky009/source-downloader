package xyz.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchResult(
    val id: Long,
    @JsonProperty("original_name")
    val originalName: String,
    val name: String
)