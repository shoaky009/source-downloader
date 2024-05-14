package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.rss.JackettSource
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object JackettSourceSupplier : ComponentSupplier<JackettSource> {

    override fun apply(context: CoreContext, props: Properties): JackettSource {
        return JackettSource(props.get("url"))
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.source("jackett")
        )
    }

}