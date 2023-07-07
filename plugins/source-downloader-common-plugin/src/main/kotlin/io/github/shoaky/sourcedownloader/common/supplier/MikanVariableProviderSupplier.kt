package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.MikanVariableProvider
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sdk.PluginContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader

class MikanVariableProviderSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<MikanVariableProvider> {

    override fun apply(props: Properties): MikanVariableProvider {
        val token = props.getOrNull<String>("token")
        val bgmtvClient = props.getOrNull<String>("bgmtv-client")
            ?.let {
                pluginContext.loadInstance(it, BgmTvApiClient::class.java)
            } ?: BgmTvApiClient()

        return MikanVariableProvider(
            token,
            bgmTvClient = bgmtvClient
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.provider("mikan"))
    }

    override fun rules(): List<ComponentRule> {
        return listOf(ComponentRule.allowDownloader(TorrentDownloader::class))
    }
}