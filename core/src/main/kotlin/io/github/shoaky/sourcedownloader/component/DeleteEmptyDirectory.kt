package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener
import kotlin.io.path.*

/**
 * 如果SourceItem下载的文件夹为空，则删除
 */
object DeleteEmptyDirectory : ProcessListener {

    @OptIn(ExperimentalPathApi::class)
    override fun onItemSuccess(itemContent: ItemContent) {
        itemContent.sourceFiles.firstOrNull()?.run {
            val directoryPath = this.fileDownloadRootDirectory()
            if (directoryPath != null && directoryPath.walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .all { it.isDirectory() }
            ) {
                directoryPath.deleteRecursively()
            }
        }
    }
}