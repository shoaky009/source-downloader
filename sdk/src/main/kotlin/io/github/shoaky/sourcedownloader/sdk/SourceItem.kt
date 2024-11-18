package io.github.shoaky.sourcedownloader.sdk

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.hash.Hashing
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import java.net.URI
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class SourceItem @JvmOverloads constructor(
    val title: String,
    val link: URI,
    val datetime: OffsetDateTime,
    val contentType: String,
    /**
     * 该字段在某些场景下有些歧义, 例如:实际Item是会解析成多个HTTP的时候
     */
    val downloadUri: URI,
    val attrs: Map<String, Any> = emptyMap(),
    val tags: Set<String> = emptySet(),
) {

    constructor(
        title: String,
        link: String,
        datetime: OffsetDateTime,
        contentType: String,
        downloadUri: String,
    ) : this(title, URI(link), datetime, contentType, URI(downloadUri))

    constructor(
        title: String,
        link: String,
        datetime: LocalDateTime,
        contentType: String,
        downloadUri: String,
    ) : this(title, URI(link), datetime.atOffset(DEFAULT_OFFSET), contentType, URI(downloadUri))

    constructor(
        title: String,
        link: URI,
        datetime: LocalDateTime,
        contentType: String,
        /**
         * 该字段在某些场景下有些歧义, 例如:实际Item是会解析成多个HTTP的时候
         */
        downloadUri: URI,
        attrs: Map<String, Any> = emptyMap(),
        tags: Set<String> = emptySet(),
    ) : this(title, link, datetime.atOffset(DEFAULT_OFFSET), contentType, downloadUri, attrs, tags)

    fun hashing(): String {
        return Hashing.murmur3_128()
            .hashString("$title-$link-$contentType-$downloadUri", Charsets.UTF_8)
            .toString()
    }

    inline fun <reified T> getAttr(key: String): T? {
        return attrs[key]?.let {
            Jackson.convert(it, jacksonTypeRef())
        }
    }

    inline fun <reified T> requireAttr(key: String): T {
        return getAttr(key) ?: throw IllegalArgumentException("Attr $key not found")
    }

    companion object {

        val DEFAULT_OFFSET: ZoneOffset = OffsetDateTime.now().offset
    }
}