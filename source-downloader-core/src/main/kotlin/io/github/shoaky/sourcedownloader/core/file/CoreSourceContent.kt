package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceContent
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileMover

data class CoreSourceContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<CoreFileContent>,
    val sharedPatternVariables: MapPatternVariables
) : SourceContent {

    private var updated: Boolean = false

    init {
        sourceFiles.forEach {
            it.addSharedVariables(sharedPatternVariables)
        }
    }

    fun updateFileStatus(fileMover: FileMover) {
        if (updated) {
            return
        }

        val conflicts = sourceFiles.map { it.targetPath() }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys

        val undetectedFiles = sourceFiles.filter { it.status == FileContentStatus.UNDETECTED }
        for (sourceFile in undetectedFiles) {
            // 校验顺序不可换
            val filenameResult = sourceFile.filenamePattern.parse(sourceFile.currentVariables())
            if (filenameResult.success().not()) {
                sourceFile.status = FileContentStatus.VARIABLE_ERROR
                continue
            }

            val pathResult = sourceFile.fileSavePathPattern.parse(sourceFile.currentVariables())
            if (pathResult.success().not()) {
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