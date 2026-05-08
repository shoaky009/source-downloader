package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.RegexTrimmer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object RegexTrimmerSupplier : ComponentSupplier<RegexTrimmer> {

    override fun apply(context: CoreContext, props: Properties): RegexTrimmer {
        return RegexTrimmer(Regex(props.get<String>("regex")))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.trimmer("regex")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("regex"),
                properties = mapOf(
                    "regex" to JsonSchema(
                        type = "string",
                        description = "用于裁剪文本的正则表达式"
                    )
                )
            )
        )
    }
}
