package xyz.shoaky.sourcedownloader.common.mikan

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

internal object MikanSourceSupplier : SdComponentSupplier<MikanSource> {
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