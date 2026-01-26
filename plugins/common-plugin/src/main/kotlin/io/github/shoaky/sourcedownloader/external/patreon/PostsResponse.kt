package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.hash.Hashing
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI

data class PostsResponse(
    val data: List<Post> = emptyList(),
    // 真tm逆天接口什么类型都往一个字段里放
    val included: List<JsonNode> = emptyList(),
    val links: Links? = null,
    val meta: Meta,
) {

    @JsonIgnore
    fun getUser(): PatreonEntity<User>? {
        return included.filter { it.get("type").asText() == "user" }
            .map {
                Jackson.convert(it, jacksonTypeRef<PatreonEntity<User>>())
            }.firstOrNull()
    }
}

data class User(
    @param:JsonProperty("full_name")
    val fullName: String,
    @param:JsonProperty("first_name")
    val firstName: String
)

data class Media(
    val id: Long,
    val downloadUri: URI,
    val filename: String,
    val mediaType: String? = null,
    val mimetype: String? = null,
    val size: Long? = null,
    val metadata: JsonNode? = null,
) {

    fun resolveFilename(): String {
        if (!filename.startsWith("http")) {
            return filename
        }

        val segment = downloadUri.path.split("/").last()
        if (segment.contains('.')) {
            return segment
        }
        return Hashing.crc32().hashString(filename, Charsets.UTF_8).toString()
    }
}

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