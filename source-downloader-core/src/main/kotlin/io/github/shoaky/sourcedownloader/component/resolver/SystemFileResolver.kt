package io.github.shoaky.sourcedownloader.component.resolver

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.nio.file.Files
import kotlin.io.path.isDirectory
import kotlin.io.path.toPath

object SystemFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val path = sourceItem.downloadUri.toPath()
        if (path.isDirectory()) {
            return Files.list(path).sorted().map { SourceFile(it) }.toList()
        }
        return listOf(SourceFile(path))
    }
}