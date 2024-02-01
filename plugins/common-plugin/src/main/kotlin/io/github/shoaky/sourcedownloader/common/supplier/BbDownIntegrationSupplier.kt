package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.bilibili.BbDownIntegration
import io.github.shoaky.sourcedownloader.common.bilibili.BbDownOptions
import io.github.shoaky.sourcedownloader.common.bilibili.BilibiliSource
import io.github.shoaky.sourcedownloader.external.bbdown.BbDownClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentTopType
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object BbDownIntegrationSupplier : ComponentSupplier<BbDownIntegration> {

    override fun apply(context: CoreContext, props: Properties): BbDownIntegration {
        return BbDownIntegration(
            props.get("downloadPath"),
            BbDownClient(props.get("endpoint")),
            props.getOrDefault("options", BbDownOptions())
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.downloader("bbdown"),
            ComponentType.fileResolver("bbdown")
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allow(ComponentTopType.SOURCE, BilibiliSource::class)
        )
    }
}