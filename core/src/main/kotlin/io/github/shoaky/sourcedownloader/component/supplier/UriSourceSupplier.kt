package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.source.UriSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import java.net.URI

object UriSourceSupplier : ComponentSupplier<UriSource> {

    override fun apply(context: CoreContext, props: Properties): UriSource {
        val uri = props.get<URI>("uri")
        return UriSource(uri)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("uri")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("uri"),
                properties = mapOf(
                    "uri" to JsonSchema(type = "string", format = "uri")
                )
            )
        )
    }
}
