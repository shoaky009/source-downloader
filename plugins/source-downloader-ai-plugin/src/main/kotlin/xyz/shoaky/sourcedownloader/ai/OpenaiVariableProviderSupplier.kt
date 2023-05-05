package xyz.shoaky.sourcedownloader.ai

import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

object OpenaiVariableProviderSupplier : SdComponentSupplier<OpenAiVariableProvider> {
    override fun apply(props: Properties): OpenAiVariableProvider {
        return OpenAiVariableProvider(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("openai")
        )
    }

}