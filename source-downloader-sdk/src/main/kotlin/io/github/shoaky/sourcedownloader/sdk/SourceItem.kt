package io.github.shoaky.sourcedownloader.sdk

import com.google.common.hash.Hashing
import java.net.URI
import java.time.LocalDateTime

data class SourceItem(
    val title: String,
    val link: URI,
    val date: LocalDateTime,
    val contentType: String,
    val downloadUri: URI,
    val attributes: Map<String, Any> = emptyMap(),
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
}