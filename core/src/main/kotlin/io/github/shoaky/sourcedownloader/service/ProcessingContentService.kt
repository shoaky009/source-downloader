package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.repo.ProcessingQuery
import java.time.LocalDateTime

class ProcessingContentService(
    private val storage: ProcessingStorage,
    private val processorManager: ProcessorManager
) {

    fun getProcessingContent(id: Long): ProcessingContent {
        return storage.findById(id) ?: throw NotFoundException("Record $id not found")
    }

    /**
     * 查询ProcessingContent
     * @param query 查询条件
     * @param limit 查询数量, 默认20
     * @param maxId 最大ID, 默认0
     */
    fun queryContents(
        query: ProcessingQuery,
        limit: Int = 20,
        maxId: Long = 0
    ): Scroll {
        if (query.item != null && query.processorName == null) {
            // 为查询性能做一定的限制
            throw IllegalArgumentException("Item condition must be used with processorName")
        }
        val contents = storage.queryContents(query, limit, maxId)
        return Scroll(contents, contents.lastOrNull()?.id ?: maxId)
    }

    /**
     * 修改ProcessingContent
     * @param id ProcessingContent ID
     * @param body 修改内容
     * @return 修改后的ProcessingContent
     */
    fun modifyProcessingContent(id: Long, body: UpdateProcessingContent): ProcessingContent {
        val current = storage.findById(id) ?: throw NotFoundException("Record $id not found")
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
    fun deleteProcessingContent(id: Long) {
        storage.deleteProcessingContent(id)
    }

    /**
     * 重新处理ProcessingContent
     */
    suspend fun reprocess(id: Long) {
        val content = storage.findById(id) ?: throw NotFoundException("Record $id not found")
        val processor = processorManager.getProcessor(content.processorName).get()
        processor.reprocess(content)
    }
}