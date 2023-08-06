package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorSourceState
import io.github.shoaky.sourcedownloader.core.processor.ProcessingTargetPath
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.Path

typealias EProcessorSourceState = io.github.shoaky.sourcedownloader.repo.exposed.ProcessorSourceState

@Component
class ExposedProcessingStorage : ProcessingStorage {

    override fun save(content: ProcessingContent): ProcessingContent {
        return transaction {
            if (content.id != null) {
                Processings.update({ Processings.id eq content.id }) {
                    it[processorName] = content.processorName
                    it[sourceItemHashing] = content.sourceHash
                    it[itemContent] = content.itemContent
                    it[renameTimes] = content.renameTimes
                    it[status] = content.status
                    it[failureReason] = content.failureReason
                    it[modifyTime] = content.modifyTime
                }
                content
            } else {
                val id = Processings.insertAndGetId {
                    it[processorName] = content.processorName
                    it[sourceItemHashing] = content.sourceHash
                    it[itemContent] = content.itemContent
                    it[renameTimes] = content.renameTimes
                    it[status] = content.status
                    it[failureReason] = content.failureReason
                    it[modifyTime] = content.modifyTime
                    it[createTime] = content.createTime
                }
                content.copy(id = id.value)
            }
            // NOTE upsert id会返回0, 暂时不能用返回的
//            val id = Processings.upsert(Processings.id) {
//                if (content.id != null) {
//                    it[id] = DaoEntityID(content.id, Processings)
//                }
//                it[processorName] = content.processorName
//                it[sourceItemHashing] = content.sourceHash
//                it[itemContent] = content.itemContent
//                it[renameTimes] = content.renameTimes
//                it[status] = content.status
//                it[failureReason] = content.failureReason
//                it[modifyTime] = content.modifyTime
//                it[createTime] = content.createTime
//            } get Processings.id
//            content.copy(id = id.value)
        }
    }

    override fun save(state: ProcessorSourceState): ProcessorSourceState {
        return transaction {
            if (state.id != null) {
                ProcessorSourceStates.update({ ProcessorSourceStates.id eq state.id }) {
                    it[processorName] = state.processorName
                    it[sourceId] = state.sourceId
                    it[lastPointer] = state.lastPointer
                    it[retryTimes] = state.retryTimes
                    it[lastActiveTime] = state.lastActiveTime
                }
                state
            } else {
                val id = ProcessorSourceStates.insertAndGetId {
                    it[processorName] = state.processorName
                    it[sourceId] = state.sourceId
                    it[lastPointer] = state.lastPointer
                    it[retryTimes] = state.retryTimes
                    it[lastActiveTime] = state.lastActiveTime
                }
                state.copy(id = id.value)
            }
            // NOTE upsert id会返回0, 暂时不能用返回的
//            val id = ProcessorSourceStates.upsert(ProcessorSourceStates.id) {
//                if (state.id != null) {
//                    it[id] = DaoEntityID(state.id, ProcessorSourceStates)
//                }
//                it[processorName] = state.processorName
//                it[sourceId] = state.sourceId
//                it[lastPointer] = state.lastPointer
//                it[retryTimes] = state.retryTimes
//                it[lastActiveTime] = state.lastActiveTime
//            } get ProcessorSourceStates.id
        }
    }

