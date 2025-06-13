package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.PersistentPointer
import io.github.shoaky.sourcedownloader.core.ProcessingContent
import io.github.shoaky.sourcedownloader.core.ProcessingStorage
import io.github.shoaky.sourcedownloader.core.ProcessorConfig
import io.github.shoaky.sourcedownloader.core.component.ComponentFailureType
import io.github.shoaky.sourcedownloader.core.component.ComponentReloadEvent
import io.github.shoaky.sourcedownloader.core.component.ConfigOperator
import io.github.shoaky.sourcedownloader.core.processor.DryRunOptions
import io.github.shoaky.sourcedownloader.core.processor.ProcessorManager
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.core.processor.log
import io.github.shoaky.sourcedownloader.sdk.SourceItem
import io.github.shoaky.sourcedownloader.throwComponentException
import io.github.shoaky.sourcedownloader.util.InternalEventBus
import kotlinx.coroutines.flow.Flow

class ProcessorService(
    private val processorManager: ProcessorManager,
    private val configOperator: ConfigOperator,
    private val processingStorage: ProcessingStorage
) {

    init {
        InternalEventBus.register(ComponentReloadEvent.ADDRESS, ComponentReloadEvent::class.java) {
            handleReload(it)
        }
    }

    /**
     * 获取所有Processor的信息
     * @return Processor信息列表
     */
    fun getProcessors(name: String?, pageable: PageRequest): List<ProcessorInfo> {
        return configOperator.getAllProcessorConfig()
            .filter { name == null || it.name.contains(name) }
            .drop(pageable.pageNumber * pageable.pageSize)
            .take(pageable.pageSize)
            .map { config ->
                val wrapper = runCatching {
                    processorManager.getProcessor(config.name)
                }.getOrNull()
                val snapshot = if (wrapper?.processor != null) {
                    processorManager.getProcessor(config.name).get()
                        .getRuntimeSnapshot()
                } else {
                    null
                }
                ProcessorInfo(
                    config.name,
                    config.enabled,
                    config.category,
                    config.tags,
                    snapshot,
                    wrapper?.errorMessage
                )
            }
    }

    /**
     * 获取指定Processor的信息
     * @param processorName Processor名称
     */
    fun getConfig(processorName: String): ProcessorConfig {
        return configOperator.getProcessorConfig(processorName)
    }

    /**
     * 创建Processor
     * @param processorConfig Processor配置
     */
    fun create(processorConfig: ProcessorConfig) {
        val processorName = processorConfig.name
        if (processorManager.exists(processorName)) {
            throwComponentException(
                "Processor $processorName already exists",
                ComponentFailureType.PROCESSOR_ALREADY_EXISTS
            )
        }
        configOperator.save(processorName, processorConfig)
        processorManager.createProcessor(processorConfig)
    }

    /**
     * 更新Processor配置，会自动重载Processor
     * @param processorName Processor名称
     * @param processorConfig Processor配置
     */
    fun update(
        processorName: String,
        processorConfig: ProcessorConfig
    ): ProcessorConfig {
        configOperator.getProcessorConfig(processorName)
        configOperator.save(processorName, processorConfig)
        reload(processorName)
        return processorConfig
    }

    /**
     * 删除Processor
     * @param processorName Processor名称
     */
    fun delete(processorName: String) {
        processorManager.destroyProcessor(processorName)
        configOperator.deleteProcessor(processorName)
    }

    /**
     * 重载Processor
     *
     * @description 只重新加载Processor配置，但不会重新加载引用的Component
     * @param processorName Processor名称
     */
    fun reload(processorName: String) {
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
    fun dryRun(
        processorName: String,
        options: DryRunOptions?
    ): List<ProcessingContent> {
        val sourceProcessor = processorManager.getProcessor(processorName)
        return sourceProcessor.get().dryRun(options ?: DryRunOptions())
    }

    fun dryRunStream(
        processorName: String,
        options: DryRunOptions?
    ): Flow<ProcessingContent> {
        val sourceProcessor = processorManager.getProcessor(processorName)
        return sourceProcessor.get().dryRunStream(options ?: DryRunOptions())
    }

    /**
     * 手动触发Processor
     * @param processorName Processor名称
     */
    fun trigger(processorName: String) {
        val processor = processorManager.getProcessor(processorName).get()
        Thread.ofVirtual().name("manual-trigger", 0).start(processor.safeTask())
    }

    /**
     * 手动触发重命名
     * @param processorName Processor名称
     */
    fun rename(processorName: String) {
        val sourceProcessor = processorManager.getProcessor(processorName)
        sourceProcessor.get().runRename()
    }

    /**
     * 手动提交Items到Processor(experimental)
     */
    fun postItems(processorName: String, items: List<SourceItem>) {
        val processor = processorManager.getProcessor(processorName).get()
        processor.run(items)
    }

    /**
     * 获取Processor状态
     * @param processorName Processor名称
     * @return Processor状态
     */
    fun getState(processorName: String): ProcessorState {
        val config = configOperator.getProcessorConfig(processorName)
        val state = processingStorage.findProcessorSourceState(processorName, config.source.id)
            ?.let {
                ProcessorState(it.sourceId, it.lastPointer, it.lastActiveTime, it.retryTimes)
            } ?: ProcessorState(config.source.id, PersistentPointer(mutableMapOf()), null)
        return state
    }

    fun modifyPointer(processorName: String, sourceId: String, payload: Map<String, Any>) {
        val state = processingStorage.findProcessorSourceState(processorName, sourceId)
            ?: throw NotFoundException("Processor $processorName with source $sourceId not found")
        state.lastPointer.values.putAll(payload)
        processingStorage.save(state)
    }

    fun enableProcessor(processorName: String) {
        val config = configOperator.getProcessorConfig(processorName)
        if (config.enabled) return

        val enabled = config.copy(enabled = true)
        configOperator.save(processorName, enabled)
        processorManager.destroyProcessor(processorName)
        processorManager.createProcessor(enabled)
    }

    fun disableProcessor(processorName: String) {
        val config = configOperator.getProcessorConfig(processorName)
        if (config.enabled.not()) return

        val enabled = config.copy(enabled = false)
        configOperator.save(processorName, enabled)
        processorManager.destroyProcessor(processorName)
    }

    fun deleteContents(processorName: String): Map<String, Int> {
        val count1 = processingStorage.deleteProcessingContentByProcessorName(processorName)
        val count2 = processingStorage.deleteTargetPathByProcessorName(processorName)
        return mapOf(
            "processingContent" to count1,
            "targetPath" to count2
        )
    }

    @Synchronized
    private fun handleReload(event: ComponentReloadEvent) {
        for (ref in event.refs) {
            val notExists = processorManager.exists(ref).not()
            if (notExists) {
                continue
            }
            log.info(
                "Reloading processor {} due to component {}:{}",
                ref,
                event.componentType.fullName(),
                event.componentName
            )
            reload(ref)
        }
    }
}