package io.github.shoaky.sourcedownloader.sdk

import com.google.common.hash.Hashing
import java.net.URI
import java.time.LocalDateTime

data class SourceItem @JvmOverloads constructor(
    val title: String,
    val link: URI,
    val date: LocalDateTime,
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
        date: LocalDateTime,
        contentType: String,
        downloadUri: String,
    ) : this(title, URI(link), date, contentType, URI(downloadUri))

    fun hashing(): String {
        return Hashing.murmur3_128()
            .hashString("$title-$link-$contentType-$downloadUri", Charsets.UTF_8)
            .toString()
    }

    inline fun <reified T> getAttr(key: String): T? {
        return attrs[key] as? T
    }

    inline fun <reified T> requireAttr(key: String): T {
        return getAttr(key) ?: throw IllegalArgumentException("Attr $key not found")
    }
}