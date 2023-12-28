package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.bilibili.BilibiliSource
import io.github.shoaky.sourcedownloader.external.bilibili.BilibiliClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object BilibiliSourceSupplier : ComponentSupplier<BilibiliSource> {

    override fun apply(context: CoreContext, props: Properties): BilibiliSource {
        val cookie = props.getOrNull<String>("cookie")
        val favorites = props.getOrDefault<List<Long>>("favorites", emptyList())
        return BilibiliSource(BilibiliClient(sessionCookie = cookie), favorites)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("bilibili")
        )
    }
}