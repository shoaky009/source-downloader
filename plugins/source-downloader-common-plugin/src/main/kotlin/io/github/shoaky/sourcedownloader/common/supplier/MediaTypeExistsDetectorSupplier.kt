package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.MediaTypeExistsDetector
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object MediaTypeExistsDetectorSupplier : ComponentSupplier<MediaTypeExistsDetector> {

    override fun apply(props: Properties): MediaTypeExistsDetector {
        return MediaTypeExistsDetector
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.itemExistsDetector("media-type")
        )
    }
}