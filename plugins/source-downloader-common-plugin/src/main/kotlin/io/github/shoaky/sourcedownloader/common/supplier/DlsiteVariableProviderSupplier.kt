package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.dlsite.DlsiteVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

internal object DlsiteVariableProviderSupplier : SdComponentSupplier<DlsiteVariableProvider> {
    override fun apply(props: Properties): DlsiteVariableProvider {
        return DlsiteVariableProvider(props.getOrDefault("locale", "zh-cn"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("dlsite")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}