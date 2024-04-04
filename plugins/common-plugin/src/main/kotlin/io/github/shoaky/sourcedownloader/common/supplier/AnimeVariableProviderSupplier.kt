package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.AnimeVariableProvider
import io.github.shoaky.sourcedownloader.external.anilist.AnilistClient
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class AnimeVariableProviderSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<AnimeVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): AnimeVariableProvider {
        val bgmTvApiClient = props.getOrNull<String>("bgmtv-client")
            ?.let {
                pluginContext.loadInstance(it, BgmTvApiClient::class.java)
            } ?: BgmTvApiClient()

        return AnimeVariableProvider(
            bgmTvApiClient,
            AnilistClient(),
            props.getOrDefault("prefer-bgm-tv", false),
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("anime")
        )
    }

    override fun supportNoArgs(): Boolean = true
}