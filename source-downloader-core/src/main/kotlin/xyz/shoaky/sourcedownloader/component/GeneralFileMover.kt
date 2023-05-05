package xyz.shoaky.sourcedownloader.component

import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.moveTo

object GeneralFileMover : FileMover {
    override fun rename(sourceContent: SourceContent): Boolean {
        // NOTE 如果这里有目标文件一样的话？策略
        val sourceFiles = sourceContent.sourceFiles
        sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath())
            }
        return true
    }
}

