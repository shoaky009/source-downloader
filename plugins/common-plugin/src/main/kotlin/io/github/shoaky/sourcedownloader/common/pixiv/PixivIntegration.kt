package io.github.shoaky.sourcedownloader.common.pixiv

import com.google.common.net.HttpHeaders
import io.github.shoaky.sourcedownloader.common.anime.pathSegments
import io.github.shoaky.sourcedownloader.external.pixiv.*
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.RequestResult
import io.github.shoaky.sourcedownloader.sdk.util.flatten
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

class PixivIntegration(
    private val userId: Long,
    private val client: PixivClient,
    /**
     * bookmark:获取收藏的
     * following:获取关注的作品
     */
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
            .let { resp ->
                val mangeIds =
                    resp.manga.fieldNames().asSequence().mapNotNull { it.toLongOrNull() }.toList()
                resp.illusts.keys + mangeIds
            }
            .sorted()
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
        val illustrationType = sourceItem.requireAttr<Int>("illustrationType")
        val illustrationId = sourceItem.requireAttr<Long>("illustrationId")
        val animation = 2
        if (illustrationType != animation) {
            val pages = client.execute(IllustrationPagesRequest(illustrationId)).body().body
            return pages.map {
                val uri = it.urls.getValue("original")
                SourceFile(Path(uri.pathSegments().last()), downloadUri = uri)
            }
        }

        val ugoira = client.execute(GetUgoiraMetaRequest(illustrationId)).body().body

        val basicRequestBuilder = HttpRequest.newBuilder(ugoira.originalSrc)
            .HEAD()
        headers()
            .forEach { (key, value) -> basicRequestBuilder.setHeader(key, value) }
        val headResponse = httpClient.send(basicRequestBuilder.build(), BodyHandlers.discarding())
        if (headResponse.statusCode() != HttpStatus.OK.value()) {
            throw ProcessingException.retryable(
                "Get ugoira meta failed, illustrationId: $illustrationId, statusCode: ${headResponse.statusCode()}"
            )
        }
        val contentLength = headResponse.headers().firstValue(HttpHeaders.CONTENT_LENGTH).getOrNull()?.toLong()
        if (contentLength == null) {
            log.error("Get ugoira meta failed, illustrationId: $illustrationId")
            return emptyList()
        }

        val bodyRequest = basicRequestBuilder.GET()
            .setHeader(HttpHeaders.CONTENT_RANGE, "bytes 0-${contentLength - 1}/$contentLength")
            .build()
        val response = httpClient.send(bodyRequest, BodyHandlers.ofInputStream())
        return listOf(
            SourceFile(
                Path(ugoira.originalSrc.pathSegments().last),
                mapOf("size" to contentLength),
                data = response.body(),
            )
        )
    }

    companion object {

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
            "r18" to (illustration.xRestrict == 1),
            "illustrationType" to illustration.illustType
        ),
        illustration.tags.toSet(),
    )
}