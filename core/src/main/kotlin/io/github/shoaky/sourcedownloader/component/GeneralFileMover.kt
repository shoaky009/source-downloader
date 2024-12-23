package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.FileContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.moveTo

/**
 * 通过文件系统API移动文件
 */
object GeneralFileMover : FileMover {

    override fun move(sourceItem: SourceItem, file: FileContent): Boolean {
        file.fileDownloadPath.moveTo(file.targetPath())
        return true
    }

}
