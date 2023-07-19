package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import java.net.URI

class CreatorPostsRequest(
    val creatorId: String,
    val maxPublishedDatetime: String? = null,
    val maxId: String? = null,
    val limit: Int = 10
) : FanboxRequest<Posts>() {

    override val path: String = "/post.listCreator"
    override val responseBodyType: TypeReference<FanboxResponse<Posts>> = jacksonTypeRef()

    companion object {

        fun fromUri(uri: URI): CreatorPostsRequest {
            val queryMap = uri.queryMap()
            return CreatorPostsRequest(
                queryMap["creatorId"] ?: error("creatorId is required"),
                queryMap["maxPublishedDatetime"],
                queryMap["maxId"],
                queryMap["limit"]?.toInt() ?: 10
            )
        }
    }
}