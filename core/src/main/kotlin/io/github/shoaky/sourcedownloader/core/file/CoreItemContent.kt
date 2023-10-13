package io.github.shoaky.sourcedownloader.core.file

import io.github.shoaky.sourcedownloader.component.SimpleFileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import java.nio.file.Path

data class CoreItemContent(
    override val sourceItem: SourceItem,
    override val sourceFiles: List<CoreFileContent>,
    val sharedPatternVariables: MapPatternVariables
) : ItemContent {

    private var updated: Boolean = false

    fun updateFileStatus(fileMover: FileMover, fileExistsDetector: FileExistsDetector) {
        if (updated) {
            return
        }

        val conflicts = sourceFiles.map { it.targetPath() }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys

        val undetectedFiles = sourceFiles.filter { it.status == FileContentStatus.UNDETECTED }

        // Key 是当前处理的路径, Value是认为存在的路径
        val existsMapping: Map<Path, Path?> by lazy {
            val exists = fileMover.exists(undetectedFiles.map { it.targetPath() })
            val mapping = mutableMapOf<Path, Path?>()
            undetectedFiles.map { it.targetPath() }.zip(exists).forEach { (path, exist) ->
                mapping[path] = if (exist) path else null
            }

            if (fileExistsDetector !is SimpleFileExistsDetector) {
                // TODO 文件夹的情况没有处理
                fileExistsDetector.exists(fileMover, this).forEach { (path, existsPath) ->
                    mapping[path] = existsPath
                }
            }
            mapping
        }

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

            val existsPath = existsMapping[sourceFile.targetPath()]
            if (existsPath != null) {
                sourceFile.status = FileContentStatus.TARGET_EXISTS
                sourceFile.existTargetPath = existsPath
                continue
            }
            sourceFile.status = FileContentStatus.NORMAL
        }
        updated = true
    }

    fun movableFiles(): List<CoreFileContent> {
        if (updated.not()) {
            throw IllegalStateException("Please update file status first")
        }
        return sourceFiles.filter { it.status == FileContentStatus.NORMAL && it.fileDownloadPath != it.targetPath() }
    }

    fun downloadableFiles(): List<CoreFileContent> {
        if (updated.not()) {
            throw IllegalStateException("Please update file status first")
        }
        return sourceFiles.filter { it.status != FileContentStatus.TARGET_EXISTS }
    }
}