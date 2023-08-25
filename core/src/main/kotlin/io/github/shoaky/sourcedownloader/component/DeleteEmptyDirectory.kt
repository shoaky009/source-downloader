package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.RunAfterCompletion
import kotlin.io.path.*

/**
 * 如果SourceItem下载的文件夹为空，则删除
 */
object DeleteEmptyDirectory : RunAfterCompletion {

    @OptIn(ExperimentalPathApi::class)
    override fun accept(t: ItemContent) {
        t.sourceFiles.firstOrNull()?.run {
            val directoryPath = this.fileDownloadRootDirectory()
            if (directoryPath != null && directoryPath.walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .all { it.isDirectory() }
            ) {
                directoryPath.deleteRecursively()
            }
        }
    }
}