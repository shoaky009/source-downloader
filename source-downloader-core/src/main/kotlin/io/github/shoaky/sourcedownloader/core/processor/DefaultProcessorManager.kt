package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.component.supplier.*
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.ComponentManager
import io.github.shoaky.sourcedownloader.core.component.ProcessorWrapper
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*

class DefaultProcessorManager(
    private val processingStorage: ProcessingStorage,
    private val componentManager: ComponentManager,
    private val container: ObjectWrapperContainer,
) : ProcessorManager {

    override fun createProcessor(config: ProcessorConfig): ProcessorWrapper {
        val processorBeanName = processorBeanName(config.name)
        val processorName = config.name
        if (container.contains(processorBeanName)) {
            throw ComponentException.processorExists("Processor ${processorName} already exists")
        }

        val source = container.get(config.sourceInstanceName(), sourceTypeRef).getAndMarkRef(processorName)
        val downloader = container.get(config.downloaderInstanceName(), downloaderTypeRef).getAndMarkRef(processorName)

        val providers = config.providerInstanceNames().map {
            container.get(it, variableProviderTypeRef).getAndMarkRef(processorName)
        }
        val mover = container.get(config.moverInstanceName(), fileMoverTypeRef).getAndMarkRef(processorName)
        val resolver = container.get(config.fileResolverInstanceName(), fileResolverTypeRef).getAndMarkRef(processorName)
        val processor = SourceProcessor(
            config.name,
            config.source.id,
            source,
            providers,
            resolver,
            downloader,
            mover,
            config.savePath,
            config.options,
            processingStorage,
        )

        val checkTypes = mutableListOf(
            config.source.getComponentType(Source::class),
            config.downloader.getComponentType(Downloader::class),
            config.fileMover.getComponentType(FileMover::class),
        )
        checkTypes.addAll(
            config.variableProviders.map { it.getComponentType(VariableProvider::class) }
        )

        val cps = mutableListOf(source, downloader, mover, resolver)
        cps.addAll(providers)
        checkTypes.forEach {
            check(it, cps, config)
        }

        val fileFilters = config.options.fileContentFilters.map {
            val instanceName = it.getInstanceName(FileContentFilter::class)
            container.get(instanceName, fileContentFilterTypeRef).getAndMarkRef(processorName)
        }.toTypedArray()
        processor.addFileFilter(*fileFilters)

        val itemFilters = config.options.sourceItemFilters.map {
            val instanceName = it.getInstanceName(FileContentFilter::class)
            container.get(instanceName, fileContentFilterTypeRef).getAndMarkRef(processorName)
        }.toTypedArray()
        processor.addFileFilter(*itemFilters)

        initOptions(config.options, processor)

        val processorWrapper = ProcessorWrapper(config.name, processor)
        container.put(processorName, processorWrapper)
        log.info("处理器初始化完成:$processor")

        val task = processor.safeTask()
        config.triggerInstanceNames().map {
            container.get(it, triggerTypeRef).getAndMarkRef(processorName)
        }.forEach {
            it.addTask(task)
        }
        return processorWrapper
    }

    override fun getProcessor(name: String): ProcessorWrapper? {
        val processorBeanName = processorBeanName(name)
        return if (container.contains(processorBeanName)) {
            container.get(processorBeanName, processorTypeRef)
        } else {
            null
        }
    }

    private fun processorBeanName(name: String): String {
        if (name.startsWith("Processor-")) {
            return name
        }
        return "Processor-$name"
    }

    private fun initOptions(options: ProcessorConfig.Options, processor: SourceProcessor) {
        if (options.itemExpressionExclusions.isNotEmpty() || options.itemExpressionInclusions.isNotEmpty()) {
            processor.addItemFilter(
                ExpressionItemFilterSupplier.expressions(
                    options.itemExpressionExclusions,
                    options.itemExpressionInclusions
                )
            )
        }
        if (options.contentExpressionExclusions.isNotEmpty() || options.contentExpressionInclusions.isNotEmpty()) {
            processor.addContentFilter(
                ExpressionSourceContentFilterSupplier.expressions(
                    options.contentExpressionExclusions,
                    options.contentExpressionInclusions
                )
            )
        }
        if (options.fileExpressionExclusions.isNotEmpty() || options.fileExpressionInclusions.isNotEmpty()) {
            processor.addFileFilter(
                ExpressionFileFilterSupplier.expressions(
                    options.fileExpressionExclusions,
                    options.fileExpressionInclusions
                )
            )
        }
        val runAfterCompletion = options.runAfterCompletion
        runAfterCompletion.forEach {
            val instanceName = it.getInstanceName(RunAfterCompletion::class)
            container.get(instanceName, runAfterCompletionTypeRef).getAndMarkRef(processor.name)
                .also { completion -> processor.addRunAfterCompletion(completion) }
        }
        processor.scheduleRenameTask(options.renameTaskInterval)
        if (options.deleteEmptyDirectory) {
            val deleteEmptyDirectory = DeleteEmptyDirectorySupplier.apply(Properties.EMPTY)
            processor.addRunAfterCompletion(deleteEmptyDirectory)
        }
        if (options.touchItemDirectory) {
            val touchItemDirectory = TouchItemDirectorySupplier.apply(Properties.EMPTY)
            processor.addRunAfterCompletion(touchItemDirectory)
        }
        options.fileTaggers.forEach {
            val instanceName = it.getInstanceName(FileTagger::class)
            container.get(instanceName, fileTaggerTypeRef).getAndMarkRef(processor.name)
                .also { tagger -> processor.addTagger(tagger) }
        }
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

    override fun destroy(processorName: String) {
        val processorBeanName = processorBeanName(processorName)
        if (container.contains(processorBeanName).not()) {
            throw ComponentException.processorMissing("Processor $processorName not exists")
        }

        val processor = container.get(processorBeanName, processorTypeRef).get()
        val safeTask = processor.safeTask()
        componentManager.getAllTrigger().forEach {
            it.removeTask(safeTask)
        }
        container.remove(processorBeanName)
    }

    override fun getAllProcessorNames(): Set<String> {
        return container.getObjectsOfType(processorTypeRef).keys
    }
}