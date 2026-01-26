package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.fanbox.FanboxIntegration
import io.github.shoaky.sourcedownloader.external.fanbox.FanboxClient
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object FanboxIntegrationSupplier : ComponentSupplier<FanboxIntegration> {

    override fun apply(context: CoreContext, props: Properties): FanboxIntegration {
        val headers = props.getOrNull<Map<String, String>>("headers")
        // FANBOXSESSID=xxx;
        val cookie: String = props.get("cookie")
        val client = headers?.let {
            FanboxClient(headers = it, cookie = cookie)
        } ?: FanboxClient(cookie = cookie)

        return FanboxIntegration(client, props.getOrNull("mode"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fanbox"),
            ComponentType.fileResolver("fanbox")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            "Fanbox",
            JsonSchema(
                type = "object",
                required = listOf("cookie"),
                properties = mapOf(
                    "headers" to JsonSchema(
                        type = "object",
                        description = "Headers for Fanbox API",
                        additionalProperties = JsonSchema(
                            type = "string"
                        )
                    ),
                    "cookie" to JsonSchema(
                        type = "string",
                        description = "FANBOXSESSID cookie"
                    ),
                    "mode" to JsonSchema(
                        type = "string",
                        description = "Cookie for Fanbox API",
                        enum = listOf("all", "latestOnly")
                    )
                ),
            )
        )
    }
}