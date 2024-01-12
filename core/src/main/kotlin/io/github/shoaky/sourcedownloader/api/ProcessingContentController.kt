package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/processing-content")
private class ProcessingContentController(
    private val storage: ProcessingStorage,
    private val processorManager: ProcessorManager
) {

    @GetMapping("/{id}")
    fun findProcessingContent(@PathVariable id: Long): ProcessingContent? {
        return storage.findById(id)
    }

    @GetMapping
    fun findProcessingContents(
        query: Query,
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC)
        page: Pageable
    ): List<ProcessingContent> {
        // TODO
        return emptyList()
    }

    @PutMapping("/{id}")
    fun modifyProcessingContent(@PathVariable id: Long, @RequestBody body: ProcessingContent): ProcessingContent {
        val current = storage.findById(id)
        return storage.save(
            current.copy(
                // 这个待定，有些字段不允许修改
                itemContent = body.itemContent,
                renameTimes = body.renameTimes,
                modifyTime = LocalDateTime.now()
            )
        )
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

data class Query(
    var ids: List<Long> = emptyList(),
    var processorName: String? = null,
    var createdStart: LocalDateTime? = null,
    var createdEnd: LocalDateTime? = null,
)