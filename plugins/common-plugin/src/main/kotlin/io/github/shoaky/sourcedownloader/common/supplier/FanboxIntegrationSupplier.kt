package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxIntegration
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object FanboxIntegrationSupplier : ComponentSupplier<FanboxIntegration> {

    override fun apply(props: Properties): FanboxIntegration {
        val headers = props.getOrNull<Map<String, String>>("headers")
        val client = headers?.let {
            FanboxClient(props.get("session-id"), headers = it)
        } ?: FanboxClient(props.get("session-id"))
        return FanboxIntegration(client, props.getOrNull("mode"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fanbox"),
            ComponentType.fileResolver("fanbox")
        )
    }
}