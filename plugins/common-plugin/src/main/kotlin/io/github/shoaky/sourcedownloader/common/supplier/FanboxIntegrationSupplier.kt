package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxIntegration
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object FanboxIntegrationSupplier : ComponentSupplier<FanboxIntegration> {

    override fun apply(context: CoreContext, props: Properties): FanboxIntegration {
        val headers = props.getOrNull<Map<String, String>>("headers")
        // cf_clearance=xxx
        val cookie: String = props.get("cookie")
        val client = headers?.let {
            FanboxClient(props.get("session-id"), headers = it, cookie = cookie)
        } ?: FanboxClient(props.get("session-id"), cookie = cookie)

        return FanboxIntegration(client, props.getOrNull("mode"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fanbox"),
            ComponentType.fileResolver("fanbox")
        )
    }
}