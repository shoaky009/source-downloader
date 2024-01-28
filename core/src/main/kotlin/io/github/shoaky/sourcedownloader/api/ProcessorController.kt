package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.file.FileContentStatus
import io.github.shoaky.sourcedownloader.core.processor.DryRunOptions
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.Executors

/**
 * Processor相关的API
 */
@RestController
@RequestMapping("/api/processor")
private class ProcessorController(
    private val processorManager: ProcessorManager,
    private val configStorages: List<ProcessorConfigStorage>,
    private val configOperator: ConfigOperator,
    private val processingStorage: ProcessingStorage
) {

    private val manualTriggerExecutor = Executors.newThreadPerTaskExecutor(
        Thread.ofVirtual().name("manual-trigger", 0).factory()
    )

    /**
     * 获取所有Processor的信息
     * @return Processor信息列表
     */
    @GetMapping
    fun getProcessors(): List<ProcessorInfo> {
        val processors = processorManager.getProcessors()
        return processors.map {
            ProcessorInfo(it.name, it.get().info())
        }
    }

    /**
     * 获取指定Processor的信息
     * @param processorName Processor名称
     */
    @GetMapping("/{processorName}")
    fun getConfig(@PathVariable processorName: String): ProcessorConfig? {
        return configStorages.flatMap { it.getAllProcessorConfig() }
            .firstOrNull { it.name == processorName }
    }

    /**
     * 创建Processor
     * @param processorConfig Processor配置
     */
    @PostMapping
    fun create(@RequestBody processorConfig: ProcessorConfig) {
        val processorName = processorConfig.name
        if (processorManager.exists(processorName)) {
            throw ComponentException.processorExists("Processor $processorName already exists")
        }
        configOperator.save(processorName, processorConfig)
        processorManager.createProcessor(processorConfig)
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
    ): ProcessorInfo {
        processorManager.getProcessor(processorName)

        configOperator.save(processorName, processorConfig)
        processorManager.destroyProcessor(processorName)
        processorManager.createProcessor(processorConfig)
        val info = processorManager.getProcessor(processorName).get().info()
        return ProcessorInfo(processorName, info)
    }

    /**
     * 删除Processor
     * @param processorName Processor名称
     */
    @DeleteMapping("/{processorName}")
    fun delete(@PathVariable processorName: String) {
        processorManager.destroyProcessor(processorName)
        configOperator.deleteProcessor(processorName)
    }

    /**
     * 重载Processor
     *
     * @description 只重新加载Processor配置，但不会重新加载引用的Component
     * @param processorName Processor名称
     */
    @GetMapping("/{processorName}/reload")
    fun reload(@PathVariable processorName: String) {
        val config = configOperator.getProcessorConfig(processorName)
        if (processorManager.exists(processorName)) {
            processorManager.destroyProcessor(processorName)
        }
        processorManager.createProcessor(config)
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
    ): List<DryRunResult> {
        val sourceProcessor = processorManager.getProcessor(processorName)
        return sourceProcessor.get().dryRun(options ?: DryRunOptions())
            .map { pc ->
                val fileResult = pc.itemContent.sourceFiles.map { file ->
                    FileResult(
                        file.fileDownloadPath.toString(),
                        file.targetPath().toString(),
                        file.patternVariables.variables(),
                        file.tags,
                        file.status,
                        file.errors.takeIf { it.isNotEmpty() }?.joinToString(", ")
                    )
                }
                val itemContent = pc.itemContent
                val variables = itemContent.sharedPatternVariables.variables()
                DryRunResult(
                    itemContent.sourceItem, variables,
                    fileResult, pc.status
                )
            }
    }

    /**
     * 手动触发Processor
     * @param processorName Processor名称
     */
    @GetMapping("/{processorName}/trigger")
    fun trigger(@PathVariable processorName: String) {
        val sourceProcessor = processorManager.getProcessor(processorName)
        manualTriggerExecutor.submit(sourceProcessor.get().safeTask())
    }

    /**
     * 手动提交Items到Processor(experimental)
     */
    @PostMapping("/{processorName}/items")
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun postItems(@PathVariable processorName: String, @RequestBody items: List<SourceItem>) {
        val processor = processorManager.getProcessor(processorName).get()
        processor.run(items)
    }

    /**
     * 获取Processor状态
     * @param processorName Processor名称
     * @return Processor状态
     */
    @GetMapping("/{processorName}/state")
    fun getState(@PathVariable processorName: String): ProcessorState {
        val config = configOperator.getProcessorConfig(processorName)
        val state = processingStorage.findProcessorSourceState(processorName, config.source.name())
            ?.let {
                ProcessorState(it.lastPointer, it.lastActiveTime)
            } ?: ProcessorState(PersistentPointer(mutableMapOf()), null)
        return state
    }

}

/**
 * Processor状态
 */
private data class ProcessorState(
    /**
     * Source当前处理的未知
     */
    val pointer: PersistentPointer,
    /**
     * 最后一次活跃时间
     */
    val lastActiveTime: LocalDateTime? = null
)

private data class ProcessorInfo(
    val name: String,
    val components: Map<String, Any>
)

/**
 * Processor模拟处理结果
 */
private data class DryRunResult(
    /**
     * SourceItem
     */
    val sourceItem: SourceItem,
    /**
     * 共享命名变量
     */
    val sharedVariables: Map<String, Any>,
    /**
     * 文件处理结果
     */
    val fileResults: List<FileResult>,
    /**
     * 处理状态
     */
    val status: ProcessingContent.Status
)

/**
 * 文件处理结果
 */
private data class FileResult(
    /**
     * 下载路径
     */
    val from: String,
    /**
     * 目标路径
     */
    val to: String,
    /**
     * 私有命名变量
     */
    val variables: Map<String, Any>,
    /**
     * 文件标签
     */
    val tags: Collection<String>,
    /**
     * 文件处理状态
     */
    val status: FileContentStatus,
    /**
     * 错误信息
     */
    val error: String? = null
)