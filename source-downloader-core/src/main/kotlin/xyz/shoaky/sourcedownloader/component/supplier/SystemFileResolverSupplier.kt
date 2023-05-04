package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.resolver.SystemFileResolver
import xyz.shoaky.sourcedownloader.component.source.SystemFileSource
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

object SystemFileResolverSupplier : SdComponentSupplier<SystemFileResolver> {
    override fun apply(props: Properties): SystemFileResolver {
        return SystemFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileResolver("system-file")
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(
                SystemFileSource::class
            )
        )
    }
}