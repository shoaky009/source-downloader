package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.GeneralFileMover
import io.github.shoaky.sourcedownloader.component.HardlinkFileMover
import io.github.shoaky.sourcedownloader.component.downloader.MockDownloader
import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object SystemFileSourceSupplier : ComponentSupplier<SystemFileSource> {

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