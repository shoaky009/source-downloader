package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.MikanClient
import io.github.shoaky.sourcedownloader.common.anime.MikanSource
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class MikanSourceSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<MikanSource> {

    override fun apply(props: Properties): MikanSource {
        val client = props.getOrNull<String>("client")?.let {
            pluginContext.loadInstance(it, MikanClient::class.java)
        } ?: MikanClient(null)

        val url = props.get<String>("url")
        val allEpisode = props.getOrNull<Boolean>("all-episode") ?: false
        return MikanSource(url, allEpisode, mikanClient = client)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("mikan")
        )
    }
}