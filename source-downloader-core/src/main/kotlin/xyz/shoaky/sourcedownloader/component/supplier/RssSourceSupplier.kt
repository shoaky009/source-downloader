package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.RssSource
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.Source

object RssSourceSupplier : SdComponentSupplier<RssSource> {
    override fun apply(props: ComponentProps): RssSource {
        return RssSource(props.get("url"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("rss", Source::class)
        )
    }

    override fun getComponentClass(): Class<RssSource> {
        return RssSource::class.java
    }

}