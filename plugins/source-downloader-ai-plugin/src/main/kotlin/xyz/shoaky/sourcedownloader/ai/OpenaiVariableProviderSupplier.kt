package xyz.shoaky.sourcedownloader.ai

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object OpenaiVariableProviderSupplier : SdComponentSupplier<OpenAiVariableProvider> {
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