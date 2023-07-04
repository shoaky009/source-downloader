package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.MockDownloader
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import kotlin.io.path.Path

object MockDownloaderSupplier : ComponentSupplier<MockDownloader> {

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