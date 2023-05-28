package xyz.shoaky.sourcedownloader.common.rss

import com.apptasticsoftware.rssreader.RssReader
import org.slf4j.LoggerFactory
import xyz.shoaky.sourcedownloader.sdk.AlwaysLatestSource
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RssSource(private val url: String) : AlwaysLatestSource() {

    private val rssReader = defaultRssReader
    override fun fetch(): Iterable<SourceItem> {
        return rssReader.read(url)
            .map {
                kotlin.runCatching {
                    val enclosure = it.enclosure.get()
                    SourceItem(it.title.get(),
                        URI(it.link.get()),
                        parseTime(it.pubDate.get()),
                        enclosure.type,
                        URI(enclosure.url))
                }.onFailure {
                    log.error("获取RssItem字段发生错误", it)
                }
            }
            .filter { it.isSuccess }
            .map { it.getOrThrow() }
            .toList()
    }

    companion object {

        private val log = LoggerFactory.getLogger(RssSource::class.java)
    }
}

private val dateTimePatterns = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
)

internal val defaultRssReader: RssReader = RssReader()

fun parseTime(pubDateText: String): LocalDateTime {

    return try {
        LocalDateTime.parse(pubDateText)
    } catch (e: Exception) {
        for (pattern in dateTimePatterns) {
            runCatching {
                return LocalDateTime.parse(pubDateText, pattern)
            }
        }
        throw RuntimeException("解析日期$pubDateText 失败")
    }
}

