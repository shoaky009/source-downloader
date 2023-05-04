package xyz.shoaky.sourcedownloader.sdk

import com.google.common.hash.Hashing
import java.net.URI
import java.time.LocalDateTime

data class SourceItem(
    val title: String,
    val link: URI,
    val date: LocalDateTime,
    val contentType: String,
    val downloadUri: URI,
) {
    fun hashing(): String {
        return Hashing.sha256()
            .hashString("$title-$link-$contentType-$downloadUri", Charsets.UTF_8)
            .toString()
    }
}