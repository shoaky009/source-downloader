package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.KeywordIntegration
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import java.nio.file.Path

object KeywordIntegrationSupplier : ComponentSupplier<KeywordIntegration> {

    override fun apply(context: CoreContext, props: Properties): KeywordIntegration {
        val keywords = props.getOrDefault<List<String>>("keywords", emptyList())
        val keywordFile = props.getOrNull<Path>("keyword-file")
        return KeywordIntegration(
            keywords,
            keywordFile
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("keyword"),
            ComponentType.itemFilter("keyword"),
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "keywords" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "string"),
                        default = emptyList<String>(),
                    ),
                    "keyword-file" to JsonSchema(
                        type = "string",
                        description = "关键词文件路径"
                    )
                )
            )
        )
    }
}
