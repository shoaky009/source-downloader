package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxFileResolver
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

class FanboxFileResolverSupplier(
    private val instanceManager: InstanceManager
) : ComponentSupplier<FanboxFileResolver> {

    override fun apply(props: Properties): FanboxFileResolver {
        return FanboxFileResolver(
            instanceManager.load(props.get("client"), FanboxClient::class.java)
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileResolver("fanbox")
        )
    }
}