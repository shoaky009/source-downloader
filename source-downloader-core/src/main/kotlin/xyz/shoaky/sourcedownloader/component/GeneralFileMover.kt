package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.moveTo

object GeneralFileMover : FileMover {
    override fun rename(sourceContent: SourceContent): Boolean {
        val sourceFiles = sourceContent.sourceFiles
        sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath())
            }
        return true
    }
}

