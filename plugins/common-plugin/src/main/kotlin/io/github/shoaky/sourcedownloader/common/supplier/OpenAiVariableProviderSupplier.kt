package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.ai.OpenAiVariableProvider
import io.github.shoaky.sourcedownloader.external.openai.ChatMessage
import io.github.shoaky.sourcedownloader.external.openai.OpenAiClient
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object OpenAiVariableProviderSupplier : ComponentSupplier<OpenAiVariableProvider> {

    override fun apply(props: Properties): OpenAiVariableProvider {
        val config = props.parse<OpenAiVariableProvider.OpenAiConfig>()
        val client = OpenAiClient(
            config.apiKeys
        )
        return OpenAiVariableProvider(config.apiHost, client, ChatMessage.ofSystem(config.systemRole))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("openai")
        )
    }

}