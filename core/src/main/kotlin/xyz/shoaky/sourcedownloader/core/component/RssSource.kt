package xyz.shoaky.sourcedownloader.core.component

import com.apptasticsoftware.rssreader.RssReader
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.Source
import java.net.URL

class RssSource(private val url: String) : Source {

    private val rssReader = RssReader()
    override fun fetch(): List<SourceItem> {
        return rssReader.read(url)
            .map {
                kotlin.runCatching {
                    val enclosure = it.enclosure.get()
                    SourceItem(it.title.get(), URL(it.link.get()), enclosure.type, URL(enclosure.url))
                }.onFailure {
                    log.error("获取RssItem字段发生错误", it)
                }
            }
            .filter { it.isSuccess }
            .map { it.getOrThrow() }
            .toList()
    }

}

object RssSourceSupplier : ComponentSupplier<RssSource> {
    override fun apply(props: ComponentProps): RssSource {
        val url = props.properties["url"]?.toString() ?: throw RuntimeException("url不能为空")
        return RssSource(url)
    }

    override fun availableTypes(): List<ComponentType> {
        return listOf(
            ComponentType("rss", Source::class)
        )
    }

}