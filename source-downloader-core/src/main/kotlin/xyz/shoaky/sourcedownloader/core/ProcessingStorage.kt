package xyz.shoaky.sourcedownloader.core

import java.nio.file.Path
import java.time.LocalDateTime

interface ProcessingStorage {

    fun save(content: ProcessingContent)

    fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent>
    fun deleteById(id: Long)
    fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent?

    fun saveTargetPath(paths: List<Path>)

    fun targetPathExists(paths: List<Path>): Boolean

    fun clean(date: LocalDateTime)
}