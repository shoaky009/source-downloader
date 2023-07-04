package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.MikanVariableProvider
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.TorrentDownloader

object MikanVariableProviderSupplier : ComponentSupplier<MikanVariableProvider> {

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