package io.github.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest
import io.github.shoaky.sourcedownloader.sdk.api.HttpMethod

data class SearchTvShow(
    val query: String,
    val language: String = "ja-jp",
    val page: Int = 1,
    @JsonProperty("include_adult")
    val includeAdult: Boolean = true
) : BaseRequest<PageResult<SearchResult>>() {

    override val path: String = "/3/search/tv"
    override val responseBodyType: TypeReference<PageResult<SearchResult>> = jacksonTypeRef()
    override val httpMethod: String = HttpMethod.GET.name
    override val mediaType: MediaType = MediaType.JSON_UTF_8
}