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
    val coverImageUrl: URI? = null
)

data class Media(
    val blocks: List<Block> = emptyList(),
    val images: List<Image> = emptyList(),
    val files: List<File> = emptyList(),
    val text: String? = null,
    val fileMap: Map<String, File> = emptyMap(),
    val imageMap: Map<String, Image> = emptyMap(),
) {

    fun imageWithOrder(): List<Image> {
        val res = images.toMutableList()
        blocks.filter { it.type == "image" }.forEach { block ->
            imageMap[block.imageId]?.let { image ->
                res.add(image)
            }
        }
        return res
    }

    fun fileWithOrder(): List<File> {
        val ig = files.toMutableList()
        blocks.filter { it.type == "file" }.forEach { block ->
            fileMap[block.fileId]?.let { file ->
                ig.add(file)
            }
        }
        return ig
    }
}

data class File(
    val id: String,
    val extension: String,
    val name: String,
    val size: Long,
    val url: URI,
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

data class Block(
    val type: String,
    val text: String? = null,
    val imageId: String? = null,
    val fileId: String? = null
)