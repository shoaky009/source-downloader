package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.MikanClient
import io.github.shoaky.sourcedownloader.common.anime.MikanSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class MikanSourceSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<MikanSource> {

    override fun apply(context: CoreContext, props: Properties): MikanSource {
        val client = props.getOrNull<String>("client")?.let {
            pluginContext.loadInstance(it, MikanClient::class.java)
        } ?: MikanClient(null)

        val url = props.get<String>("url")
        val allEpisode = props.getOrNull<Boolean>("all-episode") ?: false
        return MikanSource(url, allEpisode, mikanClient = client)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("mikan")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            description = "Mikan Project",
            propertySchema = JsonSchema(
                type = "object",
                description = "Mikan Project",
                required = listOf("url"),
                properties = mapOf(
                    "url" to JsonSchema(
                        type = "string",
                        title = "URL",
                        description = "Mikan订阅地址"
                    ),
                    "all-episode" to JsonSchema(
                        type = "boolean",
                        title = "All Episode",
                        default = false,
                    ),
                    "client" to JsonSchema(
                        type = "string",
                        title = "Client",
                        description = "客户端名称"
                    )
                ),
            )
        )
    }
}