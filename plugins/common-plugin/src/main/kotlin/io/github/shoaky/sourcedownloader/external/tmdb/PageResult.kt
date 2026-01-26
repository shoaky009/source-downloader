package io.github.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty

data class PageResult<T>(
    val page: Int,
    val results: List<T>,
    @param:JsonProperty("total_pages")
    val totalPages: Int,
    @param:JsonProperty("total_results")
    val totalResults: Int,
)
