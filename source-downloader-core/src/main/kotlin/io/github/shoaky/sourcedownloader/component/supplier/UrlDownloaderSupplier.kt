package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.UrlDownloader
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import kotlin.io.path.Path

object UrlDownloaderSupplier : ComponentSupplier<UrlDownloader> {

    override fun apply(props: Properties): UrlDownloader {
        val path = props.rawValues["download-path"]?.let {
            Path(it.toString())
        } ?: throw RuntimeException("download-path is null")
        return UrlDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("url"),
        )
    }

}