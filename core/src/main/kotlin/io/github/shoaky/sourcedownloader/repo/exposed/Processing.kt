package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.core.ProcessingContent.Status
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Processings : LongIdTable("processing_record") {

    val processorName = varchar("processor_name", 64)
    val sourceItemHashing = varchar("source_item_hashing", 64)
    val itemContent = json<CoreItemContent>("item_content")
    val renameTimes = integer("rename_times").default(0)
    val status = enum<Status, Int>("status")
    val failureReason = text("failure_reason").nullable()
    val modifyTime = datetime("modify_time").nullable()
    val createTime = datetime("create_time")

    init {
        uniqueIndex("uidx_processorname_sourceid", sourceItemHashing, processorName)
    }
}

class Processing(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Processing>(Processings)

    var processorName by Processings.processorName
    var sourceItemHashing by Processings.sourceItemHashing
    var itemContent by Processings.itemContent
    var renameTimes by Processings.renameTimes
    var status by Processings.status
    var failureReason by Processings.failureReason
    var modifyTime by Processings.modifyTime
    var createTime by Processings.createTime

}