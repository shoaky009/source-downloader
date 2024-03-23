package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.BgmTvVariableProvider
import io.github.shoaky.sourcedownloader.external.bangumi.BgmTvApiClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class BgmTvVariableProviderSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<BgmTvVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): BgmTvVariableProvider {
        val client = props.getOrNull<String>("client")
            ?.let {
                pluginContext.loadInstance(it, BgmTvApiClient::class.java)
            } ?: BgmTvApiClient()

        return BgmTvVariableProvider(
            client,
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("bgmtv")
        )
    }

    override fun supportNoArgs(): Boolean = true
}