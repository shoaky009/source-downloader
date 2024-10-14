package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.torrent.TransmissionDownloader
import io.github.shoaky.sourcedownloader.external.transmission.TransmissionClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object TransmissionDownloaderSupplier : ComponentSupplier<TransmissionDownloader> {

    override fun apply(context: CoreContext, props: Properties): TransmissionDownloader {
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

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "url" to JsonSchema(
                        type = "string",
                        description = "Transmission RPC URL"
                    ),
                    "username" to JsonSchema(
                        type = "string",
                        description = "Transmission RPC username"
                    ),
                    "password" to JsonSchema(
                        type = "string",
                        description = "Transmission RPC password"
                    )
                ),
                required = listOf("url")
            )
        )
    }
}