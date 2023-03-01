package xyz.shoaky.sourcedownloader.repo.jpa

import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.core.ProcessingContent
import xyz.shoaky.sourcedownloader.core.ProcessingStorage

@Component
class JpaProcessingStorage(private val repository: ProcessingRecordRepository) : ProcessingStorage {
    override fun saveRenameTask(content: ProcessingContent) {
        val record = ProcessingRecord()
        record.processorName = content.processorName
        record.sourceItemHashing = content.sourceHash
        record.sourceContent = content.sourceContent
        record.downloadTask = content.downloadTask
        record.renameTimes = content.renameTimes
        record.modifyTime = content.modifyTime
        record.createTime = content.createTime
        record.id = content.id

        println("saveRenameTask: $record")
        repository.save(record)
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return repository.findByProcessorNameAndRenameTimesLessThan(name, renameTimesThreshold)
            .map { record ->
                val processingContent = ProcessingContent(
                    record.processorName,
                    record.sourceItemHashing,
                    record.sourceContent,
                    record.downloadTask,
                    record.renameTimes,
                    record.modifyTime,
                    record.createTime
                )
                processingContent.id = record.id
                println("findRenameContent: $processingContent")
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
                record.downloadTask,
                record.renameTimes,
                record.modifyTime,
                record.createTime
            )
            processingContent.id = record.id
            println("findByNameAndHash: $processingContent")
            processingContent
        }
    }
}