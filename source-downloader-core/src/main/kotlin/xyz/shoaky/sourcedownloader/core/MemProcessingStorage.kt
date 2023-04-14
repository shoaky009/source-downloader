package xyz.shoaky.sourcedownloader.core

import java.nio.file.Path

object MemProcessingStorage : ProcessingStorage {

    private val contents = mutableListOf<ProcessingContent>()
    private val targetPaths = mutableSetOf<Path>()
    override fun save(content: ProcessingContent) {
        contents.add(content)
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return contents.filter {
            it.processorName == name && it.renameTimes < renameTimesThreshold
                && it.status == ProcessingContent.Status.WAITING_TO_RENAME
        }
    }

    override fun deleteById(id: Long) {
        contents.removeIf { it.id == id }
    }

    override fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent? {
        return contents.firstOrNull { it.processorName == processorName && it.sourceHash == itemHashing }
    }

    override fun saveTargetPath(paths: List<Path>) {
        targetPaths.addAll(paths)
    }

    override fun targetPathExists(paths: List<Path>): Boolean {
        return paths.all { targetPaths.contains(it) }
    }

}