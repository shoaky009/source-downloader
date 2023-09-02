package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.component.*
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper
import io.github.shoaky.sourcedownloader.core.component.SourceHashingItemFilter
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.sdk.component.*

class DefaultProcessorManager(
    private val processingStorage: ProcessingStorage,
    private val componentManager: ComponentManager,
    private val container: ObjectWrapperContainer,
) : ProcessorManager {

    override fun createProcessor(config: ProcessorConfig) {
        if (config.enabled.not()) {
            log.info("Processor:'${config.name}' is disabled")
            return
        }

        val processorBeanName = processorBeanName(config.name)
        val processorName = config.name
        if (container.contains(processorBeanName)) {
            throw ComponentException.processorExists("Processor $processorName already exists")
        }

        val source = container.get(config.sourceInstanceName(), sourceTypeRef).getAndMarkRef(processorName)
        val downloader = container.get(config.downloaderInstanceName(), downloaderTypeRef).getAndMarkRef(processorName)

        val mover = container.get(config.moverInstanceName(), fileMoverTypeRef).getAndMarkRef(processorName)
        val resolver = container.get(config.fileResolverInstanceName(), fileResolverTypeRef).getAndMarkRef(processorName)

        val checkTypes = mutableListOf(
            config.source.getComponentType(Source::class),
            config.downloader.getComponentType(Downloader::class),
            config.fileMover.getComponentType(FileMover::class),
        )
        checkTypes.addAll(
            config.variableProviders.map { it.getComponentType(VariableProvider::class) }
        )

        val cps = mutableListOf(source, downloader, mover, resolver)
        // cps.addAll(providers)
        checkTypes.forEach {
            check(it, cps, config)
        }

        val processor = SourceProcessor(
            config.name,
            config.source.id,
            source,
            resolver,
            downloader,
            mover,
            config.savePath,
            processingStorage,
            createOptions(config),
        )

        val processorWrapper = ProcessorWrapper(config.name, processor)
        container.put(processorBeanName, processorWrapper)
        log.info("Processor:'${processor.name}' initialization completed")

        config.triggerInstanceNames().map {
            container.get(it, triggerTypeRef).getAndMarkRef(processorName)
        }.forEach {
            it.addTask(processor.safeTask())
        }
    }

    override fun getProcessor(name: String): ProcessorWrapper {
        val processorBeanName = processorBeanName(name)
        return if (container.contains(processorBeanName)) {
            container.get(processorBeanName, processorTypeRef)
        } else {
            throw ComponentException.processorMissing("Processor $name not found")
        }
    }

    private fun processorBeanName(name: String): String {
        if (name.startsWith("Processor-")) {
            return name
        }
        return "Processor-$name"
    }

    // TODO 重构这一校验，目标通过组件的描述对象
    // TODO 第二个参数应该给组件的描述对象
    private fun check(componentType: ComponentType, components: List<SdComponent>, config: ProcessorConfig) {
        val supplier = componentManager.getSupplier(componentType)
        val compatibilities = supplier.rules().groupBy { it.type }

        val componentTypeMapping = components.groupBy {
            ComponentTopType.fromClass(it::class)
        }.flatMap { (key, value) ->
            key.map { it to value }
        }.groupBy({ it.first }, { it.second })
            .mapValues { it.value.flatten().distinct() }

        for (rules in compatibilities) {
            val typeComponents = componentTypeMapping[rules.key] ?: emptyList()
            if (typeComponents.isEmpty()) {
                continue
            }
            var exception: Exception? = null
            val allow = rules.value.any { rule ->
                components.map {
                    try {
                        rule.verify(it)
                        return@map true
                    } catch (ex: ComponentException) {
                        exception = ex
                        return@map false
                    }
                }.any()
            }
            if (allow.not()) {
                exception?.let {
                    throw ComponentException.compatibility("Processor:${config.name} ${it.message}")
                }
            }
        }
    }

    override fun getProcessors(): List<ProcessorWrapper> {
        return container.getObjectsOfType(processorTypeRef).values.toList()
    }

    override fun destroyProcessor(processorName: String) {
        val processorBeanName = processorBeanName(processorName)
        if (container.contains(processorBeanName).not()) {
            throw ComponentException.processorMissing("Processor '$processorName' not exists")
        }

        log.info("Processor:$processorName destroying")
        val processor = container.get(processorBeanName, processorTypeRef).get()
        val safeTask = processor.safeTask()
        componentManager.getAllTrigger().forEach {
            it.removeTask(safeTask)
        }
        processor.close()
        container.remove(processorBeanName)
        componentManager.getAllComponent().forEach {
            it.removeRef(processorName)
        }
    }

    override fun getAllProcessorNames(): Set<String> {
        return container.getObjectsOfType(processorTypeRef).keys
    }

    private fun createOptions(config: ProcessorConfig): ProcessorOptions {
        val options = config.options
        val sourceItemFilter = mutableListOf<SourceItemFilter>()
        if (options.saveProcessingContent) {
            sourceItemFilter.add(SourceHashingItemFilter(config.name, processingStorage))
        }
        if (options.itemExpressionExclusions.isNotEmpty() || options.itemExpressionInclusions.isNotEmpty()) {
            sourceItemFilter.add(ExpressionItemFilter(
                options.itemExpressionExclusions,
                options.itemExpressionInclusions
            ))
        }
        sourceItemFilter.addAll(
            config.options.sourceItemFilters.map {
                val instanceName = it.getInstanceName(SourceItemFilter::class)
                container.get(instanceName, sourceItemFilterTypeRef).getAndMarkRef(config.name)
            }
        )

        val itemContentFilters = mutableListOf<ItemContentFilter>()
        if (options.contentExpressionExclusions.isNotEmpty() || options.contentExpressionInclusions.isNotEmpty()) {
            itemContentFilters.add(ExpressionItemContentFilter(
                options.contentExpressionExclusions,
                options.contentExpressionInclusions
            ))
        }
        itemContentFilters.addAll(
            config.options.itemContentFilters.map {
                val instanceName = it.getInstanceName(ItemContentFilter::class)
                container.get(instanceName, itemContentFilterTypeRef).getAndMarkRef(config.name)
            }
        )

        val fileContentFilters = mutableListOf<FileContentFilter>()
        if (options.fileExpressionExclusions.isNotEmpty() || options.fileExpressionInclusions.isNotEmpty()) {
            fileContentFilters.add(ExpressionFileFilter(
                options.fileExpressionExclusions,
                options.fileExpressionInclusions
            ))
        }
        fileContentFilters.addAll(
            config.options.fileContentFilters.map {
                val instanceName = it.getInstanceName(FileContentFilter::class)
                container.get(instanceName, fileContentFilterTypeRef).getAndMarkRef(config.name)
            }
        )

        val listeners = options.processListeners.map {
            val instanceName = it.getInstanceName(ProcessListener::class)
            container.get(instanceName, processListenerTypeRef).getAndMarkRef(config.name)
        }.toMutableList()
        if (options.deleteEmptyDirectory) {
            listeners.add(DeleteEmptyDirectory)
        }
        if (options.touchItemDirectory) {
            listeners.add(TouchItemDirectory)
        }

        val taggers = options.fileTaggers.map {
            val instanceName = it.getInstanceName(FileTagger::class)
            container.get(instanceName, fileTaggerTypeRef).getAndMarkRef(config.name)
        }

        val providers = config.providerInstanceNames().map {
            container.get(it, variableProviderTypeRef).getAndMarkRef(config.name)
        }.toMutableList()

        val fileReplacementDeciderName = options.fileReplacementDecider.getInstanceName(FileReplacementDecider::class)
        val fileReplacementDecider = container.get(
            fileReplacementDeciderName, fileReplacementDeciderRef
        ).component

        val fileExistsDetector = options.fileExistsDetector?.let {
            val instanceName = it.getInstanceName(FileExistsDetector::class)
            container.get(instanceName, fileExistsDetectorRef).component
        } ?: SimpleFileExistsDetector

        val variableReplacers: MutableList<VariableReplacer> = mutableListOf(
            *options.variableReplacers.toTypedArray(), WindowsPathReplacer
        )

        val fileGrouping = mutableMapOf<SourceFileMatcher, FileOption>()
        for (fileOption in options.fileGrouping) {
            val taggedFileContentFilters = mutableListOf<FileContentFilter>()
            if (fileOption.fileExpressionExclusions.isNotEmpty() || fileOption.fileExpressionInclusions.isNotEmpty()) {
                taggedFileContentFilters.add(ExpressionFileFilter(
                    fileOption.fileExpressionExclusions,
                    fileOption.fileExpressionInclusions
                ))
            }
            taggedFileContentFilters.addAll(
                fileOption.fileContentFilters.map {
                    val instanceName = it.getInstanceName(FileContentFilter::class)
                    container.get(instanceName, fileContentFilterTypeRef).getAndMarkRef(config.name)
                }
            )

            val matcher = if (fileOption.tags.isNotEmpty()) {
                TagSourceFileMatcher(fileOption.tags)
            } else if (fileOption.matchedExpression != null) {
                ExpressionSourceFileMatcher(fileOption.matchedExpression)
            } else {
                throw ComponentException.other("fileGrouping must have tags or matchedExpression")
            }
            fileGrouping[matcher] = FileOption(
                fileOption.savePathPattern?.let {
                    CorePathPattern(it.pattern)
                },
                fileOption.filenamePattern?.let {
                    CorePathPattern(it.pattern)
                },
                taggedFileContentFilters
            )
        }

        options.manualSources.forEach {
            val instanceName = it.getInstanceName(ManualSource::class)
            container.get(instanceName, manualSourceRef).getAndMarkRef(config.name)
        }

        return ProcessorOptions(
            CorePathPattern(options.savePathPattern.pattern),
            CorePathPattern(options.filenamePattern.pattern),
            providers,
            listeners,
            sourceItemFilter,
            itemContentFilters,
            fileContentFilters,
            taggers,
            variableReplacers,
            fileReplacementDecider,
            fileGrouping,
            options.saveProcessingContent,
            options.renameTaskInterval,
            options.downloadOptions,
            options.variableConflictStrategy,
            options.renameTimesThreshold,
            options.variableErrorStrategy,
            options.variableNameReplace,
            options.fetchLimit,
            options.pointerBatchMode,
            options.category,
            options.tags,
            options.itemErrorContinue,
            fileExistsDetector,
            options.channelBufferSize,
            options.listenerMode
        )
    }
}