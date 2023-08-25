package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.torrent.TransmissionDownloader
import io.github.shoaky.sourcedownloader.external.transmission.TransmissionClient
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object TransmissionDownloaderSupplier : ComponentSupplier<TransmissionDownloader> {

    override fun apply(props: Properties): TransmissionDownloader {
        return TransmissionDownloader(
            TransmissionClient(
                props.get("url"),
                props.getOrNull("username"),
                props.getOrNull("password")
            ))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            // NOTE transmission支持完整的命名支持再添加mover
            ComponentType.downloader("transmission")
        )
    }
}