package xyz.shoaky.sourcedownloader.common.torrent

import xyz.shoaky.sourcedownloader.external.transmission.TransmissionClient
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object TransmissionDownloaderSupplier : SdComponentSupplier<TransmissionDownloader> {
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