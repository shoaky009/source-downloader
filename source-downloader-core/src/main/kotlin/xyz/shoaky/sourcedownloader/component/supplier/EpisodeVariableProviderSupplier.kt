package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.provider.EpisodeVariableProvider
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object EpisodeVariableProviderSupplier : SdComponentSupplier<EpisodeVariableProvider> {
    override fun apply(props: ComponentProps): EpisodeVariableProvider {
        return EpisodeVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("episode")
        )
    }
}