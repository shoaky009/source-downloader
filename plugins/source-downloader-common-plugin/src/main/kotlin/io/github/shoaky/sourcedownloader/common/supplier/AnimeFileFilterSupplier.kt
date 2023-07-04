package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.anime.AnimeFileFilter
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object AnimeFileFilterSupplier : ComponentSupplier<AnimeFileFilter> {

    override fun apply(props: Properties): AnimeFileFilter {
        return AnimeFileFilter
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(
            ComponentType.fileFilter("anime")
        )
    }

    override fun autoCreateDefault(): Boolean = true
}