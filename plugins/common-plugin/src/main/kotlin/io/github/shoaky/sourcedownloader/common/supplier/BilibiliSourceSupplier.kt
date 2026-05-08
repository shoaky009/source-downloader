package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.bilibili.BilibiliSource
import io.github.shoaky.sourcedownloader.external.bilibili.BilibiliClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object BilibiliSourceSupplier : ComponentSupplier<BilibiliSource> {

    override fun apply(context: CoreContext, props: Properties): BilibiliSource {
        val cookie = props.getOrNull<String>("cookie")
        val favorites = props.get<List<Long>>("favorites")
        return BilibiliSource(BilibiliClient(sessionCookie = cookie), favorites)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("bilibili")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("favorites"),
                properties = mapOf(
                    "cookie" to JsonSchema(type = "string"),
                    "favorites" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "integer")
                    )
                )
            )
        )
    }
}
