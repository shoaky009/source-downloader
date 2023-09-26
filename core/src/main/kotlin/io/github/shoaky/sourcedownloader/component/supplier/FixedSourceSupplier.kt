package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.source.FixedSource
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object FixedSourceSupplier : ComponentSupplier<FixedSource> {

    override fun apply(props: Properties): FixedSource {
        return FixedSource(props.get("content"), props.getOrDefault("offset-mode", false))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("fixed"),
            ComponentType.fileResolver("fixed"),
        )
    }
}