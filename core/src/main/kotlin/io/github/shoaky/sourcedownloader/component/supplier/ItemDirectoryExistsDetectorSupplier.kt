package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.ItemDirectoryExistsDetector
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object ItemDirectoryExistsDetectorSupplier : ComponentSupplier<ItemDirectoryExistsDetector> {

    override fun apply(props: Properties): ItemDirectoryExistsDetector {
        return ItemDirectoryExistsDetector
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.itemExistsDetector("item-dir")
        )
    }
}