package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.downloader.UrlDownloader
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import java.nio.file.Path

object UrlDownloaderSupplier : ComponentSupplier<UrlDownloader> {

    override fun apply(context: CoreContext, props: Properties): UrlDownloader {
        val path = props.get<Path>("download-path")
        return UrlDownloader(path)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("url"),
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("download-path"),
                properties = mapOf(
                    "download-path" to JsonSchema(type = "string")
                )
            )
        )
    }

}
