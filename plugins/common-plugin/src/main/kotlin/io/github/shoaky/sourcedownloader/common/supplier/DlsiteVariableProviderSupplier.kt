package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProvider
import io.github.shoaky.sourcedownloader.external.dlsite.DlsiteClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

internal object DlsiteVariableProviderSupplier : ComponentSupplier<DlsiteVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): DlsiteVariableProvider {
        return DlsiteVariableProvider(
            DlsiteClient(),
            props.getOrDefault("locale", "ja-jp"),
            props.getOrDefault("only-extract-id", false),
            searchWorkTypeCategories = props.getOrDefault("search-work-type-categories", emptyList()),
            preferSuggest = props.getOrDefault("prefer-suggest", true),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("dlsite")
        )
    }

    override fun supportNoArgs(): Boolean = true

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "locale" to JsonSchema(type = "string", default = "ja-jp"),
                    "only-extract-id" to JsonSchema(type = "boolean", default = false),
                    "search-work-type-categories" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "string"),
                        default = emptyList<String>(),
                    ),
                    "prefer-suggest" to JsonSchema(type = "boolean", default = true)
                )
            )
        )
    }
}
