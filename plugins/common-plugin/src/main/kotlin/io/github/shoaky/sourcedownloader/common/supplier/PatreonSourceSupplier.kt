package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.patreon.PatreonSource
import io.github.shoaky.sourcedownloader.external.patreon.PatreonClient
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

class PatreonSourceSupplier(
    private val instanceManager: InstanceManager
) : ComponentSupplier<PatreonSource> {

    override fun apply(props: Properties): PatreonSource {
        val client = instanceManager.loadInstance(props.get("client"), PatreonClient::class.java)
        return PatreonSource(client)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("patreon")
        )
    }
}