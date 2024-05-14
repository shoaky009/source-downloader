package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.TmdbVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object TmdbVariableProviderSupplier : ComponentSupplier<TmdbVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): TmdbVariableProvider {
        return TmdbVariableProvider(
            language = props.getOrDefault("language", "zh-CN")
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("tmdb")
        )
    }

    override fun supportNoArgs(): Boolean = true
}