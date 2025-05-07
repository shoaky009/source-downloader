package io.github.shoaky.sourcedownloader.core

import io.github.shoaky.sourcedownloader.core.processor.ProcessingTargetPath
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import java.nio.file.Path

object NoProcessingStorage : ProcessingStorage {

    override fun save(content: ProcessingContent): ProcessingContent {
        return content
    }

    override fun save(state: ProcessorSourceState): ProcessorSourceState {
        return state
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return emptyList()
    }

    override fun deleteProcessingContent(id: Long) {
    }

    override fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent? {
        return null
    }

    override fun findByItemHashing(itemHashing: List<String>): List<ProcessingContent> {
        return emptyList()
    }

    override fun saveTargetPaths(targetPaths: List<ProcessingTargetPath>) {
    }

    override fun targetPathExists(paths: List<Path>, excludedItemHashing: String?): List<Boolean> {
        return emptyList()
    }

    override fun findById(id: Long): ProcessingContent? {
        return null
    }

    override fun findProcessorSourceState(processorName: String, sourceId: String): ProcessorSourceState? {
        return null
    }

    override fun findTargetPaths(paths: List<Path>): List<ProcessingTargetPath> {
        return emptyList()
    }

    override fun findSubPaths(path: Path): List<ProcessingTargetPath> {
        return listOf()
    }

    override fun deleteTargetPaths(paths: List<String>, hashing: String?) {
    }

    override fun queryAllContent(query: ProcessingQuery): List<ProcessingContent> {
        return emptyList()
    }

    override fun queryContents(query: ProcessingQuery, limit: Int, maxId: Long): List<ProcessingContent> {
        return emptyList()
    }

    override fun deleteProcessingContentByProcessorName(processorName: String): Int {
        return 0
    }

    override fun deleteTargetPathByProcessorName(processorName: String): Int {
        return 0
    }

    override fun existsByNameAndIdentify(processorName: String, identity: String): Boolean {
        return false
    }

    override fun existsByNameAndHash(processorName: String, itemHashing: String): Boolean {
        return false
    }
}