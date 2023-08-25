package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.MikanSource
import io.github.shoaky.sourcedownloader.common.anime.MikanSupport
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

internal object MikanSourceSupplier : ComponentSupplier<MikanSource> {

    override fun apply(props: Properties): MikanSource {
        val url = props.get<String>("url")
        val allEpisode = props.getOrNull<Boolean>("all-episode") ?: false
        val token = props.getOrNull<String>("token")
        return MikanSource(url, allEpisode, mikanSupport = MikanSupport(token))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("mikan")
        )
    }
}