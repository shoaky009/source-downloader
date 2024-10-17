package io.github.shoaky.sourcedownloader.repo

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import java.time.LocalDateTime

data class ProcessingQuery(
    val processorName: List<String>? = null,
    val status: List<ProcessingContent.Status>? = null,
    val id: List<Long>? = null,
    val itemHash: String? = null,
    val createTime: RangeCondition<LocalDateTime> = RangeCondition(),
    /**
     * 该条件查询性能差
     */
    val item: ItemCondition? = null,
) {

    constructor(processorName: String?) : this(processorName?.let { listOf(it) })
}

data class ItemCondition(
    val title: String? = null,
    val attrs: Map<String, String>? = null,
    val variables: Map<String, String>? = null,
    val contentType: String? = null,
    val tags: List<String>? = null
)

data class RangeCondition<T : Comparable<*>>(
    val begin: T? = null,
    val end: T? = null
)