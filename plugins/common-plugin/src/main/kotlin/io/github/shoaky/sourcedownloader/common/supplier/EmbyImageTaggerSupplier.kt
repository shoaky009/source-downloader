package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.EmbyImageTagger
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object EmbyImageTaggerSupplier : ComponentSupplier<EmbyImageTagger> {

    override fun apply(context: CoreContext, props: Properties): EmbyImageTagger {
        return EmbyImageTagger
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileTagger("emby-image")
        )
    }

    override fun supportNoArgs(): Boolean = true
}