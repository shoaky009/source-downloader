package io.github.shoaky.sourcedownloader.common.anime

import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import io.github.shoaky.sourcedownloader.common.rss.defaultRssReader
import io.github.shoaky.sourcedownloader.common.rss.parseTime
import io.github.shoaky.sourcedownloader.sdk.PointedItem
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer
import io.github.shoaky.sourcedownloader.sdk.component.Source
import io.github.shoaky.sourcedownloader.sdk.util.LimitedExpander
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDateTime

internal class MikanSource(
    private val url: String,
    private val allEpisode: Boolean = false,
    private val rssReader: RssReader = defaultRssReader,
    private val mikanSupport: MikanSupport = MikanSupport(null)
) : Source<MikanPointer> {

    override fun fetch(pointer: MikanPointer?, limit: Int): Iterable<PointedItem<MikanPointer>> {
        val sourceItems = rssReader.read(url)
            .map {
                fromRssItem(it)
            }.toList()

        if (allEpisode.not()) {
            return sourceItems.map {
                PointedItem(it, MikanPointer(it.date))
            }.toList()
        }

        val items = sourceItems
            .sortedBy { it.date }
            .filter { pointer == null || it.date.isAfter(pointer.date) }
            .map {
                PointedItem(it, MikanPointer(it.date))
            }

        val expander = LimitedExpander(items, limit) { pi ->
            val fansubRss = mikanSupport.getEpisodePageInfo(pi.sourceItem.link.toURL()).fansubRss
                ?: return@LimitedExpander emptyList()

            var pointedItems = rssReader.read(fansubRss)
                .map {
                    // TODO 改进指针，包括列表内的也能过滤
                    PointedItem(fromRssItem(it), MikanPointer(pi.sourceItem.date))
                }.toList()
                .sortedBy { it.sourceItem.date }
            if (pointedItems.contains(pi).not()) {
                log.debug("Item不在RSS列表中:{}", pi)
                pointedItems = pointedItems.toMutableList()
                pointedItems.add(pi)
            }
            pointedItems
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
}

data class MikanPointer(
    val date: LocalDateTime? = null
) : SourceItemPointer {

}