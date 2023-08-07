package io.github.shoaky.sourcedownloader.component

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import io.github.shoaky.sourcedownloader.sdk.component.ItemExistsDetector

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