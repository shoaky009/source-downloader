package io.github.shoaky.sourcedownloader.common.rss

import com.apptasticsoftware.rssreader.Enclosure
import com.apptasticsoftware.rssreader.RssReader
import io.github.shoaky.sourcedownloader.external.rss.RssExtReader
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AlwaysLatestSource
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.jvm.optionals.getOrDefault

class RssSource(
    private val url: String,
    private val tags: List<String> = emptyList(),
    private val attributes: Map<String, String> = emptyMap(),
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
) : AlwaysLatestSource() {

    private val rssReader = RssExtReader()

    init {
        tags.forEach {
            rssReader.addItemExtension(it) { i, s ->
                i.tags.add(s)
            }
        }

        attributes.forEach {
            rssReader.addItemExtension(it.key) { i, s ->
                i.attrs[it.value] = s
            }
        }
    }

    override fun fetch(): Iterable<SourceItem> {
        return rssReader.read(url)
            .map { item ->
                val enclosure = item.enclosure.getOrDefault(defaultEnclosure)
                SourceItem(
                    item.title.orElseThrow {
                        IllegalArgumentException("$url title is null")
                    },
                    URI(item.link.orElseThrow {
                        IllegalArgumentException("$url link is null")
                    }),
                    item.pubDate.map {
                        LocalDateTime.parse(it, dateFormat)
                    }.getOrDefault(LocalDateTime.now()),
                    enclosure.type,
                    URI(enclosure.url.ifBlank {
                        item.link.get()
                    }),
                    item.attrs,
                    item.tags
                )
            }
            .toList()
    }

    companion object {

        private val defaultEnclosure = Enclosure().apply {
            this.type = ""
            this.url = ""
        }
    }
}

private val dateTimePatterns = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
)

internal val defaultRssReader: RssReader = RssReader(httpClient)

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

