package io.github.shoaky.sourcedownloader.external.bilibili

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest

class GetFavorites(
    @JsonProperty("media_id")
    val mediaId: Long,
    val pn: Int = 1,
    val ps: Int = 20,
    val type: Int = 0,
    val order: String = "mtime",
) : BaseRequest<BilibiliResponse<FavoritesResponse>>() {

    override val path: String = "/x/v3/fav/resource/list"
    override val responseBodyType: TypeReference<BilibiliResponse<FavoritesResponse>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null

}

data class FavoritesResponse(
    val medias: List<Media>,
    @JsonProperty("has_more")
    val hasMore: Boolean
)