package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.EpisodeVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object EpisodeVariableProviderSupplier : ComponentSupplier<EpisodeVariableProvider> {

    override fun apply(props: Properties): EpisodeVariableProvider {
        return EpisodeVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("episode")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}