package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.*
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourcePointer
import io.github.shoaky.sourcedownloader.sdk.component.Source

class FanboxSource(
    private val fanboxClient: FanboxClient
) : Source<FanboxPointer> {

    override fun fetch(pointer: FanboxPointer, limit: Int): Iterable<PointedItem<CreatorPointer>> {
        val supporting = fanboxClient.execute(SupportingPostsRequest()).body().body

        // for (sup in supporting) {
        //     val creatorPointer = pointer.getOrDefault(sup.creatorId)
        //     val paginateRequest = CreatorPaginateRequest(sup.creatorId)
        //     val pages = fanboxClient.execute(paginateRequest).body().body
        // }
        return supporting.items.map {
            val item = it.toItem(fanboxClient.server)
            PointedItem(item, CreatorPointer(it.creatorId))
        }
    }

    private fun creatorPosts(pointer: CreatorPointer): List<PointedItem<CreatorPointer>> {
        val paginateRequest = CreatorPaginateRequest(pointer.creatorId)
        val pages = fanboxClient.execute(paginateRequest).body().body

        val startPage = pointer.page ?: pages.size
        val pageUri = pages[startPage - 1]
        val postsRequest = CreatorPostsRequest.fromUri(pageUri)

        val items = fanboxClient.execute(postsRequest).body().body.items
        val itemIndex = pointer.itemIndex ?: items.size

        return items.map {
            val sourceItem = it.toItem(fanboxClient.server)
            PointedItem(sourceItem, pointer.copy(page = startPage, itemIndex = itemIndex))
        }
    }

    override fun defaultPointer(): FanboxPointer {
        return FanboxPointer(supportingCount = 0)
    }

}

class PaginationExpander<T, SP : SourcePointer, IP : ItemPointer>(
    private val expanItems: List<T>,
    private val limit: Int,
    private val sourcePointer: SP,
    private val supplier: (T, SP) -> DDD<T, IP>,
) : Iterator<List<PointedItem<IP>>> {

    private var expanIndex = 0
    private var totalCounting = 0
    private var current: DDD<T, IP>? = null

    override fun hasNext(): Boolean {
        return expanIndex < expanItems.size || totalCounting < limit
    }

    override fun next(): List<PointedItem<IP>> {
        // val supporting = expanItems[expanIndex]
        // val ddd = supplier.invoke(supporting, sourcePointer)
        //
        // val fetch = ddd.fetch(supporting)
        // if (fetch.isEmpty()) {
        //     expanIndex++
        // }
        return emptyList()
    }
}

interface DDD<T, IP : ItemPointer> {

    fun fetch(pointer: IP): List<PointedItem<IP>>

    fun totalPage(): Int

}

class Fanbox(
    private val creatorId: String,
    private val startPage: Int,
    private val fanboxClient: FanboxClient
) : DDD<Supporting, CreatorPointer> {

    private val pages by lazy {
        fanboxClient.execute(
            CreatorPaginateRequest("")
        ).body().body
    }

    override fun fetch(pointer: CreatorPointer): List<PointedItem<CreatorPointer>> {
        val page = pointer.page
        val pages = fanboxClient.execute(
            CreatorPaginateRequest(creatorId)
        ).body().body
        val postsRequest = CreatorPostsRequest.fromUri(pages[page + 1])
        val items = fanboxClient.execute(postsRequest).body().body.items
        return items.mapIndexed { index, item ->
            val sourceItem = item.toItem(fanboxClient.server)
            PointedItem(sourceItem, pointer.copy(page = page, itemIndex = index))
        }
    }

    override fun totalPage(): Int {
        return pages.size
    }

}