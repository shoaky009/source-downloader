package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.net.MediaType
import io.github.shoaky.sourcedownloader.sdk.api.BaseRequest

class GetBookmarksRequest(
    @JsonIgnore
    val userId: Long,
    val offset: Int = 0,
    val limit: Int = 50,
    val rest: String = "show",
    val lang: String = "zh"
) : BaseRequest<PixivResponse<BookmarkResponse>>() {

    override val path: String = "/ajax/user/$userId/illusts/bookmarks?tag="
    override val responseBodyType: TypeReference<PixivResponse<BookmarkResponse>> = jacksonTypeRef()
    override val httpMethod: String = "GET"
    override val mediaType: MediaType? = null

    fun next(): GetBookmarksRequest {
        return GetBookmarksRequest(userId, offset + limit, limit, rest)
    }
}

data class BookmarkResponse(
    val works: List<Illustration> = emptyList(),
    val total: Int,
)