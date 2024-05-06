package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI
import java.time.ZonedDateTime

data class Post(
    val attributes: PostAttrs,
    val id: Long,
    val type: String,
    val relationships: Map<String, JsonNode>,
) {

    fun mediaIds(): List<Long> {
        // audio
        // attachments
        // images
        return relationships["media"]?.get("data")?.map {
            it.get("id").asLong()
        } ?: emptyList()
    }
}

data class PostAttrs(
    val content: String? = null,
    @JsonProperty("current_user_can_view")
    val currentUserCanView: Boolean,
    val embed: Any? = null,
    val image: Any? = null,
    @JsonProperty("is_paid")
    val isPaid: Boolean,
    @JsonProperty("meta_image_url")
    val metaImageUrl: URI? = null,
    @JsonProperty("patreon_url")
    val patreonUrl: URI,
    @JsonProperty("post_file")
    val postFile: Any? = null,
    @JsonProperty("post_type")
    val postType: String,
    @JsonProperty("published_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT")
    val publishedAt: ZonedDateTime,
    val thumbnail: Any? = null,
    val title: String,
    val url: URI,
    @JsonProperty("post_metadata")
    val postMetadata: JsonNode? = null
) {

    fun getImageOrder(): List<Long> {
        val node = postMetadata?.get("image_order") ?: emptyList()
        return Jackson.convert(node)
    }
}