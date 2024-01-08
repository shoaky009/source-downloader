package io.github.shoaky.sourcedownloader.support

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import org.springframework.stereotype.Component

@Component
object TestDirErrorDownloaderSupplier : ComponentSupplier<TestDirErrorDownloader> {

    override fun apply(context: CoreContext, props: Properties): TestDirErrorDownloader {
        return TestDirErrorDownloader(props.get("download-path"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("test-dir-error")
        )
    }
}