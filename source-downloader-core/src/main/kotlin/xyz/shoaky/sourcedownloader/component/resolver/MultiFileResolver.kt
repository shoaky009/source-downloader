package xyz.shoaky.sourcedownloader.component.resolver

import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Path

object MultiFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        TODO("Not yet implemented")
    }
}