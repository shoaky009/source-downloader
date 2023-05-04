package xyz.shoaky.sourcedownloader.component.resolver

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Path

// TODO 后面改成灵活点的
object MultiFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val contentType = sourceItem.contentType
        if (contentType.contains("torrent")) {
            return TorrentFileResolver.resolveFiles(sourceItem)
        }
        return UrlFileResolver.resolveFiles(sourceItem)
    }
}