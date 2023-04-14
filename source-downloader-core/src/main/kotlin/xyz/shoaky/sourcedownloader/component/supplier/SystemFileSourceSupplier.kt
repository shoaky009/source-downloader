package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.GeneralFileMover
import xyz.shoaky.sourcedownloader.component.SystemFileSource
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object SystemFileSourceSupplier : SdComponentSupplier<SystemFileSource> {
    override fun apply(props: ComponentProps): SystemFileSource {
        return SystemFileSource(props.get("path"),
            props.getOrDefault("mode", 0)
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("system-file"),
            ComponentType.downloader("system-file"),
        )
    }

    override fun getComponentClass(): Class<SystemFileSource> {
        return SystemFileSource::class.java
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(SystemFileSource::class),
            ComponentRule.allowDownloader(SystemFileSource::class),
            ComponentRule.allowMover(GeneralFileMover::class),
        )
    }
}