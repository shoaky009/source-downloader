package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.ydl.YoutubeDLIntegration
import io.github.shoaky.sourcedownloader.external.ydl.YoutubeDLClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import java.nio.file.Path

object YoutubeDLIntegrationSupplier : ComponentSupplier<YoutubeDLIntegration> {

    override fun apply(context: CoreContext, props: Properties): YoutubeDLIntegration {
        val client = YoutubeDLClient(
            props.get("endpoint"),
            props.get("api-key")
        )
        return YoutubeDLIntegration(
            client,
            props.get<Path>("download-path")
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("ydl"),
            ComponentType.downloader("youtube-dl"),
            ComponentType.fileResolver("ydl"),
            ComponentType.fileResolver("youtube-dl"),
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("endpoint", "api-key", "download-path"),
                properties = mapOf(
                    "endpoint" to JsonSchema(type = "string", format = "uri"),
                    "api-key" to JsonSchema(type = "string"),
                    "download-path" to JsonSchema(type = "string")
                )
            )
        )
    }
}
