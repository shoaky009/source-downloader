package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.HttpDownloader
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.nio.file.Path

object HttpDownloaderSupplier : ComponentSupplier<HttpDownloader> {

    override fun apply(props: Properties): HttpDownloader {
        val path = props.get<Path>("path")
        return HttpDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("http")
        )
    }
}