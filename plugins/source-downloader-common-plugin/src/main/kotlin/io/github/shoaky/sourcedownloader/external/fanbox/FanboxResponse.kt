package io.github.shoaky.sourcedownloader.external.fanbox

import com.fasterxml.jackson.annotation.JsonFormat
import java.net.URI
import java.time.LocalDateTime

data class FanboxResponse<T>(
    val body: T,
)

data class Posts(
    val items: List<Post> = emptyList(),
    val nextUrl: URI? = null
) {

    fun hasNext(): Boolean {
        return nextUrl != null
    }

    fun nextRequest(): CreatorPostsRequest? {
        return nextUrl?.let { CreatorPostsRequest.fromUri(it) }
    }
}

data class User(
    val userId: String,
    val name: String,
)

data class Cover(
    private val type: String,
    private val url: URI,
)

data class Post(
    val id: Long,
    val title: String,
    val creatorId: String,
    val isRestricted: Boolean,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val user: User,
    val feeRequired: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Tokyo")
    val publishedDatetime: LocalDateTime,
    val hasAdultContent: Boolean,
    val tags: List<String> = emptyList(),
    val cover: Cover? = null
)

data class PostDetail(
    val id: String,
    val title: String,
    val creatorId: String,
    val isRestricted: Boolean,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val user: User,
    val feeRequired: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Tokyo")
    val publishedDatetime: LocalDateTime,
    val hasAdultContent: Boolean,
    val tags: List<String> = emptyList(),
    val cover: Cover? = null,
    val body: Media,
)

data class Media(
    val images: List<Image> = emptyList(),
    val text: String? = null
)

data class Image(
    val id: String,
    val extension: String,
    val height: Int,
    val width: Int,
    val originalUrl: URI,
    val thumbnailUrl: URI,
)

data class Supporting(
    val id: String,
    val title: String,
    val fee: Int,
    val creatorId: String,
    val user: User,
    val hasAdultContent: Boolean
)