package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.*
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.NullPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI

/**
 * 获取对应SessionId用户的赞助者贴子并迭代
 */
class FanboxSource(
    private val client: FanboxClient,
    private val mode: String? = null
) : Source<FanboxPointer> {

    override fun fetch(pointer: FanboxPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        if (mode == "latestOnly") {
            return client.execute(SupportingPostsRequest(30)).body()
                .body.items.filter { it.isRestricted.not() }
                .map { PointedItem(toItem(client.server, it), NullPointer) }
        }
        val supportings = client.execute(SupportingRequest()).body().body
        val results = mutableListOf<PointedItem<ItemPointer>>()
        for (supporting in supportings) {
            val creatorId = supporting.creatorId
            val creatorPointer = pointer.creatorPointers[creatorId] ?: CreatorPointer(creatorId)
            val iterator = CreatorPostsIterator(creatorPointer, client)
            for (pointedItems in iterator) {
                results.addAll(pointedItems)
                if (results.size >= limit) {
                    break
                }
            }
        }
        return results
    }

    override fun defaultPointer(): FanboxPointer {
        return FanboxPointer()
    }

    override fun headers(): Map<String, String> {
        return client.basicHeaders
    }
}

private class CreatorPostsIterator(
    private val creatorPointer: CreatorPointer,
    private val client: FanboxClient,
) : Iterator<List<PointedItem<ItemPointer>>> {

    private val lastTimeStatus = creatorPointer.touchBottom
    private val lastMaxId = creatorPointer.topId ?: 0L
    private var finished = false
    private var posts: Posts = Posts()

    override fun hasNext(): Boolean {
        if (finished) {
            return false
        }
        if (creatorPointer.touchBottom.not()) {
            return true
        }
        val lastMaxId = creatorPointer.topId
        return posts.items.all { it.id != lastMaxId }
    }

    override fun next(): List<PointedItem<ItemPointer>> {
        val request = creatorPointer.nextRequest()

        posts = client.execute(request).body().body
        finished = posts.hasNext().not()
        val items = posts.items.filter { it.isRestricted.not() }
            .map {
                creatorPointer.update(it, finished)
                PointedItem(toItem(client.server, it), creatorPointer) to it
            }

        return if (lastTimeStatus) {
            items.filter {
                it.second.id > lastMaxId
            }.map { it.first }
        } else {
            items.map { it.first }
        }
    }

}

private fun toItem(server: URI, post: Post): SourceItem {
    val uri = server.resolve("posts/${post.id}")
    return SourceItem(
        post.title,
        uri,
        post.publishedDatetime,
        "fanbox",
        uri,
        mapOf<String, Any>(
            "likes" to post.likeCount,
            "comments" to post.commentCount,
            "adult" to post.hasAdultContent,
            "fee" to post.feeRequired,
            "postId" to post.id,
            "username" to post.user.name,
            "creatorId" to post.creatorId
        ),
        post.tags.toSet()
    )
}