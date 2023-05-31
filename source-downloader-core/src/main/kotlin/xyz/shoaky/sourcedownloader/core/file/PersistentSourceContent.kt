package xyz.shoaky.sourcedownloader.core.file

import xyz.shoaky.sourcedownloader.sdk.MapPatternVariables
import xyz.shoaky.sourcedownloader.sdk.SourceContent
import xyz.shoaky.sourcedownloader.sdk.SourceItem
import xyz.shoaky.sourcedownloader.sdk.component.FileMover

data class PersistentSourceContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<CoreFileContent>,
    val sharedPatternVariables: MapPatternVariables
) : SourceContent {

    private var updated: Boolean = false

    fun updateFileStatus(fileMover: FileMover) {
        val conflicts = sourceFiles.map { it.targetPath() }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys

        for (sourceFile in sourceFiles) {
            if (fileMover.exists(listOf(sourceFile.targetPath()))) {
                sourceFile.status = FileContentStatus.TARGET_EXISTS
                continue
            }

            if (conflicts.contains(sourceFile.targetPath())) {
                sourceFile.status = FileContentStatus.FILE_CONFLICT
                continue
            }

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
        }
    }

    fun getRenameFiles(fileMover: FileMover): List<CoreFileContent> {
        if (!updated) {
            updateFileStatus(fileMover)
            updated = true
        }
        return sourceFiles.filter { it.status == FileContentStatus.NORMAL && it.fileDownloadPath != it.targetPath() }
    }

    fun getDownloadFiles(fileMover: FileMover): List<CoreFileContent> {
        if (!updated) {
            updateFileStatus(fileMover)
            updated = true
        }
        return sourceFiles.filter { it.status != FileContentStatus.TARGET_EXISTS }
    }
}