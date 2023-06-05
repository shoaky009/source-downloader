package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.anime.MikanVariableProvider
import xyz.shoaky.sourcedownloader.sdk.ComponentRule
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.TorrentDownloader

object MikanVariableProviderSupplier : SdComponentSupplier<MikanVariableProvider> {
    override fun apply(props: Properties): MikanVariableProvider {
        val token = props.getOrNull<String>("token")
        return MikanVariableProvider(token)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.provider("mikan"))
    }

    override fun rules(): List<ComponentRule> {
        return listOf(ComponentRule.allowDownloader(TorrentDownloader::class))
    }
}