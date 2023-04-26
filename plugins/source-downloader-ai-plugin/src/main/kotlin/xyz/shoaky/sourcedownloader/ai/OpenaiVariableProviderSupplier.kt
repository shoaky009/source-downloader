package xyz.shoaky.sourcedownloader.ai

import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object OpenaiVariableProviderSupplier : SdComponentSupplier<OpenaiVariableProvider> {
    override fun apply(props: ComponentProps): OpenaiVariableProvider {
        return OpenaiVariableProvider(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("openai")
        )
    }

}