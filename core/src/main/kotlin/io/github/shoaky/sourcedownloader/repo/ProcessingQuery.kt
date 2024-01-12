package io.github.shoaky.sourcedownloader.repo

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.repo.exposed.Processings
import io.github.shoaky.sourcedownloader.util.fromValue
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere

class ProcessingQuery(
    val processorName: String? = null,
    val itemHash: String? = null,
    val status: Int? = null,
) {

    fun apply(query: Query) {
        processorName?.apply {
            query.andWhere { Processings.processorName eq processorName }
        }
        itemHash?.apply {
            query.andWhere { Processings.itemHash eq itemHash }
        }
        status?.apply {
            val fromValue = ProcessingContent.Status::class.fromValue(status)
            query.andWhere { Processings.status eq fromValue }
        }
    }
}