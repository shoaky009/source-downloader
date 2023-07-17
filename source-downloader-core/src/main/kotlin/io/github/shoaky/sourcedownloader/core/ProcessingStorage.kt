package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.processor.ProcessingTargetPath
import java.nio.file.Path

interface ProcessingStorage {

    fun save(content: ProcessingContent): ProcessingContent

    fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent>

    fun deleteProcessingContent(id: Long)

    fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent?

    /**
     * 需要从targetPath获取到[ProcessingContent]，从而目标文件存在时提供上下文信息做出决策
     */
    fun saveTargetPath(paths: ProcessingTargetPath)

    fun targetPathExists(paths: List<Path>): Boolean

    fun findById(id: Long): ProcessingContent?

    fun findProcessorSourceState(processorName: String, sourceId: String): ProcessorSourceState?

    fun save(state: ProcessorSourceState)

    fun findTargetPath(path: Path): ProcessingTargetPath?

    fun deleteTargetPath(paths: List<Path>)
}