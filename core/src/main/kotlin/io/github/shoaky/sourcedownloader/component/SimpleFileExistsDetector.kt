package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

/**
 * 全部TargetPath存在时则认为Item存在
 */
object SimpleFileExistsDetector : FileExistsDetector {

    override fun exists(fileMover: FileMover, content: ItemContent): Map<Path, Boolean> {
        val paths = content.sourceFiles.map { it.targetPath() }
        val exists = fileMover.exists(paths)
        return paths.zip(exists).toMap()
    }
}