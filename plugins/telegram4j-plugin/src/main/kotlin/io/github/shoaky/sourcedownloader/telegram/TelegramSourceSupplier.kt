package io.github.shoaky.sourcedownloader.telegram

import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema
import io.github.shoaky.sourcedownloader.sdk.plugin.PluginContext

class TelegramSourceSupplier(
    private val pluginContext: PluginContext
) : ComponentSupplier<TelegramSource> {

    override fun apply(context: CoreContext, props: Properties): TelegramSource {
        val chats = props.get<List<ChatConfig>>("chats")
        val clientName = props.get<String>("client")
        val sites = props.getOrDefault<Set<String>>("sites", setOf("Telegraph"))
        val nonMedia = props.getOrDefault<Boolean>("include-non-media", false)
        val client = pluginContext.loadInstance(clientName, TelegramClientWrapper::class.java)
        return TelegramSource(TelegramMessageFetcher(client), chats, sites, nonMedia)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("telegram"),
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                properties = mapOf(
                    "client" to JsonSchema(
                        title = "客户端",
                        type = "string",
                        description = "客户端名称引用"
                    ),
                    "chats" to JsonSchema(
                        title = "频道",
                        type = "array",
                        items = JsonSchema(
                            type = "object",
                            required = listOf("chatId"),
                            properties = mapOf(
                                "chatId" to JsonSchema(
                                    title = "频道ID",
                                    type = "string",
                                    description = "私聊不需要-号频道需要-号,不需要添加100前缀",
                                ),
                                "beginDate" to JsonSchema(
                                    title = "消息起始日期",
                                    type = "string",
                                )
                            ),
                        ),
                    ),
                    "sites" to JsonSchema(
                        title = "站点",
                        type = "array",
                        items = JsonSchema(
                            type = "string",
                        ),
                        description = "消息为webpage类型时支持的网站",
                        default = listOf("Telegraph"),
                    ),
                    "include-non-media" to JsonSchema(
                        title = "包括非媒体类型",
                        type = "boolean",
                        description = "非媒体类型的消息是否也要包括",
                        default = false
                    )
                ),
                required = listOf("client", "chats")
            )
        )
    }
}