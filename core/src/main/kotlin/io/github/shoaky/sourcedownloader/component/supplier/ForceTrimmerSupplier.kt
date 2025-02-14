package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ForceTrimmer
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object ForceTrimmerSupplier : ComponentSupplier<ForceTrimmer> {

    override fun apply(context: CoreContext, props: Properties): ForceTrimmer {
        return ForceTrimmer
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.trimmer("force"))
    }

    override fun supportNoArgs(): Boolean = true
    
}