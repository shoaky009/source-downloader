package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.provider.MetadataVariableProvider
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object MetadataVariableProviderSupplier : SdComponentSupplier<MetadataVariableProvider> {
    override fun apply(props: ComponentProps): MetadataVariableProvider {
        return MetadataVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("metadata")
        )
    }

}