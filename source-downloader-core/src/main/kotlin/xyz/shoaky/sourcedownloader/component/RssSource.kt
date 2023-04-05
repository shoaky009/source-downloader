package xyz.shoaky.sourcedownloader.component

import com.apptasticsoftware.rssreader.RssReader
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RssSource(private val url: String) : Source {

    private val rssReader = RssReader()
    override fun fetch(): List<SourceItem> {
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
        private val patterns = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        private fun parseTime(pubDateText: String): LocalDateTime {

            return try {
                LocalDateTime.parse(pubDateText)
            } catch (e: Exception) {
                for (pattern in patterns) {
                    return LocalDateTime.parse(pubDateText, pattern)
                }
                throw RuntimeException("解析日期$pubDateText 失败")
            }
        }
    }
}

