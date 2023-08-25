package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.CreatorPostsRequest
import io.github.shoaky.sourcedownloader.external.fanbox.Post
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import java.time.LocalDateTime

data class CreatorPointer(
    val creatorId: String,
    var nextMaxId: Long? = null,
    var nextMaxDate: LocalDateTime? = null,
    var topId: Long? = null,
    var topDate: LocalDateTime? = null,
    var touchBottom: Boolean = false
) : ItemPointer {

    init {
        if (touchBottom) {
            // 触底拉取完成了，这2个字段不从持久化中的数据读取
            // 需要拉取最新的数据
            nextMaxId = null
            nextMaxDate = null
        }
    }

    fun update(post: Post, isBottom: Boolean) {
        nextMaxId = post.id
        nextMaxDate = post.publishedDatetime
        if (touchBottom.not()) {
            touchBottom = isBottom
        }
        if (post.id > (topId ?: 0L)) {
            topId = post.id
            topDate = post.publishedDatetime
        }
    }

    fun nextRequest(): CreatorPostsRequest {
        val date = nextMaxDate?.minusSeconds(1)?.toString()
        return CreatorPostsRequest(creatorId, date, nextMaxId)
    }
}