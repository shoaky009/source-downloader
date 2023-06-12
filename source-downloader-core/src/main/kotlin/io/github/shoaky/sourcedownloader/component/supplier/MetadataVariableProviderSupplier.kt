package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.provider.MetadataVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object MetadataVariableProviderSupplier : SdComponentSupplier<MetadataVariableProvider> {
    override fun apply(props: Properties): MetadataVariableProvider {
        return MetadataVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("metadata")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}