package io.github.shoaky.sourcedownloader.component.resolver

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import java.net.URI
import kotlin.io.path.Path

/**
 * URL文件解析器，只会返回单个文件
 */
object UrlFileResolver : ItemFileResolver {
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val filename = getFilename(sourceItem.downloadUri).takeIf { it.isNullOrBlank().not() }
                ?: sourceItem.hashing()

        val sourceFile = SourceFile(Path(filename))
        return listOf(sourceFile)
    }

    private fun getFilename(uri: URI): String? {
        val path = uri.path ?: return null

        val separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR)
        return (if (separatorIndex != -1) path.substring(separatorIndex + 1) else path)
    }

    private const val FOLDER_SEPARATOR_CHAR: Char = '/'
}