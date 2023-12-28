package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.pixiv.PixivIntegration
import io.github.shoaky.sourcedownloader.external.pixiv.PixivClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentRule
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object PixivIntegrationSupplier : ComponentSupplier<PixivIntegration> {

    override fun apply(context: CoreContext, props: Properties): PixivIntegration {
        val sessionId = props.get<String>("session-id")
        val mode = props.getOrDefault<String>("mode", "bookmark")
        val client = PixivClient(sessionId)
        val userId = props.getOrNull<Long>("user-id") ?: run {
            if (client.sessionId == null) {
                throw IllegalStateException("Client sessionId is null")
            }
            Regex("\\d+_").find(client.sessionId)?.value?.dropLast(1)?.toLongOrNull()
                ?: throw IllegalStateException("sessionId is null")
        }
        return PixivIntegration(userId, client, mode)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("pixiv"),
            ComponentType.fileResolver("pixiv")
        )
    }

    override fun rules(): List<ComponentRule> {
        return listOf(
            ComponentRule.allowSource(PixivIntegration::class)
        )
    }
}