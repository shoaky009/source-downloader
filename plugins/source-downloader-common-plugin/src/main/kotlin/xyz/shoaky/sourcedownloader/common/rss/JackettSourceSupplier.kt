package xyz.shoaky.sourcedownloader.common.rss

import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.Source

object JackettSourceSupplier : SdComponentSupplier<JackettSource> {
    override fun apply(props: ComponentProps): JackettSource {
        return JackettSource(props.get("url"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType("jackett", Source::class)
        )
    }

}