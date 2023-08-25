package io.github.shoaky.sourcedownloader.repo.exposed

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime

object TargetPaths : IdTable<String>("target_path_record") {
    override val id: Column<EntityID<String>> = text("id").entityId()
    val processorName = varchar("processor_name", 64).nullable()
    val itemHashing = varchar("item_hashing", 64).nullable()
    val createTime = datetime("create_time")
}

class TargetPath(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, TargetPath>(TargetPaths)

    var processorName by TargetPaths.processorName
    var itemHashing by TargetPaths.itemHashing
    var createTime by TargetPaths.createTime
}