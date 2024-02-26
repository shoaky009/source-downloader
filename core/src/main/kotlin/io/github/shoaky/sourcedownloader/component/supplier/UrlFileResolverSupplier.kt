package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.resolver.UrlFileResolver
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object UrlFileResolverSupplier : ComponentSupplier<UrlFileResolver> {

    override fun apply(context: CoreContext, props: Properties): UrlFileResolver {
        return UrlFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("url"))
    }

    override fun supportNoArgs(): Boolean = true
}