package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.MappedFileTagger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object MappedFileTaggerSupplier : ComponentSupplier<MappedFileTagger> {

    override fun apply(context: CoreContext, props: Properties): MappedFileTagger {
        return MappedFileTagger(props.get("mapping"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("mapped")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("mapping"),
                properties = mapOf(
                    "mapping" to JsonSchema(
                        type = "object",
                        additionalProperties = JsonSchema(type = "string")
                    )
                )
            )
        )
    }
}
