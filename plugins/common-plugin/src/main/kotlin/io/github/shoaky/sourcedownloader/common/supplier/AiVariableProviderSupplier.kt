package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.ai.AiVariableProvider
import io.github.shoaky.sourcedownloader.external.openai.AiClient
import io.github.shoaky.sourcedownloader.external.openai.ChatMessage
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

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

}