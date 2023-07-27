package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.core.PersistentPointer
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ProcessorSourceStates : LongIdTable("processor_source_state_record") {

    val processorName = text("processor_name")
    val sourceId = text("source_id")
    val lastPointer = json<PersistentPointer>("last_pointer")
    val retryTimes = integer("retry_times").default(0)
    val lastActiveTime = datetime("last_active_time").default(LocalDateTime.now())

    init {
        uniqueIndex("uidx_sourceitemhashing_processorname", processorName, sourceId)
    }
}

class ProcessorSourceState(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ProcessorSourceState>(ProcessorSourceStates)

    var processorName by ProcessorSourceStates.processorName
    var sourceId by ProcessorSourceStates.sourceId
    var lastPointer by ProcessorSourceStates.lastPointer
    var retryTimes by ProcessorSourceStates.retryTimes
    var lastActiveTime by ProcessorSourceStates.lastActiveTime
}