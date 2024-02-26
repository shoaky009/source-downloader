package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.FileSizeReplacementDecider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object FileSizeReplacementDeciderSupplier : ComponentSupplier<FileSizeReplacementDecider> {

    override fun apply(context: CoreContext, props: Properties): FileSizeReplacementDecider {
        return FileSizeReplacementDecider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileReplacementDecider("size")
        )
    }

    override fun supportNoArgs(): Boolean = true
}