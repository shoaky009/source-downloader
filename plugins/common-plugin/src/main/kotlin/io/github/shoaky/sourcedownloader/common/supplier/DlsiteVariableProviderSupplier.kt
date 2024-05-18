package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProvider
import io.github.shoaky.sourcedownloader.external.dlsite.DlsiteClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

internal object DlsiteVariableProviderSupplier : ComponentSupplier<DlsiteVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): DlsiteVariableProvider {
        return DlsiteVariableProvider(
            DlsiteClient(),
            props.getOrDefault("locale", "ja-jp"),
            props.getOrDefault("onlyExtractId", false),
            searchWorkTypeCategories = props.getOrDefault("searchWorkTypeCategories", emptyList()),
            preferSuggest = props.getOrDefault("preferSuggest", true),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("dlsite")
        )
    }

    override fun supportNoArgs(): Boolean = true
}