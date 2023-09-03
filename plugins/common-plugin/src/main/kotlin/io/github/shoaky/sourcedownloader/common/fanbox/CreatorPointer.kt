package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.CreatorPostsRequest
import io.github.shoaky.sourcedownloader.external.fanbox.Post
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import java.time.LocalDateTime

data class CreatorPointer(
    val creatorId: String,
    var nextMaxId: Long? = null,
    var nextMaxDate: LocalDateTime? = null,
    val topId: Long? = null,
    val topDate: LocalDateTime? = null,
    val touchBottom: Boolean = false
) : ItemPointer {

    init {
        if (touchBottom) {
            // 触底拉取完成了，这2个字段不从持久化中的数据读取
            // 需要拉取最新的数据
            nextMaxId = null
            nextMaxDate = null
        }
    }

    fun update(post: Post, isBottom: Boolean): CreatorPointer {
        val (topId, topDate) = if (post.id > (topId ?: 0L)) {
            post.id to post.publishedDatetime
        } else {
            topId to topDate
        }
        val touchBottom = if (touchBottom.not()) isBottom else touchBottom
        return CreatorPointer(creatorId, post.id, post.publishedDatetime, topId, topDate, touchBottom)
    }

    fun nextRequest(): CreatorPostsRequest {
        val date = nextMaxDate?.minusSeconds(1)?.toString()
        return CreatorPostsRequest(creatorId, date, nextMaxId)
    }
}