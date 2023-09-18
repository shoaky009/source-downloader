package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.MockDownloader
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.nio.file.Path

object MockDownloaderSupplier : ComponentSupplier<MockDownloader> {

    override fun apply(props: Properties): MockDownloader {
        val path = props.get<Path>("download-path")
        return MockDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("mock")
        )
    }

}