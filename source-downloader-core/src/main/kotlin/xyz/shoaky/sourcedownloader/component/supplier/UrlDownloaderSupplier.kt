package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.downloader.UrlDownloader
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import kotlin.io.path.Path

object UrlDownloaderSupplier : SdComponentSupplier<UrlDownloader> {
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