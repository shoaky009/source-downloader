package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sdk.component.ItemExistsDetector

/**
 * 通过文件系统API判断目标文件Item的顶级目录，如果存在则认为Item已经存在
 * 例如TargetPath:/mnt/anime/FATE/Season01/EP01.mp4
 * Item顶级目录:/mnt/anime/FATE
 */
object ItemDirectoryExistsDetector : ItemExistsDetector {

    override fun exists(fileMover: FileMover, content: ItemContent): Boolean {
        content.sourceFiles.mapNotNull { it.fileSaveRootDirectory() }
            .distinct()
            .forEach {
                if (fileMover.exists(listOf(it)).not()) {
                    return false
                }
            }
        return true
    }
}