package io.github.shoaky.sourcedownloader.component.supplier

import io.github.shoaky.sourcedownloader.component.source.UriSource
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import java.net.URI

object UriSourceSupplier : ComponentSupplier<UriSource> {

    override fun apply(props: Properties): UriSource {
        val uri = props.get<URI>("uri")
        return UriSource(uri)
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("uri")
        )
    }
}