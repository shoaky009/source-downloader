package xyz.shoaky.sourcedownloader.component.resolver

import org.springframework.core.io.UrlResource
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Path
import kotlin.io.path.Path

object UrlFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<Path> {
        val downloadUri = sourceItem.downloadUri
        val filename = UrlResource(downloadUri).filename.takeIf { it.isNullOrBlank().not() }
            ?: sourceItem.hashing()
        return listOf(Path(filename))
    }
}