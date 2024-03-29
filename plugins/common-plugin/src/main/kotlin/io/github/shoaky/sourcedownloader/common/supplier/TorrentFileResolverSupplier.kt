package io.github.shoaky.sourcedownloader.common.supplier

import io.github.shoaky.sourcedownloader.common.torrent.TorrentFileResolver
import io.github.shoaky.sourcedownloader.sdk.CoreContext
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType

object TorrentFileResolverSupplier : ComponentSupplier<TorrentFileResolver> {

    override fun apply(context: CoreContext, props: Properties): TorrentFileResolver {
        return TorrentFileResolver
    }

    override fun supplyTypes(): List<ComponentType> {
        return listOf(ComponentType.fileResolver("torrent"))
    }

    override fun supportNoArgs(): Boolean = true
}