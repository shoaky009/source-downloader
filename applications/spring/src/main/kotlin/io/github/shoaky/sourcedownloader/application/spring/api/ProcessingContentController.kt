package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import io.github.shoaky.sourcedownloader.service.ProcessingContentService
import io.github.shoaky.sourcedownloader.service.Scroll
import io.github.shoaky.sourcedownloader.service.UpdateProcessingContent
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * ProcessingContent相关的API

 */
@RestController
@RequestMapping("/api/processing-content")
private class ProcessingContentController(
    private val service: ProcessingContentService
) {

    /**
     * 获取指定ProcessingContent
     * @param id ProcessingContent ID
     * @return ProcessingContent
     */
    @GetMapping("/{id}")
    fun getProcessingContent(@PathVariable id: Long): ProcessingContent {
        return service.getProcessingContent(id)
    }

    /**
     * TODO 改POST
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
        return service.queryContents(query, limit, maxId)
    }

    /**
     * 修改ProcessingContent
     * @param id ProcessingContent ID
     * @param body 修改内容
     * @return 修改后的ProcessingContent
     */
    @PutMapping("/{id}")
    fun modifyProcessingContent(@PathVariable id: Long, @RequestBody body: UpdateProcessingContent): ProcessingContent {
        return service.modifyProcessingContent(id, body)
    }

    /**
     * 删除ProcessingContent
     * @param id ProcessingContent ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProcessingContent(@PathVariable id: Long) {
        service.deleteProcessingContent(id)
    }

    /**
     * 重新处理ProcessingContent
     */
    @PostMapping("/{id}/reprocess")
    suspend fun reprocess(@PathVariable id: Long) {
        service.reprocess(id)
    }
}