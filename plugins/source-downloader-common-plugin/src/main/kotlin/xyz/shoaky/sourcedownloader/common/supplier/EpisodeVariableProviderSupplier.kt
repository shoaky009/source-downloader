package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.EpisodeVariableProvider
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object EpisodeVariableProviderSupplier : SdComponentSupplier<EpisodeVariableProvider> {
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