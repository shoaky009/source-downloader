package xyz.shoaky.sourcedownloader.common.rss

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.Source

object RssSourceSupplier : SdComponentSupplier<RssSource> {
    override fun apply(props: Properties): RssSource {
        return RssSource(props.get("url"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("rss", Source::class)
        )
    }

}