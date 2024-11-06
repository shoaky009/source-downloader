package io.github.shoaky.sourcedownloader.common.rss

import com.apptasticsoftware.rssreader.Enclosure
import com.apptasticsoftware.rssreader.RssReader
import com.google.common.net.UrlEscapers
import io.github.shoaky.sourcedownloader.external.rss.RssExtReader
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.AlwaysLatestSource
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.util.http.httpClient
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.jvm.optionals.getOrDefault

/**
 * 通过RSS源获取资源，支持自定义属性映射
 */
class RssSource(
    private val url: String,
    private val tags: List<String> = emptyList(),
    private val attributes: Map<String, String> = emptyMap(),
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : AlwaysLatestSource() {

    private val rssReader = RssExtReader()

    init {
        tags.forEach {
            rssReader.addItemExtension(it) { i, s ->
                i.tags.add(s)
            }
        }

        attributes.forEach {
            rssReader.addItemExtension(it.value) { i, s ->
                i.attrs[it.key] = s
            }
        }
    }

    override fun fetch(): Iterable<SourceItem> {
        return rssReader.read(url)
            .map { item ->
                val datetime = item.pubDate.map {
                    LocalDateTime.parse(it, dateFormat).atZone(zoneId)
                }.getOrDefault(ZonedDateTime.now())
                val enclosure = item.enclosure.getOrDefault(defaultEnclosure)
                SourceItem(
                    item.title.orElseThrow {
                        ComponentException.processing("$url title is null")
                    },
                    URI(item.link.orElseThrow {
                        ComponentException.processing("$url link is null")
                    }),
                    datetime,
                    enclosure.type,
                    URI(
                        UrlEscapers.urlFragmentEscaper().escape(
                            enclosure.url.ifBlank {
                                item.link.get()
                            })
                    ),
                    item.attrs,
                    item.tags
                )
            }
            .toList()
    }

    override fun group(): String? {
        return URI(url).host
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
        throw ComponentException.processing("解析日期$pubDateText 失败")
    }
}

