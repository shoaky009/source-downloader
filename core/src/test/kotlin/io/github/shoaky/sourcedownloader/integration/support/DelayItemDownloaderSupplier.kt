package io.github.shoaky.sourcedownloader.integration.support

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object DelayItemDownloaderSupplier : ComponentSupplier<DelayItemDownloader> {

    override fun apply(context: CoreContext, props: Properties): DelayItemDownloader {
        return DelayItemDownloader(
            props.get("download-path")
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("delay-item")
        )
    }
}