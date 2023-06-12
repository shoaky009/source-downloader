package io.github.shoaky.sourcedownloader.component.resolver

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import org.springframework.core.io.UrlResource
import kotlin.io.path.Path

object UrlFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val urlResource = UrlResource(sourceItem.downloadUri)
        val filename = urlResource.filename.takeIf { it.isNullOrBlank().not() }
                ?: sourceItem.hashing()

        val contentLength = urlResource.contentLength()
        val sourceFile = SourceFile(Path(filename), buildMap {
            if (contentLength > 0) {
                put("size", contentLength)
            }
        })
        return listOf(sourceFile)
    }
}