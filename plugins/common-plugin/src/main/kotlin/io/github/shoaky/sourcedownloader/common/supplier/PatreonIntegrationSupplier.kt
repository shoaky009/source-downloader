package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.patreon.PatreonIntegration
import io.github.shoaky.sourcedownloader.external.patreon.PatreonClient
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object PatreonIntegrationSupplier : ComponentSupplier<PatreonIntegration> {

    override fun apply(props: Properties): PatreonIntegration {
        val sid = props.get<String>("session-id")
        val headers = props.getOrNull<Map<String, String>>("headers")
        headers?.let {
            return PatreonIntegration(PatreonClient(sid, headers = it))
        }
        return PatreonIntegration(PatreonClient(sid))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("patreon"),
            ComponentType.fileResolver("patreon"),
        )
    }
}