package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.AnimeReplacementDecider
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object AnimeReplacementDeciderSupplier : ComponentSupplier<AnimeReplacementDecider> {

    override fun apply(context: CoreContext, props: Properties): AnimeReplacementDecider {
        return AnimeReplacementDecider
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileReplacementDecider("anime")
        )
    }

    override fun supportNoArgs(): Boolean = true
}