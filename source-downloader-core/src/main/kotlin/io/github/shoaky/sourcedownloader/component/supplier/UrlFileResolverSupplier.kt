package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.resolver.UrlFileResolver
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object UrlFileResolverSupplier : SdComponentSupplier<UrlFileResolver> {
    override fun apply(props: Properties): UrlFileResolver {
        return UrlFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("url"))
    }

    override fun autoCreateDefault(): Boolean = true
}