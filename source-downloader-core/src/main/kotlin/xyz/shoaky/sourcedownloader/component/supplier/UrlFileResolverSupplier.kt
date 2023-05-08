package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.resolver.UrlFileResolver
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object UrlFileResolverSupplier : SdComponentSupplier<UrlFileResolver> {
    override fun apply(props: Properties): UrlFileResolver {
        return UrlFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("url"))
    }

    override fun autoCreateDefault(): Boolean = true
}