package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.ai.AiVariableProvider
import io.github.shoaky.sourcedownloader.external.openai.AiClient
import io.github.shoaky.sourcedownloader.external.openai.ChatMessage
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentMetadata
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.JsonSchema

object AiVariableProviderSupplier : ComponentSupplier<AiVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): AiVariableProvider {
        val config = props.parse<AiVariableProvider.AiConfig>()
        val client = AiClient(
            config.apiKeys
        )
        val primary = props.getOrNull<String>("primary")
        return AiVariableProvider(
            config.apiHost,
            client,
            ChatMessage.ofSystem(config.systemRole),
            primary,
            config.model,
            temperature = config.temperature
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            // ComponentType.variableProvider("openai"),
            ComponentType.variableProvider("ai")
        )
    }

    override fun metadata(): ComponentMetadata {
        return ComponentMetadata(
            propertySchema = JsonSchema(
                type = "object",
                required = listOf("apiKeys"),
                properties = mapOf(
                    "apiKeys" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "string")
                    ),
                    "resolveVariables" to JsonSchema(
                        type = "array",
                        items = JsonSchema(type = "string"),
                        default = emptyList<String>(),
                    ),
                    "apiHost" to JsonSchema(
                        type = "string",
                        format = "uri",
                        default = "https://api.openai.com"
                    ),
                    "systemRole" to JsonSchema(type = "string"),
                    "model" to JsonSchema(type = "string", default = "gpt-3.5-turbo"),
                    "temperature" to JsonSchema(type = "number", default = 0.85),
                    "primary" to JsonSchema(type = "string")
                )
            )
        )
    }

}
