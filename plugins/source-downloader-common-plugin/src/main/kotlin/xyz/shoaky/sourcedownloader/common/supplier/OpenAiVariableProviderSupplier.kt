package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.ai.OpenAiVariableProvider
import xyz.shoaky.sourcedownloader.external.openai.ChatMessage
import xyz.shoaky.sourcedownloader.external.openai.OpenAiClient
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object OpenAiVariableProviderSupplier : SdComponentSupplier<OpenAiVariableProvider> {
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