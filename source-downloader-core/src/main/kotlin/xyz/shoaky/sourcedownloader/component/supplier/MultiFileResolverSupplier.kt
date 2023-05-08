package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.resolver.MultiFileResolver
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object MultiFileResolverSupplier : SdComponentSupplier<MultiFileResolver> {
    override fun apply(props: Properties): MultiFileResolver {
        return MultiFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileResolver("multi")
        )
    }
}