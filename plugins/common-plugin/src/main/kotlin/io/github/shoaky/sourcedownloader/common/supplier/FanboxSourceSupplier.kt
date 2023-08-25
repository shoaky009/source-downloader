package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxSource
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

class FanboxSourceSupplier(
    private val instanceManager: InstanceManager
) : ComponentSupplier<FanboxSource> {

    override fun apply(props: Properties): FanboxSource {
        val load = instanceManager.load(props.get("client"), FanboxClient::class.java)
        return FanboxSource(load, props.getOrNull("mode"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fanbox")
        )
    }
}