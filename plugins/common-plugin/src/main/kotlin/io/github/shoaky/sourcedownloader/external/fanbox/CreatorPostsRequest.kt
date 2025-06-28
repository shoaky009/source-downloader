package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import java.net.URI

class CreatorPostsRequest(
    val creatorId: String,
    val firstPublishedDatetime: String? = null,
    val firstId: Long? = null,
    val limit: Int = 25
) : FanboxRequest<List<Post>>() {

    override val path: String = "/post.listCreator"
    override val responseBodyType: TypeReference<FanboxResponse<List<Post>>> = jacksonTypeRef()

    companion object {

        fun fromUri(uri: URI): CreatorPostsRequest {
            val queryMap = uri.queryMap()
            return CreatorPostsRequest(
                queryMap["creatorId"] ?: error("creatorId is required"),
                queryMap["firstPublishedDatetime"],
                queryMap["firstId"]?.toLong(),
                queryMap["limit"]?.toInt() ?: 50
            )
        }
    }
}