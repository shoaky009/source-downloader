package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.FileDirectoryExistsDetector
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object ItemDirectoryExistsDetectorSupplier : ComponentSupplier<FileDirectoryExistsDetector> {

    override fun apply(props: Properties): FileDirectoryExistsDetector {
        return FileDirectoryExistsDetector
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.itemExistsDetector("item-dir")
        )
    }
}