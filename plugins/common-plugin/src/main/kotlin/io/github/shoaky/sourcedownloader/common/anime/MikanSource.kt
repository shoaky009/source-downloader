package io.github.shoaky.sourcedownloader.common.anime

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import io.github.shoaky.sourcedownloader.common.rss.defaultRssReader
import io.github.shoaky.sourcedownloader.common.rss.parseTime
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.ExpandIterator
import io.github.shoaky.sourcedownloader.sdk.util.IterationResult
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * 通过Mikan的RSS源获取资源
 */
class MikanSource(
    private val url: String,
    private val allEpisode: Boolean = false,
    private val rssReader: RssReader = defaultRssReader,
    private val mikanClient: MikanClient = MikanClient(null),
    /**
     * 该字段只是用于单元测试时候的控制，不要在正常使用时候设置为false
     */
    private val cleanPointer: Boolean = true,
) : Source<MikanPointer> {

    override fun fetch(pointer: MikanPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        if (cleanPointer) {
            pointer.cleanMonthly()
        }
        val mikananiZoneOffset = ZoneOffset.ofHours(8)
        val sourceItems = rssReader.read(url)
            .map {
                fromRssItem(it, mikananiZoneOffset)
            }
            .toList()

        if (allEpisode.not()) {
            return sourceItems.map {
                PointedItem(it, NullPointer)
            }.toList()
        }

        val latestItems = sourceItems
            .filter { it.datetime.isAfter(pointer.latest) }
            .sortedBy { it.datetime }

        // 解决新番更新和添加顺序不一致的问题
        if (latestItems.isEmpty()) {
            return sourceItems.map {
                PointedItem(it, NullPointer)
            }.toList()
        }

        return ExpandIterator<SourceItem, PointedItem<ItemPointer>>(latestItems, limit) { item ->
            val fansubRss = mikanClient.getEpisodePageInfo(item.link.toURL()).fansubRss
            if (fansubRss == null) {
                log.debug("FansubRss is null:{}", item)
                return@ExpandIterator IterationResult(emptyList(), true)
            }

            val fansubQuery = URI(fansubRss).queryMap()
            val bangumiId = fansubQuery["bangumiId"] ?: error("bangumiId is null")
            val subGroupId = fansubQuery["subgroupid"] ?: error("subgroupid is null")
            var fansubItems = rssReader.read(fansubRss)
                .map {
                    fromRssItem(it, mikananiZoneOffset)
                }
                .toList()
                .sortedBy { it.datetime }
            // 字幕组的RSS列表会比订阅的晚些更新
            if (fansubItems.contains(item).not()) {
                log.debug("Item不在RSS列表中:{}", item)
                fansubItems = fansubItems.toMutableList()
                fansubItems.add(item)
            }

            val result = fansubItems
                .map {
                    PointedItem(it, FansubPointer(bangumiId, subGroupId, it.datetime))
                }
                .filter {
                    val key = it.pointer.key()
                    val date = pointer.shows[key]
                    date == null || it.sourceItem.datetime.isAfter(date)
                }
            if (log.isDebugEnabled) {
                log.debug("Fetch fansub items:{}", result)
            }
            IterationResult(result, true)
        }.asIterable()
    }

    private companion object {

        private val log = LoggerFactory.getLogger(MikanSource::class.java)

        private fun fromRssItem(rssItem: Item, offset: ZoneOffset): SourceItem {
            val enclosure = rssItem.enclosure.get()
            return SourceItem(
                rssItem.title.get(),
                URI(rssItem.link.get()),
                parseTime(rssItem.pubDate.get()).atOffset(offset),
                enclosure.type,
                URI(enclosure.url)
            )
        }
    }

    override fun defaultPointer(): MikanPointer {
        return MikanPointer()
    }
}

data class MikanPointer(
    var latest: OffsetDateTime = OffsetDateTime.MIN,
    val shows: MutableMap<String, OffsetDateTime> = mutableMapOf(),
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer is FansubPointer) {
            shows[itemPointer.key()] = itemPointer.date
            latest = maxOf(latest, itemPointer.date)
        }
    }

    fun cleanMonthly() {
        val range = OffsetDateTime.now().minusMonths(1L)
            .rangeUntil(OffsetDateTime.MAX)
        shows.entries.removeIf {
            range.contains(it.value).not()
        }
    }
}

data class FansubPointer(
    val bangumiId: String,
    val subGroupId: String,
    val date: OffsetDateTime
) : ItemPointer {

    fun key(): String {
        return "$bangumiId-$subGroupId"
    }
}