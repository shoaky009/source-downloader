package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorSourceState
import io.github.shoaky.sourcedownloader.core.processor.ProcessingTargetPath
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.Path

typealias EProcessorSourceState = io.github.shoaky.sourcedownloader.repo.exposed.ProcessorSourceState

class ExposedProcessingStorage : ProcessingStorage {

    override fun save(content: ProcessingContent): ProcessingContent {
        return transaction {
            if (content.id != null) {
                Processings.update({ Processings.id eq content.id }) {
                    it[processorName] = content.processorName
                    it[itemHash] = content.itemHash
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
                    it[itemHash] = content.itemHash
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
                    itemHash = it.itemHash,
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
                Processings.processorName eq processorName and (Processings.itemHash eq itemHashing)
            }.firstOrNull()?.let {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    itemHash = it.itemHash,
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
        if (itemHashing.isEmpty()) {
            return emptyList()
        }
        return transaction {
            Processing.find {
                Processings.itemHash inList itemHashing
            }.map {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    itemHash = it.itemHash,
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

    override fun targetPathExists(paths: List<Path>, excludedItemHashing: String?): List<Boolean> {
        val ids = paths.map { it.toString() }
        return transaction {
            val existingIds = TargetPaths
                .selectAll().where {
                    TargetPaths.id inList ids and (TargetPaths.itemHashing neq excludedItemHashing)
                }
                .map { it[TargetPaths.id].value }
                .toSet()
            ids.map { existingIds.contains(it) }
        }
    }

    override fun findById(id: Long): ProcessingContent? {
        return transaction {
            Processing.findById(id)?.let {
                ProcessingContent(
                    id = it.id.value,
                    processorName = it.processorName,
                    itemHash = it.itemHash,
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
        if (paths.isEmpty()) {
            return emptyList()
        }
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

    override fun findSubPaths(path: Path): List<ProcessingTargetPath> {
        return transaction {
            TargetPath.find {
                TargetPaths.id glob "$path*"
            }.map {
                ProcessingTargetPath(
                    processorName = it.processorName,
                    itemHashing = it.itemHashing,
                    targetPath = Path(it.id.value),
                )
            }
        }
    }

    override fun deleteTargetPaths(paths: List<String>, hashing: String?) {
        // TODO 只删除是该hashing下的paths，或强制删除
        val (pathPrefixes, ids) = paths.partition { it.endsWith("*") }
        if (ids.isNotEmpty()) {
            transaction {
                TargetPaths.deleteWhere {
                    id inList ids
                }
            }
        }
        if (pathPrefixes.isNotEmpty()) {
            transaction {
                pathPrefixes.forEach { prefix ->
                    TargetPaths.deleteWhere {
                        id glob prefix
                    }
                }
            }
        }
    }

    override fun queryAllContent(query: ProcessingQuery): List<ProcessingContent> {
        return transaction {
            val builder = Processings.selectAll()
            builder.applyConditions(query)
            builder
                .map {
                    ProcessingContent(
                        id = it[Processings.id].value,
                        processorName = it[Processings.processorName],
                        itemHash = it[Processings.itemHash],
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

    override fun queryContents(query: ProcessingQuery, limit: Int, maxId: Long): List<ProcessingContent> {
        return transaction {
            val builder = Processings.selectAll()
            builder.applyConditions(query)
            if (maxId > 0) builder.andWhere { Processings.id less maxId }
            builder.orderBy(Processings.id, SortOrder.DESC)
                .limit(limit)
                .map {
                    ProcessingContent(
                        id = it[Processings.id].value,
                        processorName = it[Processings.processorName],
                        itemHash = it[Processings.itemHash],
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

    private fun Query.applyConditions(query: ProcessingQuery) {
        if (query.id != null) this.andWhere { Processings.id inList query.id }
        if (query.processorName != null) this.andWhere { Processings.processorName inList query.processorName }
        if (query.status != null) this.andWhere { Processings.status inList query.status }
        if (query.itemHash != null) this.andWhere { Processings.itemHash eq query.itemHash }
        if (query.item?.title != null) {
            this.andWhere { Processings.itemContent.extract<String>(".sourceItem.title") glob "*${query.item.title}*" }
        }
        if (query.item?.attrs != null) {
            query.item.attrs.forEach { (key, value) ->
                this.andWhere { Processings.itemContent.extract<String>(".sourceItem.attrs.$key") eq value }
            }
        }
        if (query.item?.variables != null) {
            query.item.variables.forEach { (key, value) ->
                this.andWhere { Processings.itemContent.extract<String>(".itemVariables.$key") eq value }
            }
        }
        if (query.item?.contentType != null) {
            this.andWhere { Processings.itemContent.extract<String>(".sourceItem.contentType") eq query.item.contentType }
        }
        if (query.createTime.begin != null) this.andWhere { Processings.createTime greaterEq query.createTime.begin }
        if (query.createTime.end != null) this.andWhere { Processings.createTime lessEq query.createTime.end }
    }
}

class GlobOp(
    private val expr1: Expression<*>,
    private val pattern: String
) : Op<Boolean>() {

    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(expr1, " GLOB ", "\"$pattern\"")
    }
}

infix fun <S1> Expression<in S1>.glob(pattern: String): Op<Boolean> {
    return GlobOp(this, pattern)
}

fun Query.andWhere(andPart: SqlExpressionBuilder.() -> Op<Boolean>) = adjustWhere {
    val expr = Op.build { andPart() }
    if (this == null) expr
    else this and expr
}