package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isSymbolicLink

/**
 * 通过文件系统API创建硬链接
 */
object HardlinkFileMover : FileMover {

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        val targetFilePath = file.targetPath()
        Files.createLink(targetFilePath, file.fileDownloadPath)
        return true
    }

    override fun replace(itemContent: ItemContent): Boolean {
        itemContent.fileContents.forEach {
            val targetFilePath = it.targetPath()
            if (targetFilePath.isSymbolicLink().not()) {
                log.warn("target file is not symbolic link: $targetFilePath will not be replaced")
                return@forEach
            }
            targetFilePath.deleteIfExists()
            Files.createLink(targetFilePath, it.fileDownloadPath)
        }
        return true
    }

    private val log = LoggerFactory.getLogger(HardlinkFileMover::class.java)
}