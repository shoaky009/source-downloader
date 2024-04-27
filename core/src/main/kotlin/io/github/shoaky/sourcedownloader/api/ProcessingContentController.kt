package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * ProcessingContent相关的API

 */
@RestController
@RequestMapping("/api/processing-content")
private class ProcessingContentController(
    private val storage: ProcessingStorage,
    private val processorManager: ProcessorManager
) {

    /**
     * 获取指定ProcessingContent
     * @param id ProcessingContent ID
     * @return ProcessingContent
     */
    @GetMapping("/{id}")
    fun getProcessingContent(@PathVariable id: Long): ProcessingContent {
        return storage.findById(id)
    }

    /**
     * 查询ProcessingContent
     * @param query 查询条件
     * @param limit 查询数量, 默认20
     * @param maxId 最大ID, 默认0
     */
    @GetMapping
    fun queryContents(
        query: ProcessingQuery,
        limit: Int = 20,
        maxId: Long = 0
    ): Scroll {
        if (query.itemTitle != null && query.processorName == null) {
            throw IllegalArgumentException("itemTitle must be used with processorName")
        }
        val contents = storage.queryContents(query, limit, maxId)
        return Scroll(
            contents,
            contents.lastOrNull()?.id ?: maxId
        )
    }

    /**
     * 修改ProcessingContent
     * @param id ProcessingContent ID
     * @param body 修改内容
     * @return 修改后的ProcessingContent
     */
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

    /**
     * 删除ProcessingContent
     * @param id ProcessingContent ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProcessingContent(@PathVariable id: Long) {
        storage.deleteProcessingContent(id)
    }

    /**
     * 重新处理ProcessingContent
     */
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