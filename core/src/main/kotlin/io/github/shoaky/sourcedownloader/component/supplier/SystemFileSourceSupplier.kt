package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.source.SystemFileSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object SystemFileSourceSupplier : ComponentSupplier<SystemFileSource> {

    override fun apply(context: CoreContext, props: Properties): SystemFileSource {
        return SystemFileSource(props.get("path"),
            props.getOrDefault("mode", 0)
        )
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("system-file"),
            ComponentType.downloader("system-file"),
        )
    }

}