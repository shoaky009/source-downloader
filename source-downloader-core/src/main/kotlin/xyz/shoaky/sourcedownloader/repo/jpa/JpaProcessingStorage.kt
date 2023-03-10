package xyz.shoaky.sourcedownloader.repo.jpa

import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.core.ProcessingContent
import xyz.shoaky.sourcedownloader.core.ProcessingStorage
import xyz.shoaky.sourcedownloader.util.fromValue
import java.nio.file.Path
import java.time.LocalDateTime

@Component
class JpaProcessingStorage(
    private val repository: ProcessingRecordRepository,
    private val tr: TargetPathRepository
) : ProcessingStorage {
    override fun save(content: ProcessingContent) {
        val record = ProcessingRecord()
        record.processorName = content.processorName
        record.sourceItemHashing = content.sourceHash
        record.sourceContent = content.sourceContent
        record.renameTimes = content.renameTimes
        record.status = content.status.value
        record.modifyTime = content.modifyTime
        record.createTime = content.createTime
        record.id = content.id
        repository.save(record)
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return repository.findByProcessorNameAndRenameTimesLessThan(name, renameTimesThreshold)
            .map { record ->
                val processingContent = ProcessingContent(
                    record.processorName,
                    record.sourceItemHashing,
                    record.sourceContent,
                    record.renameTimes,
                    ProcessingContent.Status::class.fromValue(record.status),
                    record.modifyTime,
                    record.createTime
                )
                processingContent.id = record.id
                processingContent
            }
    }

    override fun deleteById(id: Long) {
        repository.deleteById(id)
    }

    override fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent? {
        val record = repository.findByProcessorNameAndSourceItemHashing(processorName, itemHashing)
        return record?.let {
            val processingContent = ProcessingContent(
                record.processorName,
                record.sourceItemHashing,
                record.sourceContent,
                record.renameTimes,
                ProcessingContent.Status::class.fromValue(record.status),
                record.modifyTime,
                record.createTime
            )
            processingContent.id = record.id
            processingContent
        }
    }

    override fun saveTargetPath(paths: List<Path>) {
        val now = LocalDateTime.now()
        val map = paths.map {
            val rc = TargetPathRecord()
            rc.id = it.toString()
            rc.createTime = now
            rc
        }
        tr.saveAll(map)
    }

    override fun targetPathExists(paths: List<Path>): Boolean {
        val ids = paths.map { it.toString() }
        return tr.existsAllByIdIn(ids)
    }

    override fun clean(date: LocalDateTime) {
        TODO("Not yet implemented")
    }
}