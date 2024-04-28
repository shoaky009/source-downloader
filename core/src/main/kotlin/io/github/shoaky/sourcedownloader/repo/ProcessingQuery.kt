package io.github.shoaky.sourcedownloader.repo

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.repo.exposed.Processings
import io.github.shoaky.sourcedownloader.repo.exposed.glob
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.json.extract
import java.time.LocalDateTime

data class ProcessingQuery(
    val processorName: List<String>? = null,
    val status: List<ProcessingContent.Status>? = null,
    val id: List<Long>? = null,
    val itemHash: String? = null,
    val itemTitle: String? = null,
    val createTime: RangeCondition<LocalDateTime> = RangeCondition()
) {

    constructor(processorName: String?) : this(processorName?.let { listOf(it) })

    fun apply(query: Query) {
        processorName?.apply {
            query.andWhere { Processings.processorName inList processorName }
        }
        itemHash?.apply {
            query.andWhere { Processings.itemHash eq itemHash }
        }
        status?.apply {
            query.andWhere { Processings.status inList status }
        }
        id?.apply {
            query.andWhere { Processings.id inList id }
        }
        itemTitle?.apply {
            query.andWhere { Processings.itemContent.extract<String>(".sourceItem.title") glob "*$itemTitle*" }
        }
        createTime.apply {
            begin?.apply {
                query.andWhere { Processings.createTime greaterEq begin }
            }
            end?.apply {
                query.andWhere { Processings.createTime lessEq end }
            }
        }
    }
}

data class RangeCondition<T : Comparable<*>>(
    val begin: T? = null,
    val end: T? = null
)