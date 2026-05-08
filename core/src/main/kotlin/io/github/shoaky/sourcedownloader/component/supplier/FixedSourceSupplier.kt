package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.source.FixedSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object FixedSourceSupplier : ComponentSupplier<FixedSource> {

    override fun apply(context: CoreContext, props: Properties): FixedSource {
        return FixedSource(props.get("content"), props.getOrDefault("offset-mode", false))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fixed"),
            ComponentType.fileResolver("fixed"),
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("content"),
                properties = mapOf(
                    "content" to fixedSourceContentSchema(),
                    "offset-mode" to JsonSchema(
                        type = "boolean",
                        default = false,
                        description = "按偏移量分页返回固定内容"
                    )
                )
            )
        )
    }
}

private fun fixedSourceContentSchema(): JsonSchema {
    return JsonSchema(
        type = "array",
        items = JsonSchema(
            type = "object",
            required = listOf("item", "files"),
            properties = mapOf(
                "item" to JsonSchema(
                    type = "object",
                    required = listOf("title", "link", "datetime", "contentType", "downloadUri"),
                    properties = mapOf(
                        "title" to JsonSchema(type = "string"),
                        "link" to JsonSchema(type = "string", format = "uri"),
                        "datetime" to JsonSchema(type = "string", format = "date-time"),
                        "contentType" to JsonSchema(type = "string"),
                        "downloadUri" to JsonSchema(type = "string", format = "uri"),
                        "attrs" to JsonSchema(type = "object", additionalProperties = true),
                        "tags" to JsonSchema(type = "array", items = JsonSchema(type = "string")),
                        "identity" to JsonSchema(type = "string")
                    )
                ),
                "files" to JsonSchema(
                    type = "array",
                    items = JsonSchema(
                        type = "object",
                        required = listOf("path"),
                        properties = mapOf(
                            "path" to JsonSchema(type = "string"),
                            "attrs" to JsonSchema(type = "object", additionalProperties = true),
                            "downloadUri" to JsonSchema(type = "string", format = "uri"),
                            "fileUri" to JsonSchema(type = "string", format = "uri"),
                            "tags" to JsonSchema(type = "array", items = JsonSchema(type = "string"))
                        )
                    )
                )
            )
        )
    )
}
