package io.github.shoaky.sourcedownloader.common.anime

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import io.github.shoaky.sourcedownloader.common.rss.defaultRssReader
import io.github.shoaky.sourcedownloader.common.rss.parseTime
import io.github.shoaky.sourcedownloader.sdk.*
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.LimitedExpander
import io.github.shoaky.sourcedownloader.sdk.util.queryMap
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime

/**
 * 通过Mikan的RSS源获取资源
 */
class MikanSource(
    private val url: String,
    private val allEpisode: Boolean = false,
    private val rssReader: RssReader = defaultRssReader,
    private val mikanSupport: MikanSupport = MikanSupport(null),
    /**
     * 该字段只是用于单元测试时候的控制，不要在正常使用时候设置为false
     */
    private val cleanPointer: Boolean = true,
) : Source<MikanPointer> {

    override fun fetch(pointer: MikanPointer, limit: Int): Iterable<PointedItem<ItemPointer>> {
        if (cleanPointer) {
            pointer.cleanMonthly()
        }
        val sourceItems = rssReader.read(url)
            .map {
                fromRssItem(it)
            }
            .toList()

        if (allEpisode.not()) {
            return sourceItems.map {
                PointedItem(it, NullPointer)
            }.toList()
        }

        val items = sourceItems
            .filter { it.date.isAfter(pointer.latest) }
            .sortedBy { it.date }

        val expander = LimitedExpander(items, limit) { item ->
            val fansubRss = mikanSupport.getEpisodePageInfo(item.link.toURL()).fansubRss
            if (fansubRss == null) {
                log.debug("FansubRss is null:{}", item)
                return@LimitedExpander emptyList()
            }

            val fansubQuery = URI(fansubRss).queryMap()
            val bangumiId = fansubQuery["bangumiId"] ?: error("bangumiId is null")
            val subGroupId = fansubQuery["subgroupid"] ?: error("subgroupid is null")
            var pointedItems = rssReader.read(fansubRss)
                .map {
                    fromRssItem(it)
                }
                .toList()
                .sortedBy { it.date }
            if (pointedItems.contains(item).not()) {
                log.debug("Item不在RSS列表中:{}", item)
                pointedItems = pointedItems.toMutableList()
                pointedItems.add(item)
            }

            pointedItems
                .map {
                    PointedItem(
                        it,
                        FansubPointer(bangumiId, subGroupId, it.date)
                    )
                }
                .filter {
                    val key = it.pointer.key()
                    val date = pointer.shows[key]
                    date == null || it.sourceItem.date.isAfter(date)
                }
        }

        log.debug("Fetching mikan source pointer is:{}", pointer)
        return expander.asSequence().flatten().asIterable()
    }

    private companion object {

        private val log = LoggerFactory.getLogger(MikanSource::class.java)

        private fun fromRssItem(rssItem: Item): SourceItem {
            val enclosure = rssItem.enclosure.get()
            return SourceItem(
                rssItem.title.get(),
                URI(rssItem.link.get()),
                parseTime(rssItem.pubDate.get()),
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
    var latest: LocalDateTime = LocalDateTime.MIN,
    val shows: MutableMap<String, LocalDateTime> = mutableMapOf(),
) : SourcePointer {

    override fun update(itemPointer: ItemPointer) {
        if (itemPointer is FansubPointer) {
            shows[itemPointer.key()] = itemPointer.date
            latest = maxOf(latest, itemPointer.date)
        }
    }

    fun cleanMonthly() {
        val range = LocalDateTime.now().minusMonths(1L).rangeUntil(LocalDateTime.MAX)
        shows.entries.removeIf {
            range.contains(it.value).not()
        }
    }
}

data class FansubPointer(
    val bangumiId: String,
    val subGroupId: String,
    val date: LocalDateTime
) : ItemPointer {

    fun key(): String {
        return "$bangumiId-$subGroupId"
    }
}