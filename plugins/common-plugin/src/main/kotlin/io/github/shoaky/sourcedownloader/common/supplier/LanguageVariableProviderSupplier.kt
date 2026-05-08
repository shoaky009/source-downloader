package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.LanguageVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object LanguageVariableProviderSupplier : ComponentSupplier<LanguageVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): LanguageVariableProvider {
        val readContent = props.getOrDefault("read-content", true)
        return LanguageVariableProvider(readContent)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("language")
        )
    }

    override fun supportNoArgs(): Boolean = true

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "read-content" to JsonSchema(
                        type = "boolean",
                        default = true
                    )
                )
            )
        )
    }
}
