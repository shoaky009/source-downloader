package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.MediaTypeExistsDetector
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object MediaTypeExistsDetectorSupplier : ComponentSupplier<MediaTypeExistsDetector> {

    override fun apply(context: CoreContext, props: Properties): MediaTypeExistsDetector {
        return MediaTypeExistsDetector
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.itemExistsDetector("media-type")
        )
    }

    override fun supportNoArgs(): Boolean = true
}