package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object SystemFileSourceSupplier : ComponentSupplier<SystemFileSource> {

    override fun apply(context: CoreContext, props: Properties): SystemFileSource {
        return SystemFileSource(props.get("path"),
            props.getOrDefault("mode", 0)
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("system-file"),
            ComponentType.downloader("system-file"),
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("path"),
                properties = mapOf(
                    "path" to JsonSchema(type = "string"),
                    "mode" to JsonSchema(
                        type = "integer",
                        description = "0: 顶层文件/目录作为 item, 1: 所有子文件各自作为 item",
                        enum = listOf(0, 1),
                        default = 0
                    )
                )
            )
        )
    }

}
