package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.replacer.FullWidthReplacer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object FullWidthReplacerSupplier : ComponentSupplier<FullWidthReplacer> {

    override fun apply(context: CoreContext, props: Properties): FullWidthReplacer {
        return FullWidthReplacer
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableReplacer("full-width")
        )
    }

    override fun supportNoArgs(): Boolean {
        return true
    }
}