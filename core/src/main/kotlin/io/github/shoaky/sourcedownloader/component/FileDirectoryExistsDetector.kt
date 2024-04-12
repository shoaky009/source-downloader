package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

/**
 * 通过文件系统API判断目标文件Item的顶级目录，如果存在则认为Item已经存在
 * 例如TargetPath:/mnt/anime/FATE/Season01/EP01.mp4
 * Item顶级目录:/mnt/anime/FATE
 */
object FileDirectoryExistsDetector : FileExistsDetector {

    override fun exists(fileMover: FileMover, content: ItemContent): Map<Path, Path?> {
        val dirs = content.fileContents.mapNotNull { it.fileSaveRootDirectory() }.distinct()
        val exists = dirs.zip(fileMover.exists(dirs)).toMap()
        return content.fileContents.associate {
            val dir = it.fileSaveRootDirectory()
            it.targetPath() to if (exists[dir] == true) dir else null
        }
    }
}