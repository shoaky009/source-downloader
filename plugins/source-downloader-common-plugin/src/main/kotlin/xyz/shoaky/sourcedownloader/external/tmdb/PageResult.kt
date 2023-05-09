package xyz.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class PageResult(
    val id: Long,
    val name: String,
    @JsonProperty("original_name")
    val originalName: String
)
