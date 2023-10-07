package io.github.shoaky.sourcedownloader.common.pixiv

import io.github.shoaky.sourcedownloader.external.pixiv.*
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.RequestResult
import io.github.shoaky.sourcedownloader.sdk.util.flatten
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.io.path.Path

class PixivIntegration(
    // 后面优化 不用填uid
    private val userId: Long,
    private val client: PixivClient,
    private val mode: String,
) : Source<PixivPointer>, ItemFileResolver {

    override fun fetch(pointer: PixivPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        if (mode == "bookmark") {
            val bookmarkIterator = BookmarkIterator(pointer, client, userId).flatten()
            return object : Iterable<PointedItem<ItemPointer>> {
                override fun iterator(): Iterator<PointedItem<ItemPointer>> {
                    return bookmarkIterator
                }
            }
        }

        val followings = getFollowings(pointer)
        return ExpandIterator<PixivUser, PointedItem<ItemPointer>>(followings, limit) { following ->
            val items = getFollowingIllustration(following, pointer)
                .map {
                    val sourceItem = createSourceItem(it)
                    PointedItem(sourceItem, IllustrationPointer(it.id, it.userId))
                }
            RequestResult(items, items.isEmpty())
        }.asIterable()
    }

    private fun getFollowingIllustration(following: PixivUser, pointer: PixivPointer): List<Illustration> {
        val lastIllustrationId = pointer.lastIllustrationRecord[following.userId] ?: 0L
        val exists = following.illusts.firstOrNull { it.id == lastIllustrationId }
        if (exists != null) {
            return following.illusts.filter { it.id > lastIllustrationId }
        }

        return client.execute(GetUserAllRequest(following.userId)).body().body
            .illusts.map { it.key }.sorted()
            .takeWhile { it > lastIllustrationId }
            .chunked(50)
            .flatMap {
                val request = GetIllustrationsRequest(following.userId, it)
                client.execute(request).body().body.values.toList()
            }
    }

    private fun getFollowings(pointer: PixivPointer): MutableList<PixivUser> {
        val followings: MutableList<PixivUser> = mutableListOf()
        var request = GetFollowingRequest(userId)
        val followingFetchLimit = 50
        while (true) {
            val response = client.execute(request).body()
            if (response.body.users.isEmpty()) {
                break
            }
            // 如果用户的最新作品id大于上次记录的id，那么就是有新作品了
            // 但如果Illustration中是返回人气做的话这里要改，目前是按新发布的返回
            val users = response.body.users
                .filter { user ->
                    val lastIllustrationId = pointer.lastIllustrationRecord[user.userId] ?: 0L
                    if (user.illusts.isEmpty()) {
                        return@filter false
                    }
                    user.illusts.maxOf { it.id } > lastIllustrationId
                }
            followings.addAll(users)
            request = request.nextRequest()
            if (followings.size >= followingFetchLimit) {
                break
            }
        }
        return followings
    }

    override fun defaultPointer(): PixivPointer {
        return PixivPointer()
    }

    override fun headers(): Map<String, String> {
        return client.basicHeaders
    }

    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val downloadUri = sourceItem.downloadUri
        val path = imagePathRegex.find(downloadUri.path)?.value
        if (path == null) {
            log.error("Can't find path in $downloadUri")
            return emptyList()
        }

        val illustrationId = sourceItem.requireAttr<Long>("illustrationId")
        val illustration = client.execute(GetIllustrationRequest(illustrationId)).body().body
        val originalUri = illustration.urls["original"]
        if (originalUri == null) {
            log.error("Can't find original url in $illustration")
            return emptyList()
        }

        val ext = originalUri.path.substringAfterLast('.')
        val pageCount = illustration.pageCount
        return (0..<pageCount).map { seq ->
            val uri = "${downloadUri.scheme}://${downloadUri.host}/img-original/$path$seq.$ext"
            SourceFile(Path("${illustrationId}_$seq.$ext"), fileUri = URI(uri))
        }
        // 目前不能确定文件扩展名，暂时改用请求api的方式
        // val ext = downloadUri.path.substringAfterLast('.')
        // val uris = (0..<pageCount).map { seq ->
        //     val uri = "${downloadUri.scheme}://${downloadUri.host}/img-original/$path$seq.$ext"
        //     SourceFile(Path("${illustrationId}_$seq.$ext"), fileUri = URI(uri))
        // }
    }

    companion object {

        private val imagePathRegex = Regex("img/\\d{4}/\\d{2}/\\d{2}/\\d{2}/\\d{2}/\\d{2}/\\d+_p")
        private val log = LoggerFactory.getLogger(PixivIntegration::class.java)
    }
}

private class BookmarkIterator(
    private val pointer: PixivPointer,
    private val client: PixivClient,
    userId: Long,
) : Iterator<List<PointedItem<ItemPointer>>> {

    private val touchBottom = pointer.touchBottom
    private val topBookmarkId = pointer.topBookmarkId
    private val lastBookmarkId = pointer.currentBookmarkId ?: Long.MAX_VALUE.toString()
    private var request = GetBookmarksRequest(userId, 0)
    private var finished = false
    private var response: BookmarkResponse = BookmarkResponse(emptyList(), 0)

    override fun hasNext(): Boolean {
        if (finished) {
            return false
        }
        if (touchBottom.not()) {
            return true
        }
        val lastMaxId = pointer.topBookmarkId
        return response.works.all { it.bookmarkData!!.id != lastMaxId }
    }

    override fun next(): List<PointedItem<ItemPointer>> {
        response = client.execute(request).body().body
        request = request.next()
        finished = response.works.isEmpty()
        if (!touchBottom) {
            pointer.touchBottom = (response.works.size < request.limit)
        }
        val items = response.works.filter { it.isMasked.not() }
            .map {
                val sourceItem = createSourceItem(it)
                PointedItem(sourceItem, BookmarkPointer(it.bookmarkData!!.id)) to it
            }

        // bookmarkData 不会为空偷懒没写独立的类型
        return if (touchBottom) {
            items.filter { (_, post) -> post.bookmarkData!!.id > topBookmarkId }.map { (item, _) -> item }
        } else {
            items.filter { (_, post) -> post.bookmarkData!!.id < lastBookmarkId }.map { (item, _) -> item }
        }
    }

}

private fun createSourceItem(illustration: Illustration): SourceItem {
    return SourceItem(
        illustration.title,
        URI("https://www.pixiv.net/artworks/${illustration.id}"),
        illustration.createDate,
        "illustration",
        illustration.url,
        mapOf(
            "userId" to illustration.userId,
            "username" to illustration.userName,
            "illustrationId" to illustration.id,
            "r18" to (illustration.xRestrict == 1)
        ),
        illustration.tags.toSet(),
    )
}