package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.moveTo

object GeneralFileMover : FileMover {
    override fun move(sourceContent: SourceContent): Boolean {
        sourceContent.sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath())
            }
        return true
    }

    override fun replace(sourceContent: SourceContent): Boolean {
        sourceContent.sourceFiles.forEach {
            it.fileDownloadPath.moveTo(it.targetPath(), true)
        }
        return true
    }
}

