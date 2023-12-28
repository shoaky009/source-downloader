package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.UrlDownloader
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.nio.file.Path

object UrlDownloaderSupplier : ComponentSupplier<UrlDownloader> {

    override fun apply(context: CoreContext, props: Properties): UrlDownloader {
        val path = props.get<Path>("download-path")
        return UrlDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("url"),
        )
    }

}