package io.github.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.shoaky.sourcedownloader.component.SimpleFileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.ItemContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.FileExistsDetector
import io.github.shoaky.sourcedownloader.sdk.component.FileMover
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.name

data class CoreItemContent(
    override val sourceItem: SourceItem,
    @JsonAlias("sourceFiles")
    override val fileContents: List<CoreFileContent>,
    @JsonAlias("sharedPatternVariables")
    override val itemVariables: MapPatternVariables
) : ItemContent {

    private var updated: Boolean = false

    fun updateFileStatus(fileMover: FileMover, fileExistsDetector: FileExistsDetector) {
        if (updated) {
            return
        }

        val undetectedFiles = fileContents.filter { it.status == FileContentStatus.UNDETECTED }

        // Key 是当前处理的路径, Value是认为存在的路径
        val existsMapping: Map<Path, Path?> by lazy {
            val exists = fileMover.exists(undetectedFiles.map { it.targetPath() })
            val mapping = mutableMapOf<Path, Path?>()
            undetectedFiles.map { it.targetPath() }.zip(exists).forEach { (path, exist) ->
                mapping[path] = if (exist) path else null
            }

            if (log.isDebugEnabled) {
                log.debug("FileMover item:{}, exists: {}", sourceItem, mapping.filter { it.value != null })
            }

            if (fileExistsDetector !is SimpleFileExistsDetector) {
                // TODO 文件夹的情况没有处理
                fileExistsDetector.exists(fileMover, this).forEach { (path, existsPath) ->
                    // 如果fileMover认为已经存在Detector不能覆盖
                    mapping.computeIfAbsent(path) { existsPath }
                }
            }
            mapping
        }

        if (log.isDebugEnabled) {
            log.debug("Item:{}, existsMapping: {}", sourceItem.title, existsMapping)
        }

        val conflicts = fileContents.map { it.targetPath() }.groupingBy { it }.eachCount()
            .filter { it.value > 1 }.keys
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
        return fileContents.filter { it.status == FileContentStatus.NORMAL && it.fileDownloadPath != it.targetPath() }
    }

    fun downloadableFiles(): List<CoreFileContent> {
        if (updated.not()) {
            throw IllegalStateException("Please update file status first")
        }
        return fileContents.filter { it.status != FileContentStatus.TARGET_EXISTS && it.status != FileContentStatus.DOWNLOADED }
    }

    override fun summaryContent(): String {
        if (fileContents.size == 1 && fileContents.first().status.isSuccessful()) {
            val name = fileContents.first().targetPath().name
            return "$name 处理完成"
        }

        val hasWarning = fileContents.any { it.status.isWarning() }
        if (hasWarning) {
            val statusGrouping = fileContents.groupingBy { it.status }.eachCount()
            val statusSummary = statusGrouping.map { "${it.key.status()}:${it.value}个" }.joinToString(",")
            return StringBuilder()
                .append("${sourceItem.title}内的${fileContents.size}个文件处理完成 ")
                .append(statusSummary).toString()
        }
        return "${sourceItem.title}内的${fileContents.size}个文件处理完成"
    }

    companion object {

        private val log = LoggerFactory.getLogger(CoreItemContent::class.java)
    }
}