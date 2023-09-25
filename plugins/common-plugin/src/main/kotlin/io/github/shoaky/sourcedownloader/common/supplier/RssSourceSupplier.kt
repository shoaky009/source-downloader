package io.github.shoaky.sourcedownloader.common.supplier

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.shoaky.sourcedownloader.common.rss.RssSource
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.Source
import java.time.format.DateTimeFormatter
import java.util.*

object RssSourceSupplier : ComponentSupplier<RssSource> {

    override fun apply(props: Properties): RssSource {
        val config = props.parse<Config>()
        return RssSource(
            config.url,
            config.tags,
            config.attributes,
            config.dateFormat?.let {
                DateTimeFormatter.ofPattern(it, Locale.ENGLISH)
            } ?: DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("rss", Source::class)
        )
    }

    data class Config(
        val url: String,
        val tags: List<String> = emptyList(),
        val attributes: Map<String, String> = emptyMap(),
        @JsonAlias("date-format")
        val dateFormat: String? = null
    )

}