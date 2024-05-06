package io.github.shoaky.sourcedownloader.common.patreon

import io.github.shoaky.sourcedownloader.external.patreon.*
import io.github.shoaky.sourcedownloader.sdk.ItemPointer
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ExpandSource
import io.github.shoaky.sourcedownloader.sdk.component.FetchContext
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import io.github.shoaky.sourcedownloader.sdk.util.IterationResult
import org.slf4j.LoggerFactory
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path


class PatreonIntegration(
    private val client: PatreonClient
) : ExpandSource<Long, PatreonPointer>(), ItemFileResolver {

    override fun targets(pointer: PatreonPointer): List<Long> {
        return client.execute(PledgeRequest()).body().campaignIds()
    }

    /**
     * TODO 当前迭代的实现会多请求一次最后一个item没必要
     */
    override fun requestItems(
        ctx: FetchContext<PatreonPointer>,
        target: Long
    ): IterationResult<PointedItem<ItemPointer>> {
        val yearMonthsKey = "$target-YearMonths"
        val yearMonths = ctx.loadAttr(yearMonthsKey) {
            val postTags = client.execute(PostTagRequest(target)).body()
            postTags.data.filter { it.attributes.tagType == "year_month" }
                .map { YearMonth.parse(it.attributes.value) }.sorted()
        }

        val pointer = ctx.pointer.campaignPointers[target] ?: CampaignPointer(target)
        val requestYearMonth = findNextYearMonth(pointer.lastYearMonth, yearMonths, pointer.lastOfMonth)
        val postsRequest = PostsRequest(target, month = requestYearMonth)
        val response = client.execute(postsRequest).body()

        // 偷懒没有处理一个月超过20条的, api太逆天了没心情写
        val fullName = response.getUser()?.attributes?.fullName
        val posts = response.data.filter { it.id > pointer.lastPostId }
        val items = posts.mapIndexed { index, post ->
            val sourceItem = SourceItem(
                post.attributes.title,
                post.attributes.url,
                post.attributes.publishedAt.toLocalDateTime(),
                post.attributes.postType,
                post.attributes.url,
                buildMap {
                    put("campaignId", target)
                    put("postId", post.id)
                    fullName?.let {
                        put("username", it)
                    }
                }
            )

            PointedItem(
                sourceItem,
                CampaignPointer(target, requestYearMonth, posts.lastIndex == index, post.id)
            )
        }

        val terminated = yearMonths.max() == requestYearMonth
        if (terminated) {
            ctx.attrs.remove(yearMonthsKey)
        }
        return IterationResult(items, terminated)
    }

    private fun findNextYearMonth(
        doneYearMonth: YearMonth?,
        yearMonths: List<YearMonth>,
        lastOfMonth: Boolean
    ): YearMonth {
        if (doneYearMonth == null) {
            return yearMonths.first()
        }
        val max = yearMonths.max()
        if (max == doneYearMonth) {
            return max
        }

        val nextMonth = yearMonths.maxBy {
            val until = it.until(doneYearMonth, ChronoUnit.MONTHS)
            if (until >= 0L) Long.MIN_VALUE else until
        }
        return if (lastOfMonth) nextMonth else nextMonth.minusMonths(1)
    }

    override fun defaultPointer(): PatreonPointer {
        return PatreonPointer()
    }

    override fun headers(sourceItem: SourceItem): Map<String, String> {
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
            val filename = media.resolveFilename()
            SourceFile(
                Path("${media.id}_${filename}"),
                buildMap {
                    put("mediaId", media.id)
                    put("filename", filename)
                    media.mimetype?.let { put("mimetype", it) }
                    media.mediaType?.let { put("mediaType", it) }
                    media.size?.let { put("size", it) }
                },
                downloadUri = media.downloadUri
            )
        }.toMutableList()

        val content = response.data.attributes.content
        if (!content.isNullOrBlank()) {
            files.add(
                SourceFile(
                    Path("${response.data.id}_content.html"),
                    mapOf("mimetype" to "text"),
                    data = content.byteInputStream()
                )
            )
        }
        return files
    }

    companion object {

        private val lastNumberRegex = Regex("\\d+\$")
        private val log = LoggerFactory.getLogger(PatreonIntegration::class.java)
    }
}