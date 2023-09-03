package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxFileResolver
import io.github.shoaky.sourcedownloader.common.fanbox.FanboxSource
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.InstanceManager
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

class FanboxFileResolverSupplier(
    private val instanceManager: InstanceManager
) : ComponentSupplier<FanboxFileResolver> {

    override fun apply(props: Properties): FanboxFileResolver {
        return FanboxFileResolver(
            instanceManager.loadInstance(props.get("client"), FanboxClient::class.java)
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileResolver("fanbox")
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allow(ComponentTopType.SOURCE, FanboxSource::class)
        )
    }
}