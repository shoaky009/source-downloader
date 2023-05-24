package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.GeneralFileMover
import xyz.shoaky.sourcedownloader.component.HardlinkFileMover
import xyz.shoaky.sourcedownloader.component.downloader.MockDownloader
import xyz.shoaky.sourcedownloader.component.source.SystemFileSource
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object SystemFileSourceSupplier : SdComponentSupplier<SystemFileSource> {
    override fun apply(props: Properties): SystemFileSource {
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

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(SystemFileSource::class),
            ComponentRule.allowDownloader(SystemFileSource::class),
            ComponentRule.allowDownloader(MockDownloader::class),
            ComponentRule.allowDownloader(HardlinkFileMover::class),
            ComponentRule.allowMover(GeneralFileMover::class),
        )
    }
}