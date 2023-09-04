package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.sdk.util.JoinStringSerializer

class PostRequest(
    postId: Long,
    @JsonSerialize(using = JoinStringSerializer::class)
    val include: List<String> = mediaInclude,
) : PatreonRequest<PostResponse>() {

    override val path: String = "/api/posts/$postId"
    override val responseBodyType: TypeReference<PostResponse> = jacksonTypeRef()

    companion object {

        private val mediaInclude = listOf("media")
    }
}