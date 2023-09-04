package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.databind.JsonNode
import java.net.URI

data class PostsResponse(
    val data: List<Post> = emptyList(),
    // 真tm逆天接口什么类型都往一个字段里放
    val included: List<JsonNode> = emptyList(),
    val links: Links? = null,
    val meta: Meta,
)

data class Media(
    val id: Long,
    val downloadUri: URI,
    val filename: String,
    val mediaType: String? = null,
    val mimetype: String? = null,
    val size: Long? = null,
    val metadata: JsonNode? = null,
)

data class Meta(
    val pagination: Pagination,
)

data class Pagination(
    val cursors: Cursors,
    val total: Int,
)

data class Links(
    val next: URI? = null,
)

data class Cursors(
    val next: String? = null,
)