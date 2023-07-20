package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import kotlin.io.path.moveTo

object GeneralFileMover : FileMover {

    override fun move(itemContent: ItemContent): Boolean {
        itemContent.sourceFiles
            .forEach {
                it.fileDownloadPath.moveTo(it.targetPath())
            }
        return true
    }

    override fun replace(itemContent: ItemContent): Boolean {
        itemContent.sourceFiles.forEach {
            it.fileDownloadPath.moveTo(it.targetPath(), true)
        }
        return true
    }
}

