package io.github.shoaky.sourcedownloader.external.pixiv

import java.net.URI
import java.time.OffsetDateTime

data class PixivUser(
    val userId: Long,
    val userName: String,
    val illusts: List<Illustration> = emptyList()
)

data class Illustration(
    val id: Long,
    val title: String,
    val illustType: Int,
    val tags: List<String> = emptyList(),
    val userId: Long,
    val userName: String,
    val pageCount: Int,
    val url: URI,
    val restrict: Int,
    val xRestrict: Int,
    val createDate: OffsetDateTime,
    val bookmarkData: Bookmark? = null,
    val isMasked: Boolean = false
)

data class Bookmark(
    val id: String,
)

data class IllustrationDetail(
    val id: Long,
    val title: String,
    val illustType: Int,
    val userId: Long,
    val userName: String,
    val pageCount: Int,
    val restrict: Int,
    val xRestrict: Int,
    val urls: Map<String, URI>
)