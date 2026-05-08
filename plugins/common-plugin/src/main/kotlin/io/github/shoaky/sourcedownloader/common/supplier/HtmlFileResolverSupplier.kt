package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.HtmlFileResolver
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object HtmlFileResolverSupplier : ComponentSupplier<HtmlFileResolver> {

    override fun apply(context: CoreContext, props: Properties): HtmlFileResolver {
        return HtmlFileResolver(
            props.get("css-selector"),
            props.get("extract-attribute"),
            props.getOrDefault<Boolean>("direct-mode", false),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("html"))
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("css-selector", "extract-attribute"),
                properties = mapOf(
                    "css-selector" to JsonSchema(type = "string"),
                    "extract-attribute" to JsonSchema(type = "string"),
                    "direct-mode" to JsonSchema(type = "boolean", default = false)
                )
            )
        )
    }
}
