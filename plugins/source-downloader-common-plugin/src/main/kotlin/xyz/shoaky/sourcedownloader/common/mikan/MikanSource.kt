package xyz.shoaky.sourcedownloader.common.mikan

import com.apptasticsoftware.rssreader.RssReader
import org.jsoup.Jsoup
import xyz.shoaky.sourcedownloader.common.mikan.MikanVariableProvider.Companion.log
import xyz.shoaky.sourcedownloader.common.rss.parseTime
import xyz.shoaky.sourcedownloader.sdk.PointedItem
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.SourceItemPointer
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.LocalDateTime
import java.util.function.Function
import kotlin.streams.asSequence

internal class MikanSource(
    private val url: String,
    private val allEpisode: Boolean = false,
    token: String? = null,
    private val rssReader: RssReader = defaultRssReader,
    private val extractor: MikanSubGroupItemExtractor = MikanSubGroupItemExtractor(
        rssReader, MikanSupport(token)
    )
) : Source<MikanPointer> {


    companion object {
        private val defaultRssReader: RssReader = RssReader()
    }

    override fun fetch(pointer: MikanPointer?, limit: Int): Iterable<PointedItem<MikanPointer>> {
        val sourceItems = rssReader.read(url)
            .map {
                val enclosure = it.enclosure.get()
                SourceItem(it.title.get(),
                    URI(it.link.get()),
                    parseTime(it.pubDate.get()),
                    enclosure.type,
                    URI(enclosure.url)
                )
            }

        if (allEpisode.not()) {
            return sourceItems.map {
                PointedItem(it, MikanPointer(it.date))
            }.toList()
        }

        return sourceItems
            .sorted(compareBy { it.date })
            .filter { pointer == null || it.date.isAfter(pointer.date) }
            .map {
                PointedItem(it, MikanPointer(it.date))
            }
            .asSequence()
            .flatMap { pi ->
                extractor.apply(pi.sourceItem).map { sourceItem ->
                    PointedItem(sourceItem, MikanPointer(sourceItem.date))
                }.sortedBy { it.sourceItem.date }
            }
            .take(limit)
            .asIterable()
    }
}

internal class MikanSubGroupItemExtractor(
    private val rssReader: RssReader,
    private val support: MikanSupport
) : Function<SourceItem, List<SourceItem>> {
    override fun apply(t: SourceItem): List<SourceItem> {
        val subGroupRss = support.getSubGroupRss(t.link.toString())
            ?: return emptyList()
        val read = rssReader.read(subGroupRss)
        return read.map {
            val enclosure = it.enclosure.get()
            SourceItem(it.title.get(),
                URI(it.link.get()),
                parseTime(it.pubDate.get()),
                enclosure.type,
                URI(enclosure.url)
            )
        }.toList()
    }

}

internal class MikanSupport(
    private val token: String?
) {

    fun getSubGroupRss(uri: String): String? {
        var url = Jsoup.newSession()
            .cookie(".AspNetCore.Identity.Application", token ?: "")
            .url(uri)
            .get().select(".mikan-rss")
            .firstOrNull()?.attr("href")

        if (url == null) {
            log.info("无法获取rss链接, uri:{}", uri)
            return null
        }

        if (url.startsWith("http").not()) {
            // 暂时不管反代的
            url = "https://mikanani.me$url"
        }
        return url
    }
}

internal data class MikanPointer(
    val date: LocalDateTime
) : SourceItemPointer