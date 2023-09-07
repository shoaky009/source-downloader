package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.external.patreon.PatreonClient
import io.github.shoaky.sourcedownloader.external.patreon.PledgeRequest
import io.github.shoaky.sourcedownloader.external.patreon.PostRequest
import io.github.shoaky.sourcedownloader.external.patreon.PostsRequest
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.OldestToLatestSource
import io.github.shoaky.sourcedownloader.sdk.component.RequestResult
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import org.slf4j.LoggerFactory
import kotlin.io.path.Path

class PatreonIntegration(
    private val client: PatreonClient
) : OldestToLatestSource<Long, PatreonPointer>(), ItemFileResolver {

    override fun targets(pointer: PatreonPointer): List<Long> {
        return client.execute(PledgeRequest()).body().campaignIds()
    }

    override fun requestItems(pointer: PatreonPointer, target: Long): RequestResult<PointedItem<ItemPointer>> {
        val cp = pointer.campaignPointers[target] ?: CampaignPointer(target)
        val lastCursor = cp.cursor
        val postsRequest = PostsRequest(target, cursor = lastCursor)
        val response = client.execute(postsRequest).body()

        val nextCursor = response.links?.next?.queryMap()?.get("page[cursor]")
        val items = response.data.filter { it.id > cp.lastPostId }.map { post ->
            val sourceItem = SourceItem(
                post.attributes.title,
                post.attributes.url,
                post.attributes.publishedAt.toLocalDateTime(),
                post.attributes.postType,
                post.attributes.url,
            )
            PointedItem(sourceItem, CampaignPointer(target, nextCursor, post.id))
        }
        return RequestResult(items, nextCursor == null)
    }

    override fun defaultPointer(): PatreonPointer {
        return PatreonPointer()
    }

    override fun headers(): Map<String, String> {
        return client.headers
    }

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val postId = lastNumberRegex.find(sourceItem.link.path)?.groupValues?.last()
        if (postId == null) {
            log.info("No postId found in item: {}", sourceItem)
            return emptyList()
        }

        // 可以在fetch的时候就把这些信息放入attrs中就不用再请求了
        val response = client.execute(PostRequest(postId.toLong())).body()
        val files = response.postMedias().map { media ->
            SourceFile(
                Path("${media.id}_${media.filename}"),
                buildMap {
                    media.mimetype?.let { put("mimetype", it) }
                    media.mediaType?.let { put("mediaType", it) }
                    media.size?.let { put("size", it) }
                },
                fileUri = media.downloadUri
            )
        }.toMutableList()

        val content = response.data.attributes.content
        if (content.isNotBlank()) {
            files.add(
                SourceFile(
                    Path("${response.data.id}_content.html"),
                    mapOf("originName" to "content.html"),
                    data = content.byteInputStream()
                ))
        }
        return files
    }

    companion object {

        private val lastNumberRegex = Regex("\\d+\$")
        private val log = LoggerFactory.getLogger(PatreonIntegration::class.java)
    }
}