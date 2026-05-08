package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.ResolutionVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object ResolutionVariableProviderSupplier : ComponentSupplier<ResolutionVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): ResolutionVariableProvider {
        val opt = props.getOrDefault("onlyHighResolution", true)
        return ResolutionVariableProvider(opt)

    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("resolution")
        )
    }

    override fun supportNoArgs(): Boolean {
        return true
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "onlyHighResolution" to JsonSchema(
                        type = "boolean",
                        default = true
                    )
                )
            )
        )
    }
}
