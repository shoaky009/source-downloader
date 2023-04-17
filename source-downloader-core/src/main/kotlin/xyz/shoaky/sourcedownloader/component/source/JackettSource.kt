package xyz.shoaky.sourcedownloader.component.source

import com.apptasticsoftware.rssreader.RssReader
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class JackettSource(
    private val url: String
) : Source {

    private val rssReader = RssReader()
    override fun fetch(): List<SourceItem> {
        return rssReader.read(url)
            .map {
                kotlin.runCatching {
                    val enclosure = it.enclosure.get()
                    SourceItem(it.title.get(),
                        URI(it.comments.get()),
                        parseTime(it.pubDate.get()),
                        enclosure.type,
                        URI(enclosure.url)
                    )
                }
            }
            .filter { it.isSuccess }
            .map { it.getOrThrow() }
            .toList()
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        private fun parseTime(pubDateText: String): LocalDateTime {
            return try {
                LocalDateTime.parse(pubDateText, formatter)
            } catch (e: Exception) {
                throw RuntimeException("解析日期$pubDateText 失败")
            }
        }
    }
}

