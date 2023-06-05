package xyz.shoaky.sourcedownloader.common.supplier

import xyz.shoaky.sourcedownloader.common.torrent.TorrentFileResolver
import xyz.shoaky.sourcedownloader.sdk.Properties
import xyz.shoaky.sourcedownloader.sdk.component.ComponentType
import xyz.shoaky.sourcedownloader.sdk.component.SdComponentSupplier

object TorrentFileResolverSupplier : SdComponentSupplier<TorrentFileResolver> {
    override fun apply(props: Properties): TorrentFileResolver {
        return TorrentFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("torrent"))
    }

    override fun autoCreateDefault(): Boolean = true
}