package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.core.PersistentPointer
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.json.json
import java.time.LocalDateTime

object ProcessorSourceStates : LongIdTable("processor_source_state_record") {

    val processorName = text("processor_name")
    val sourceId = text("source_id")
    val lastPointer =
        json("last_pointer", { Jackson.toJsonString(it) }, { Jackson.fromJson(it, PersistentPointer::class) })
    val retryTimes = integer("retry_times").default(0)
    val lastActiveTime = datetime("last_active_time").default(LocalDateTime.now())

    init {
        uniqueIndex("uidx_processorname_sourceid", processorName, sourceId)
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