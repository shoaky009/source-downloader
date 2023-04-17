package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.downloader.UrlDownloader
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import kotlin.io.path.Path

object UrlDownloaderSupplier : SdComponentSupplier<UrlDownloader> {
    override fun apply(props: ComponentProps): UrlDownloader {
        val path = props.properties["download-path"]?.let {
            Path(it.toString())
        } ?: throw RuntimeException("download-path is null")
        return UrlDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("url"),
        )
    }

    override fun getComponentClass(): Class<UrlDownloader> {
        return UrlDownloader::class.java
    }
}