package io.github.shoaky.sourcedownloader.common.rss

import com.apptasticsoftware.rssreader.RssReader
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AlwaysLatestSource
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 通过Jackett的RSS源获取资源
 */
class JackettSource(
    private val url: String,
    private val rssReader: RssReader = defaultRssReader
) : AlwaysLatestSource() {

    override fun fetch(): Iterable<SourceItem> {
        return rssReader.read(url)
            .map {
                kotlin.runCatching {
                    val enclosure = it.enclosure.get()
                    SourceItem(
                        it.title.get(),
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
                throw ComponentException.processing("解析日期$pubDateText 失败")
            }
        }
    }

}

