package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.resolver.SystemFileResolver
import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object SystemFileResolverSupplier : ComponentSupplier<SystemFileResolver> {

    override fun apply(context: CoreContext, props: Properties): SystemFileResolver {
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