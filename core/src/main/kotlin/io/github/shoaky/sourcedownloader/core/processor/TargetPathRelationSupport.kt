package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.file.CoreFileContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import java.nio.file.Path

class TargetPathRelationSupport(
    sourceItem: SourceItem,
    files: List<CoreFileContent>,
    processingStorage: ProcessingStorage
) {

    private val currentItemHashing: String = sourceItem.hashing()

    private val targetPaths = processingStorage.findTargetPaths(
        files.mapNotNull { it.existTargetPath }
    ).filter { it.itemHashing != currentItemHashing }

    private val contents = processingStorage.findByItemHashing(
        targetPaths.mapNotNull { it.itemHashing }.distinct()
    ).filter { it.status == ProcessingContent.Status.RENAMED }
        .groupBy { it.sourceHash }
        .mapValues { ent -> ent.value.maxBy { it.createTime } }

    private val targetPathMapping = targetPaths.filter { it.itemHashing != null }
        .associateBy { it.targetPath }

    fun getBeforeContent(targetPath: Path): ProcessingContent? {
        val processingTargetPath = targetPathMapping[targetPath] ?: return null
        val itemHashing = processingTargetPath.itemHashing ?: return null
        return contents[itemHashing]
    }
}