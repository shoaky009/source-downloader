package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.moveTo

/**
 * 通过文件系统API移动文件
 */
object GeneralFileMover : FileMover {

    override fun move(itemContent: ItemContent): Boolean {
        itemContent.fileContents
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath())
            }
        return true
    }
}

