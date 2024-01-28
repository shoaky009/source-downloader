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
            Regex("\\d+_").find(sessionId)?.value?.dropLast(1)?.toLongOrNull()
                ?: throw IllegalStateException("sessionId is invalid, because user-id is not provided")
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