package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.ProcessContext
import io.github.shoaky.sourcedownloader.sdk.component.ProcessListener
import java.nio.file.Files
import kotlin.io.path.*

/**
 * 如果SourceItem下载的文件夹为空，则删除
 */
object DeleteEmptyDirectory : ProcessListener {

    @OptIn(ExperimentalPathApi::class)
    override fun onItemSuccess(context: ProcessContext, itemContent: ItemContent) {
        if (singleFile(itemContent)) {
            val fileContent = itemContent.fileContents.first()
            val parent = fileContent.fileDownloadPath.parent
            if (parent == null || parent.notExists()) {
                return
            }

            val isEmpty = Files.newDirectoryStream(parent).use { it.none() }
            if (isEmpty) {
                parent.deleteIfExists()
            }
            return
        }

        itemContent.fileContents.firstOrNull()?.run {
            val directoryPath = this.fileDownloadRootDirectory()
            if (directoryPath != null && directoryPath.walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .all { it.isDirectory() }
            ) {
                directoryPath.deleteRecursively()
            }
        }
    }

    private fun singleFile(itemContent: ItemContent) = itemContent.fileContents.size == 1
}