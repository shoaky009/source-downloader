package io.github.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchResult(
    val id: Long,
    @param:JsonProperty("original_name")
    val originalName: String,
    val name: String
)