    override fun findRenameContent(name: String, renameTimesThreshold: Int): List<ProcessingContent> {
        return transaction {
            Processing.find {
                Processings.processorName eq name and Processings.status.eq(ProcessingContent.Status.WAITING_TO_RENAME) and (Processings.renameTimes less renameTimesThreshold)
            }.map {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    sourceHash = it.sourceItemHashing,
                    itemContent = it.itemContent,
                    renameTimes = it.renameTimes,
                    status = it.status,
                    failureReason = it.failureReason,
                    modifyTime = it.modifyTime,
                    createTime = it.createTime
                )
            }
        }
    }

    override fun deleteProcessingContent(id: Long) {
        transaction {
            Processings.deleteWhere { Processings.id eq id }
        }
    }

    override fun findByNameAndHash(processorName: String, itemHashing: String): ProcessingContent? {
        return transaction {
            Processing.find {
                Processings.processorName eq processorName and (Processings.sourceItemHashing eq itemHashing)
            }.firstOrNull()?.let {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    sourceHash = it.sourceItemHashing,
                    itemContent = it.itemContent,
                    renameTimes = it.renameTimes,
                    status = it.status,
                    failureReason = it.failureReason,
                    modifyTime = it.modifyTime,
                    createTime = it.createTime
                )
            }
        }
    }

    override fun findByItemHashing(itemHashing: List<String>): List<ProcessingContent> {
        return transaction {
            Processing.find {
                Processings.sourceItemHashing inList itemHashing
            }.map {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    sourceHash = it.sourceItemHashing,
                    itemContent = it.itemContent,
                    renameTimes = it.renameTimes,
                    status = it.status,
                    failureReason = it.failureReason,
                    modifyTime = it.modifyTime,
                    createTime = it.createTime
                )
            }
        }
    }

    override fun saveTargetPaths(targetPaths: List<ProcessingTargetPath>) {
        transaction {
            val now = LocalDateTime.now()
            TargetPaths.batchUpsert(targetPaths, TargetPaths.id) { path ->
                this[TargetPaths.id] = path.targetPath.toString()
                this[TargetPaths.processorName] = path.processorName
                this[TargetPaths.itemHashing] = path.itemHashing
                this[TargetPaths.createTime] = now
            }
        }
    }

    override fun targetPathExists(paths: List<Path>): Boolean {
        return transaction {
            TargetPaths.select {
                TargetPaths.id inList paths.map { it.toString() }
            }.adjustSlice { slice(TargetPaths.id) }.limit(1).count() > 0
        }
    }

    override fun findById(id: Long): ProcessingContent? {
        return transaction {
            Processing.findById(id)?.let {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    sourceHash = it.sourceItemHashing,
                    itemContent = it.itemContent,
                    renameTimes = it.renameTimes,
                    status = it.status,
                    failureReason = it.failureReason,
                    modifyTime = it.modifyTime,
                    createTime = it.createTime
                )
            }
        }
    }

    override fun findProcessorSourceState(processorName: String, sourceId: String): ProcessorSourceState? {
        return transaction {
            EProcessorSourceState.find {
                ProcessorSourceStates.processorName eq processorName and (ProcessorSourceStates.sourceId eq sourceId)
            }.firstOrNull()?.let {
                ProcessorSourceState(
                    id = it.id.value,
                    processorName = it.processorName,
                    sourceId = it.sourceId,
                    lastPointer = it.lastPointer,
                    retryTimes = it.retryTimes,
                    lastActiveTime = it.lastActiveTime
                )
            }
        }
    }

    override fun findTargetPaths(paths: List<Path>): List<ProcessingTargetPath> {
        return transaction {
            TargetPath.find {
                TargetPaths.id inList paths.map { it.toString() }
            }.map {
                ProcessingTargetPath(
                    processorName = it.processorName,
                    itemHashing = it.itemHashing,
                    targetPath = Path(it.id.value),
                )
            }
        }
    }

    override fun deleteTargetPath(paths: List<Path>) {
        transaction {
            TargetPaths.deleteWhere {
                id inList paths.map { it.toString() }
            }
        }
    }

    override fun query(query: ProcessingQuery): List<ProcessingContent> {
        return transaction {
            val builder = Processings.selectAll()
            query.apply(builder)
            builder
                .map {
                    ProcessingContent(
                        id = it[Processings.id].value,
                        processorName = it[Processings.processorName],
                        sourceHash = it[Processings.sourceItemHashing],
                        itemContent = it[Processings.itemContent],
                        renameTimes = it[Processings.renameTimes],
                        status = it[Processings.status],
                        failureReason = it[Processings.failureReason],
                        modifyTime = it[Processings.modifyTime],
                        createTime = it[Processings.createTime]
                    )
                }
        }
    }
}