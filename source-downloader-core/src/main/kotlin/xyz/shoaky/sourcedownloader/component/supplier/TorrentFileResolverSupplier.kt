package xyz.shoaky.sourcedownloader.component.supplier

import xyz.shoaky.sourcedownloader.component.resolver.TorrentFileResolver
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.SdComponentSupplier
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType

object TorrentFileResolverSupplier : SdComponentSupplier<TorrentFileResolver> {
    override fun apply(props: Properties): TorrentFileResolver {
        return TorrentFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("torrent"))
    }

    override fun autoCreateDefault(): Boolean = true
}