package io.github.shoaky.sourcedownloader.application.spring.api

import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.processor.DryRunOptions
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.service.*
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * Processor相关的API
 */
@RestController
@RequestMapping("/api/processor")
private class ProcessorController(
    private val processorService: ProcessorService,
    private val service: ProcessingContentService
) {

    /**
     * 获取所有Processor的信息
     * @return Processor信息列表
     */
    @GetMapping
    fun getProcessors(
        name: String?, pageable: PageRequest
    ): List<ProcessorInfo> {
        return processorService.getProcessors(name, pageable)
    }

    /**
     * 获取指定Processor的信息
     * @param processorName Processor名称
     */
    @GetMapping("/{processorName}")
    fun getConfig(@PathVariable processorName: String): ProcessorConfig {
        return processorService.getConfig(processorName)
    }

    /**
     * 创建Processor
     * @param processorConfig Processor配置
     */
    @PostMapping
    fun create(@RequestBody processorConfig: ProcessorConfig) {
        processorService.create(processorConfig)
    }

    /**
     * 更新Processor配置，会自动重载Processor
     * @param processorName Processor名称
     * @param processorConfig Processor配置
     */
    @PutMapping("/{processorName}")
    fun update(
        @PathVariable processorName: String,
        @RequestBody processorConfig: ProcessorConfig
    ): ProcessorConfig {
        return processorService.update(processorName, processorConfig)
    }

    /**
     * 删除Processor
     * @param processorName Processor名称
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{processorName}")
    fun delete(@PathVariable processorName: String) {
        processorService.delete(processorName)
    }

    /**
     * 重载Processor
     *
     * @description 只重新加载Processor配置，但不会重新加载引用的Component
     * @param processorName Processor名称
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/{processorName}/reload")
    fun reload(@PathVariable processorName: String) {
        processorService.reload(processorName)
    }

    /**
     * Processor模拟处理
     * @param processorName Processor名称
     * @param options 模拟处理选项
     * @return 模拟处理结果
     */
    @RequestMapping(
        "/{processorName}/dry-run",
        method = [RequestMethod.GET, RequestMethod.POST],
    )
    fun dryRun(
        @PathVariable processorName: String,
        @RequestBody(required = false) options: DryRunOptions?
    ): List<ProcessingContent> {
        return processorService.dryRun(processorName, options ?: DryRunOptions())
    }

    @RequestMapping(
        "/{processorName}/dry-run-stream",
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    suspend fun dryRunStream(
        @PathVariable processorName: String,
        @RequestBody(required = false) options: DryRunOptions?
    ): Flow<ProcessingContent> {
        return processorService.dryRunStream(processorName, options ?: DryRunOptions())
    }

    /**
     * 手动触发Processor
     * @param processorName Processor名称
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/{processorName}/trigger")
    fun trigger(@PathVariable processorName: String) {
        processorService.trigger(processorName)
    }

    /**
     * 手动触发重命名
     * @param processorName Processor名称
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/{processorName}/rename")
    fun rename(@PathVariable processorName: String) {
        processorService.rename(processorName)
    }

    /**
     * 手动提交Items到Processor(experimental)
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{processorName}/items")
    suspend fun postItems(@PathVariable processorName: String, @RequestBody items: List<SourceItem>) {
        processorService.postItems(processorName, items)
    }

    /**
     * 获取Processor状态
     * @param processorName Processor名称
     * @return Processor状态
     */
    @GetMapping("/{processorName}/state")
    fun getState(@PathVariable processorName: String): ProcessorState {
        return processorService.getState(processorName)
    }

    @PutMapping("/{processorName}/pointer")
    fun modifyPointer(@PathVariable processorName: String, @RequestBody payload: PointerPayload) {
        processorService.modifyPointer(processorName, payload.sourceId, payload.pointer)
    }

    @DeleteMapping("/{processorName}/contents")
    fun deleteContents(@PathVariable processorName: String): Any {
        return processorService.deleteContents(processorName)
    }

    data class PointerPayload(
        val sourceId: String,
        val pointer: Map<String, Any>
    )

    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @PostMapping("/{processorName}/enable")
    fun enableProcessor(@PathVariable processorName: String) {
        processorService.enableProcessor(processorName)
    }

    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @PostMapping("/{processorName}/disable")
    fun disableProcessor(@PathVariable processorName: String) {
        processorService.disableProcessor(processorName)
    }
}