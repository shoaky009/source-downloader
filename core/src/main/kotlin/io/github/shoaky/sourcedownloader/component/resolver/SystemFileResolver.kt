package io.github.shoaky.sourcedownloader.component.resolver

import io.github.shoaky.sourcedownloader.sdk.SourceFile
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ItemFileResolver
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory
import kotlin.io.path.toPath
import kotlin.io.path.walk

/**
 * SourceItem如果是文件夹，则解析文件夹下的所有文件
 * SourceItem本身就是文件，则解析自身为单个文件
 */
object SystemFileResolver : ItemFileResolver {
    
    override fun resolveFiles(sourceItem: SourceItem): List<SourceFile> {
        val path = sourceItem.downloadUri.toPath()
        if (path.isDirectory()) {
            return path.walk().sorted().map { SourceFile(it) }.toList()
        }
        return listOf(SourceFile(path))
    }
}