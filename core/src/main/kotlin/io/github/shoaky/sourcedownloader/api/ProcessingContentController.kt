package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/processing-content")
private class ProcessingContentController(
    private val storage: ProcessingStorage,
    private val processorManager: ProcessorManager
) {

    @GetMapping("/{id}")
    fun getProcessingContent(@PathVariable id: Long): ProcessingContent {
        return storage.findById(id)
    }

    @GetMapping
    fun queryContents(
        query: ProcessingQuery,
        limit: Int = 20,
        maxId: Long = 0
    ): Scroll {
        if (query.itemTitle != null && query.processorName == null) {
            throw IllegalArgumentException("itemTitle must be used with processorName")
        }
        if ((query.createTime.begin != null || query.createTime.end != null) && query.processorName == null) {
            throw IllegalArgumentException("range must be used with processorName")
        }
        val contents = storage.queryContents(query, limit, maxId)
        return Scroll(
            contents,
            contents.lastOrNull()?.id ?: maxId
        )
    }

    @PutMapping("/{id}")
    fun modifyProcessingContent(@PathVariable id: Long, @RequestBody body: UpdateProcessingContent): ProcessingContent {
        val current = storage.findById(id)
        val update = current.copy(
            // 这个待定，有些字段不允许修改
            status = body.status ?: current.status,
            renameTimes = body.renameTimes ?: current.renameTimes,
            modifyTime = LocalDateTime.now()
        )
        return storage.save(update)
    }

    @DeleteMapping("/{id}")
    fun deleteProcessingContent(@PathVariable id: Long) {
        storage.deleteProcessingContent(id)
    }

    @PostMapping("/{id}/reprocess")
    suspend fun reprocess(@PathVariable id: Long) {
        val content = storage.findById(id)
        val processor = processorManager.getProcessor(content.processorName).get()
        processor.reprocess(content)
    }
}

data class Scroll(
    val contents: List<ProcessingContent>,
    val nextMaxId: Long
)

data class UpdateProcessingContent(
    val status: ProcessingContent.Status? = null,
    val renameTimes: Int? = null
)