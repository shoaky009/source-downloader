package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxSource
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object FanboxSourceSupplier : ComponentSupplier<FanboxSource> {

    override fun apply(props: Properties): FanboxSource {
        val sessionId = props.get<String>("session-id")
        return FanboxSource(FanboxClient(sessionId))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fanbox")
        )
    }
}