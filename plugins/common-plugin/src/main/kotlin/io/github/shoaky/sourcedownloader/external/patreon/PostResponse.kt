package io.github.shoaky.sourcedownloader.external.patreon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains
import java.net.URI

class PostResponse(
    val data: Post,
    // 真tm逆天接口什么类型都往一个字段里放
    val included: List<JsonNode> = emptyList(),
) {

    private val includedMapping = included.filter { it.contains("id") }.associateBy { "${it.get("type").asText()}_${it.get("id").asText()}" }

    fun postMedias(): List<Media> {
        val imageOrder = data.attributes.getImageOrder()
        val mediaIds = data.mediaIds().sortedBy {
            val index = imageOrder.indexOf(it)
            if (index > -1) index else Int.MAX_VALUE
        }
        return mediaIds.mapNotNull {
            val attributes = includedMapping["media_$it"]?.get("attributes") ?: return@mapNotNull null
            val downloadUrl = if (attributes.contains("download_url")) {
                attributes.required("download_url").asText()
            } else {
                attributes.required("image_urls").get("original").asText()
            }

            val filename = attributes.required("file_name").asText()
            val size = attributes.get("size_bytes").asLong()
            val mimetype = attributes.get("mimetype").textValue()
            val mediaType = attributes.get("media_type").textValue()
            val metadata = attributes.get("metadata")
            Media(it, URI(downloadUrl), filename, mediaType, mimetype, size, metadata)
        }
    }
}