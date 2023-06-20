package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Files

object HardlinkFileMover : FileMover {
    override fun move(sourceContent: SourceContent): Boolean {
        sourceContent.sourceFiles.forEach {
            val targetFilePath = it.targetPath()
            Files.createLink(targetFilePath, it.fileDownloadPath)
        }
        return true
    }
}