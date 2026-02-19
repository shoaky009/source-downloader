package io.github.shoaky.sourcedownloader.common.fanbox

import io.github.shoaky.sourcedownloader.external.fanbox.*
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.Source
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.io.path.Path

/**
 * 获取对应SessionId用户的赞助者贴子并迭代
 */
class FanboxIntegration(
    private val client: FanboxClient,
    private val mode: String? = null
) : Source<FanboxPointer>, ItemFileResolver {

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

    override fun headers(sourceItem: SourceItem): Map<String, String> {
        return client.headers
    }

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val request = PostInfoRequest(sourceItem.link.path.split("/").last())
        val post = client.execute(request).body().body
        val media = post.body
        val sourceFiles = mutableListOf<SourceFile>()
        post.coverImageUrl?.apply {
            sourceFiles.add(
                SourceFile(
                    Path("cover_${post.id}.jpeg"),
                    mapOf("type" to "cover"),
                    this
                )
            )
        }

        val images = media.imagesOrdering().mapIndexed { _, image ->
            SourceFile(
                Path("${image.id}.${image.extension}"),
                mapOf(
                    "height" to image.height,
                    "width" to image.width,
                    "type" to "image"
                ),
                image.originalUrl
            )
        }
        sourceFiles.addAll(images)

        val files = media.filesOrdering()
        val nameCount: Map<String, Int> = files.groupingBy { it.name }.eachCount()
        val fanboxFiles = files.mapIndexed { idx, file ->
            val baseName = if ((nameCount[file.name] ?: 0) > 1) {
                "${file.name}_$idx"
            } else {
                file.name
            }
            SourceFile(
                Path("$baseName.${file.extension}"),
                mapOf(
                    "size" to file.size,
                    "type" to "file"
                ),
                file.url
            )
        }
        sourceFiles.addAll(fanboxFiles)

        val textBlock = media.joinTextBlock()
        if (textBlock.trim().isNotBlank()) {
            sourceFiles.add(
                SourceFile(
                    Path("text_${post.id}.txt"),
                    mapOf(
                        "type" to "text",
                    ),
                    data = textBlock.byteInputStream()
                )
            )
        }

        val htmls = media.urlEmbedsOrdering().mapNotNull { url ->
            url.html?.let {
                SourceFile(
                    Path("${url.id}.html"),
                    mapOf(
                        "type" to "html",
                    ),
                    data = it.byteInputStream()
                )
            }
        }
        sourceFiles.addAll(htmls)
        return sourceFiles
    }
}

private class CreatorPostsIterator(
    private var creatorPointer: CreatorPointer,
    private val client: FanboxClient,
) : Iterator<List<PointedItem<ItemPointer>>> {

    private val touchBottom = creatorPointer.touchBottom
    private val lastMaxId = creatorPointer.topId ?: 0L
    private var finished = false
    private var posts: List<Post> = emptyList()

    override fun hasNext(): Boolean {
        if (finished) {
            log.debug("creatorPointer:{} finished", creatorPointer)
            return false
        }
        if (touchBottom.not()) {
            log.debug("creatorPointer:{} not touch bottom", creatorPointer)
            return true
        }
        val lastMaxId = creatorPointer.topId
        return posts.all { it.id != lastMaxId }
    }

    override fun next(): List<PointedItem<ItemPointer>> {
        val request = creatorPointer.nextRequest()

        posts = client.execute(request).body().body
        val lastPage = posts.isEmpty() || posts.size < request.limit
        val items = posts.filter { it.isRestricted.not() }
            .map {
                val update = creatorPointer.update(it, lastPage)
                creatorPointer = update
                PointedItem(toItem(client.server, it), update) to it
            }

        return if (touchBottom) {
            val res = items.filter { (_, post) -> post.id > lastMaxId }
                .map { it.first }
            finished = res.isEmpty()
            log.debug("creatorPointer:{} touch bottom next items:{}", creatorPointer, res)
            res
        } else {
            val res = items.map { (item, _) -> item }
            log.debug("creatorPointer:{} next items:{}", creatorPointer, res)
            res
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(CreatorPostsIterator::class.java)
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