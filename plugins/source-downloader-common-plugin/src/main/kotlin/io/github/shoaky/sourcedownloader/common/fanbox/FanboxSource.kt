package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.external.fanbox.Posts
import io.github.shoaky.sourcedownloader.external.fanbox.SupportingPostsRequest
import io.github.shoaky.sourcedownloader.external.fanbox.SupportingRequest
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.NullPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.component.Source

class FanboxSource(
    private val client: FanboxClient,
    private val mode: Int = 1
) : Source<FanboxPointer> {

    override fun fetch(pointer: FanboxPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        if (mode == 1) {
            return client.execute(SupportingPostsRequest(50)).body()
                .body.items.filter { it.isRestricted.not() }
                .map { PointedItem(it.toItem(client.server), NullPointer) }
        }

        val supporting = client.execute(SupportingRequest()).body().body
        val results = mutableListOf<PointedItem<ItemPointer>>()
        for (sup in supporting) {
            val creatorId = sup.creatorId
            val creatorPointer = pointer.creatorPointers[creatorId] ?: CreatorPointer(creatorId)
            val creatorPostsIterator = CreatorPostsIterator(creatorPointer, client)
            for (pointedItems in creatorPostsIterator) {
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

}

private class CreatorPostsIterator(
    private val creatorPointer: CreatorPointer,
    private val fanboxClient: FanboxClient,
) : Iterator<List<PointedItem<ItemPointer>>> {

    private val lastTimeStatus = creatorPointer.touchBottom
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

        posts = fanboxClient.execute(request).body().body
        finished = posts.hasNext().not()

        val items = posts.items.filter { it.isRestricted.not() }
            .map {
                creatorPointer.update(it, finished)
                PointedItem(it.toItem(fanboxClient.server), creatorPointer) to it
            }

        return if (lastTimeStatus) {
            items.filter {
                val lastMaxId = creatorPointer.topId ?: 0L
                it.second.id > lastMaxId
            }.map { it.first }
        } else {
            items.map { it.first }
        }
    }

}