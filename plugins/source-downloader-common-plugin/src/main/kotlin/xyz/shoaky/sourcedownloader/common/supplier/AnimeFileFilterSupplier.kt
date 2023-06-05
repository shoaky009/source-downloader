package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.anime.AnimeFileFilter
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object AnimeFileFilterSupplier : SdComponentSupplier<AnimeFileFilter> {
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