package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.anime.AnimeVariableProvider
import xyz.shoaky.sourcedownloader.external.anilist.AnilistClient
import xyz.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object AnimeVariableProviderSupplier : SdComponentSupplier<AnimeVariableProvider> {
    override fun apply(props: Properties): AnimeVariableProvider {
        return AnimeVariableProvider(
            BgmTvApiClient(props.getOrNull("bgm-token")),
            AnilistClient(autoLimit = props.getOrDefault("auto-limit", false))
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("anime")
        )
    }
}