package xyz.shoaky.sourcedownloader.sdk

import com.google.common.hash.Hashing
import java.net.URL
import java.time.LocalDateTime

//后续还需要添加
data class SourceItem(
    val title: String,
    val link: URL,
    val date: LocalDateTime,
    val contentType: String,
    val downloadUrl: URL
) {
    fun hashing(): String {
        return Hashing.sha256()
            .hashString("$title-$link-$contentType-$downloadUrl", Charsets.UTF_8)
            .toString()
    }
}