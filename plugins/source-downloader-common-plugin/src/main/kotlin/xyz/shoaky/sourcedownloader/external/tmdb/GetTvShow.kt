package xyz.shoaky.sourcedownloader.external.tmdb

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import xyz.shoaky.sourcedownloader.sdk.api.BaseRequest
import xyz.shoaky.sourcedownloader.sdk.api.HttpMethod

data class GetTvShow(
    @JsonIgnore
    val id: Long,
    val language: String = "ja-jp",
) : BaseRequest<TvShow>() {

    override val path: String = "/3/tv/$id"
    override val responseBodyType: TypeReference<TvShow> = jacksonTypeRef()
    override val httpMethod: HttpMethod = HttpMethod.GET
    override val mediaType: MediaType = MediaType.JSON_UTF_8

}