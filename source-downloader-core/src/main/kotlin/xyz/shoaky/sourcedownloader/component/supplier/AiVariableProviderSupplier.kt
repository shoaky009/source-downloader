package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.provider.AiVariableProvider
import xyz.shoaky.sourcedownloader.sdk.component.ComponentProps
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object AiVariableProviderSupplier : SdComponentSupplier<AiVariableProvider> {
    override fun apply(props: ComponentProps): AiVariableProvider {
        return AiVariableProvider(props.parse())
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.provider("ai"),
            ComponentType.provider("gpt")
        )
    }

    override fun getComponentClass(): Class<AiVariableProvider> {
        return AiVariableProvider::class.java
    }

}