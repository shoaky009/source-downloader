package io.github.shoaky.sourcedownloader.external.pixiv

import com.fasterxml.jackson.annotation.JsonFormat
import java.net.URI
import java.time.LocalDateTime

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Tokyo")
    val createDate: LocalDateTime,
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