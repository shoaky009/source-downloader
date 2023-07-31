package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover

data class CoreItemContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<CoreFileContent>,
    val sharedPatternVariables: MapPatternVariables
) : ItemContent {

    private var updated: Boolean = false


    fun updateFileStatus(fileMover: FileMover) {
        if (updated) {
            return
        }

        val conflicts = sourceFiles.map { it.targetPath() }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys

        val undetectedFiles = sourceFiles.filter { it.status == FileContentStatus.UNDETECTED }
        for (sourceFile in undetectedFiles) {
            // 校验顺序不可换
            if (sourceFile.errors.isNotEmpty()) {
                sourceFile.status = FileContentStatus.VARIABLE_ERROR
                continue
            }

            if (conflicts.contains(sourceFile.targetPath())) {
                sourceFile.status = FileContentStatus.FILE_CONFLICT
                continue
            }

            if (fileMover.exists(listOf(sourceFile.targetPath()))) {
                sourceFile.status = FileContentStatus.TARGET_EXISTS
                continue
            }
            sourceFile.status = FileContentStatus.NORMAL
        }
        updated = true
    }

    fun getMovableFiles(fileMover: FileMover): List<CoreFileContent> {
        updateFileStatus(fileMover)
        return sourceFiles.filter { it.status == FileContentStatus.NORMAL && it.fileDownloadPath != it.targetPath() }
    }

    fun getDownloadFiles(fileMover: FileMover): List<CoreFileContent> {
        updateFileStatus(fileMover)
        return sourceFiles.filter { it.status != FileContentStatus.TARGET_EXISTS }
    }
}