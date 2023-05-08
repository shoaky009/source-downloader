package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.downloader.MockDownloader
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import kotlin.io.path.Path

object MockDownloaderSupplier : SdComponentSupplier<MockDownloader> {
    override fun apply(props: Properties): MockDownloader {
        val path = props.rawValues["download-path"]?.let {
            Path(it.toString())
        } ?: throw RuntimeException("download-path is null")
        return MockDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("mock")
        )
    }

}