package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.provider.SequenceVariableProvider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object SequenceVariableProviderSupplier : ComponentSupplier<SequenceVariableProvider> {

    override fun apply(context: CoreContext, props: Properties): SequenceVariableProvider {
        return SequenceVariableProvider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableProvider("sequence")
        )
    }

    override fun supportNoArgs(): Boolean = true
}