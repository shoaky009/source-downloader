package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

internal object DlsiteVariableProviderSupplier : ComponentSupplier<DlsiteVariableProvider> {

    override fun apply(props: Properties): DlsiteVariableProvider {
        return DlsiteVariableProvider(props.getOrDefault("locale", "zh-cn"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("dlsite")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}