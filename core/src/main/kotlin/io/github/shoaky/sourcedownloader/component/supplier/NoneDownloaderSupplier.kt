package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.NoneDownloader
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import kotlin.io.path.Path

object NoneDownloaderSupplier : ComponentSupplier<NoneDownloader> {

    override fun apply(context: CoreContext, props: Properties): NoneDownloader {
        val downloadPath = props.getOrDefault("downloadPath", Path("").toAbsolutePath())
        return NoneDownloader(downloadPath)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("none")
        )
    }

    override fun supportNoArgs(): Boolean {
        return true
    }
}