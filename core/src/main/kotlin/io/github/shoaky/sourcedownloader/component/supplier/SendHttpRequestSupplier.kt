package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.SendHttpRequest
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object SendHttpRequestSupplier : ComponentSupplier<SendHttpRequest> {

    override fun apply(context: CoreContext, props: Properties): SendHttpRequest {
        return SendHttpRequest(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.listener("http")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("url"),
                properties = mapOf(
                    "url" to JsonSchema(type = "string", format = "uri"),
                    "method" to JsonSchema(
                        type = "string",
                        enum = listOf("GET", "POST", "PUT", "DELETE"),
                        default = "POST"
                    ),
                    "headers" to JsonSchema(
                        type = "object",
                        additionalProperties = JsonSchema(type = "string"),
                        default = emptyMap<String, String>(),
                    ),
                    "body" to JsonSchema(type = "string"),
                    "with-content-body" to JsonSchema(
                        type = "boolean",
                        default = false
                    )
                )
            )
        )
    }

}
