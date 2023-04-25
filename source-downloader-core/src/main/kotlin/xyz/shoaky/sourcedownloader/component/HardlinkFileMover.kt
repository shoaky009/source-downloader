package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Files

object HardlinkFileMover : FileMover {
    override fun rename(sourceContent: SourceContent): Boolean {
        sourceContent.sourceFiles.forEach {
            val targetFilePath = it.targetPath()
            Files.createLink(targetFilePath, it.fileDownloadPath)
        }
        return true
    }
}