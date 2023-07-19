package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.CreatorPaginateRequest
import io.github.shoaky.sourcedownloader.external.fanbox.CreatorPostsRequest
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.external.fanbox.SupportingRequest
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.LimitedExpander

class FanboxSource(
    private val fanboxClient: FanboxClient
) : Source<FanboxPointer> {

    override fun fetch(pointer: FanboxPointer, limit: Int): Iterable<PointedItem<CreatorPointer>> {
        val supporting = fanboxClient.execute(SupportingRequest()).body().body
        val fanboxPointer = FanboxPointer(supportingCount = supporting.size)
        val creatorPointers = fanboxPointer.creatorPointers

        val expander = LimitedExpander(supporting, limit) {
            creatorPosts(CreatorPointer(it.creatorId))
        }
        return expander.asSequence().flatten().asIterable()
    }

    private fun creatorPosts(pointer: CreatorPointer): List<PointedItem<CreatorPointer>> {
        val paginateRequest = CreatorPaginateRequest(pointer.creatorId)
        val pages = fanboxClient.execute(paginateRequest).body().body

        val startPage = pointer.page ?: pages.size
        val pageUri = pages[startPage - 1]
        val postsRequest = CreatorPostsRequest.fromUri(pageUri)

        val items = fanboxClient.execute(postsRequest).body().body.items

        return items.mapIndexed { idx, pt ->
            val sourceItem = pt.toItem(fanboxClient.server)
            PointedItem(sourceItem, pointer.copy(page = startPage, itemIndex = idx))
        }
    }

    override fun defaultPointer(): FanboxPointer {
        return FanboxPointer(supportingCount = 0)
    }

}