package io.github.shoaky.sourcedownloader.repo.exposed

import io.github.shoaky.sourcedownloader.core.ProcessingContent.Status
import io.github.shoaky.sourcedownloader.core.file.CoreItemContent
import io.github.shoaky.sourcedownloader.sdk.MapPatternVariables
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.util.Jackson
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.json.json
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.OffsetDateTime

object Processings : LongIdTable("processing_record") {

    private val fallbackItemContent = CoreItemContent(
        SourceItem("unknown", URI("unknown"), OffsetDateTime.MIN, "unknown", URI("unknown")),
        emptyList(),
        MapPatternVariables()
    )
    private val log = LoggerFactory.getLogger(Processings::class.java)

    val processorName = varchar("processor_name", 64)
    val itemHash = varchar("item_hash", 64)
    val itemIdentity = varchar("item_identity", 256).nullable()
    val itemContent = json("item_content", { Jackson.toJsonString(it) }, {
        try {
            Jackson.fromJson(it, CoreItemContent::class)
        } catch (e: Exception) {
            log.debug("Failed to parse item content, using fallback content", e)
            fallbackItemContent
        }
    })
    val renameTimes = integer("rename_times").default(0)
    val status = enum<Status, Int>("status")
    val failureReason = text("failure_reason").nullable()
    val modifyTime = datetime("modify_time").nullable()
    val createTime = datetime("create_time")

    init {
        uniqueIndex("uidx_processorname_sourceid", itemHash, processorName)
    }
}

class Processing(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Processing>(Processings)

    var processorName by Processings.processorName
    var itemHash by Processings.itemHash
    var itemIdentity by Processings.itemIdentity
    var itemContent by Processings.itemContent
    var renameTimes by Processings.renameTimes
    var status by Processings.status
    var failureReason by Processings.failureReason
    var modifyTime by Processings.modifyTime
    var createTime by Processings.createTime

}