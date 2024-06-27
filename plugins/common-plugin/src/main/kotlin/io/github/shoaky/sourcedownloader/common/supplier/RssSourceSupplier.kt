package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.rss.RssConfig
import io.github.shoaky.sourcedownloader.common.rss.RssSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.time.format.DateTimeFormatter
import java.util.*

object RssSourceSupplier : ComponentSupplier<RssSource> {

    override fun apply(context: CoreContext, props: Properties): RssSource {
        val config = props.parse<RssConfig>()
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
            ComponentType.source("rss")
        )
    }

}