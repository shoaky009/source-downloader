package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.MikanClient
import io.github.shoaky.sourcedownloader.common.anime.MikanVariableProvider
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.external.tmdb.TmdbClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class MikanVariableProviderSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<MikanVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): MikanVariableProvider {
        val bgmtvClient = props.getOrNull<String>("bgmtv-client")
            ?.let {
                pluginContext.loadInstance(it, BgmTvApiClient::class.java)
            } ?: BgmTvApiClient()

        val mikanClient = props.getOrNull<String>("mikan-client")
            ?.let {
                pluginContext.loadInstance(it, MikanClient::class.java)
            } ?: MikanClient(null)

        val tmdbClient = props.getOrNull<String>("tmdb-client")
            ?.let {
                pluginContext.loadInstance(it, TmdbClient::class.java)
            } ?: TmdbClient(TmdbClient.DEFAULT_TOKEN)
        return MikanVariableProvider(
            mikanClient,
            bgmtvClient,
            tmdbClient
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.variableProvider("mikan"))
    }

    override fun rules(): List<ComponentRule> {
        return listOf(ComponentRule.allowDownloader(TorrentDownloader::class))
    }
}