package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sdk.component.ItemExistsDetector

/**
 * 全部TargetPath存在时则认为Item存在
 */
object SimpleItemExistsDetector : ItemExistsDetector {

    override fun exists(fileMover: FileMover, content: ItemContent): Boolean {
        val paths = content.sourceFiles.map { it.targetPath() }
        return fileMover.exists(paths)
    }
}