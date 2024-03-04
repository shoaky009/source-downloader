package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.replacer.WindowsPathReplacer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object WindowsPathReplacerSupplier : ComponentSupplier<WindowsPathReplacer> {

    override fun apply(context: CoreContext, props: Properties): WindowsPathReplacer {
        return WindowsPathReplacer
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.variableReplacer("windows-path")
        )
    }

    override fun supportNoArgs(): Boolean {
        return true
    }
}