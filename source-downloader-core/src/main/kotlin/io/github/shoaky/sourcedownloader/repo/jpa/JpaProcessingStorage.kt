package io.github.shoaky.sourcedownloader.repo.jpa

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorSourceState
import io.github.shoaky.sourcedownloader.core.processor.ProcessingTargetPath
import io.github.shoaky.sourcedownloader.util.fromValue
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.Path

@Component
class JpaProcessingStorage(
    private val processingRecordRepository: ProcessingRecordRepository,
    private val targetPathRepository: TargetPathRepository,
    private val processorSourceStateRepository: ProcessorSourceStateRepository,

    ) : ProcessingStorage {

    override fun save(content: ProcessingContent): ProcessingContent {
        val record = ProcessingRecord()
        record.processorName = content.processorName
        record.sourceItemHashing = content.sourceHash
        record.sourceContent = content.sourceContent
        record.renameTimes = content.renameTimes
        record.status = content.status.value
        record.failureReason = content.failureReason
        record.modifyTime = content.modifyTime
        record.createTime = content.createTime
        record.id = content.id
        val save = processingRecordRepository.save(record)
        content.id = save.id
        return content
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return processingRecordRepository.findByProcessorNameAndRenameTimesLessThan(name, renameTimesThreshold)
            .map { record ->
                val processingContent = ProcessingContent(
                    record.id,
                    record.processorName,
                    record.sourceItemHashing,
                    record.sourceContent,
                    record.renameTimes,
                    ProcessingContent.Status::class.fromValue(record.status),
                    record.failureReason,
                    record.modifyTime,
                    record.createTime
                )
                processingContent.id = record.id
                processingContent
            }
    }

    override fun deleteProcessingContent(id: Long) {
        processingRecordRepository.deleteById(id)
    }

    override fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent? {
        val record = processingRecordRepository.findByProcessorNameAndSourceItemHashing(processorName, itemHashing)
        return convert(record)
    }

    override fun findByItemHashing(itemHashing: List<String>): List<ProcessingContent> {
        return processingRecordRepository.findBySourceItemHashingIn(itemHashing)
            .mapNotNull { convert(it) }
    }

    private fun convert(record: ProcessingRecord?): ProcessingContent? {
        return record?.let {
            val processingContent = ProcessingContent(
                record.id,
                record.processorName,
                record.sourceItemHashing,
                record.sourceContent,
                record.renameTimes,
                ProcessingContent.Status::class.fromValue(record.status),
                record.failureReason,
                record.modifyTime,
                record.createTime
            )
            processingContent.id = record.id
            processingContent
        }
    }

    override fun saveTargetPath(targetPaths: List<ProcessingTargetPath>) {
        val now = LocalDateTime.now()
        val paths = targetPaths.map {
            val rc = TargetPathRecord()
            rc.id = it.targetPath.toString()
            rc.processorName = it.processorName
            rc.itemHashing = it.itemHashing
            rc.createTime = now
            rc
        }
        targetPathRepository.saveAll(paths)
    }

    override fun targetPathExists(paths: List<Path>): Boolean {
        val ids = paths.map { it.toString() }
        return targetPathRepository.existsAllByIdIn(ids)
    }

    override fun findById(id: Long): ProcessingContent? {
        return convert(processingRecordRepository.findByIdOrNull(id))
    }

    override fun findProcessorSourceState(processorName: String, sourceId: String): ProcessorSourceState? {
        return processorSourceStateRepository.findByProcessorNameAndSourceId(
            processorName, sourceId
        )?.let {
            ProcessorSourceState(
                it.id,
                it.processorName,
                it.sourceId,
                it.lastPointer,
                it.retryTimes,
                it.lastActiveTime
            )
        }
    }

    override fun save(state: ProcessorSourceState) {
        processorSourceStateRepository.save(state.let {
            val record = ProcessorSourceStateRecord()
            record.id = state.id
            record.processorName = state.processorName
            record.sourceId = state.sourceId
            record.lastPointer = state.lastPointer
            record.retryTimes = state.retryTimes
            record.lastActiveTime = state.lastActiveTime
            record
        })
    }

    override fun findTargetPaths(paths: List<Path>): List<ProcessingTargetPath> {
        val ids = paths.map { it.toString() }
        return targetPathRepository.findAllById(ids).map {
            ProcessingTargetPath(
                Path(it.id),
                it.processorName,
                it.itemHashing
            )
        }
    }

    override fun deleteTargetPath(paths: List<Path>) {
        val ids = paths.map { it.toString() }
        targetPathRepository.deleteAllByIdInBatch(ids)
    }
}