package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.resolver.SystemFileResolver
import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

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

    override fun autoCreateDefault(): Boolean = true
}