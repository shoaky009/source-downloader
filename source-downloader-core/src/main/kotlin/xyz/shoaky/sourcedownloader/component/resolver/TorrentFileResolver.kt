package xyz.shoaky.sourcedownloader.component.resolver

import bt.metainfo.MetadataService
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Path
import kotlin.io.path.Path

object TorrentFileResolver : ItemFileResolver {

    private val metadataService = MetadataService()
    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val torrent = metadataService.fromUrl(sourceItem.downloadUri.toURL())
        if (torrent.files.size == 1) {
            return torrent.files.map { it.pathElements.joinToString("/") }
                .map { Path(it) }
        }
        val parent = Path(torrent.name)
        return torrent.files
            .filter { it.size > 0 }
            .map { it.pathElements.joinToString("/") }
            .map { parent.resolve(it) }
    }

}