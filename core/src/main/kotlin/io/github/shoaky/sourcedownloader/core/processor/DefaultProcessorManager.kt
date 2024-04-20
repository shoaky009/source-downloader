package io.github.shoaky.sourcedownloader.core.processor

import io.github.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import io.github.shoaky.sourcedownloader.component.*
import io.github.shoaky.sourcedownloader.component.replacer.WindowsPathReplacer
import io.github.shoaky.sourcedownloader.core.*
import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.expression.sourceFileDefs
import io.github.shoaky.sourcedownloader.core.expression.sourceItemDefs
import io.github.shoaky.sourcedownloader.core.file.CorePathPattern
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.throwComponentException
import io.github.shoaky.sourcedownloader.util.addToCollection

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
            throwComponentException(
                "Processor $processorName already exists",
                ComponentFailureType.PROCESSOR_ALREADY_EXISTS
            )
        }

        val sourceW = componentManager.getComponent(
            ComponentTopType.SOURCE,
            config.source,
            sourceTypeRef,
        )
        val source = sourceW.getAndMarkRef(processorName)

        val downloaderW = componentManager.getComponent(
            ComponentTopType.DOWNLOADER,
            config.downloader,
            downloaderTypeRef,
        )
        val downloader = downloaderW.getAndMarkRef(processorName)

        val moverW = componentManager.getComponent(
            ComponentTopType.FILE_MOVER,
            config.fileMover,
            fileMoverTypeRef,
        )
        val mover = moverW.getAndMarkRef(processorName)

        val resolverW = componentManager.getComponent(
            ComponentTopType.ITEM_FILE_RESOLVER,
            config.itemFileResolver,
            fileResolverTypeRef,
        )
        val resolver = resolverW.getAndMarkRef(processorName)

        val checkTypes = mutableListOf(
            config.source.getComponentType(Source::class),
            config.downloader.getComponentType(Downloader::class),
            config.fileMover.getComponentType(FileMover::class),
            config.itemFileResolver.getComponentType(ItemFileResolver::class),
        )
        checkTypes.addAll(
            config.options.variableProviders.map {
                it.getComponentType(VariableProvider::class)
            }
        )

        val componentToCheck = mapOf(
            ComponentTopType.SOURCE to sourceW,
            ComponentTopType.DOWNLOADER to downloaderW,
            ComponentTopType.FILE_MOVER to moverW,
            ComponentTopType.ITEM_FILE_RESOLVER to resolverW,
        )
        checkTypes.forEach {
            check(it, componentToCheck)
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
            config.category,
            config.tags,
            createOptions(config, source.group()),
        )

        val processorWrapper = ProcessorWrapper(config.name, processor)
        container.put(processorBeanName, processorWrapper)
        log.info("Processor:'${processor.name}' initialization completed")

        config.triggers.map {
            componentManager.getComponent(
                ComponentTopType.TRIGGER,
                it,
                triggerTypeRef,
            ).getAndMarkRef(processorName)
        }.forEach {
            it.addTask(processor.safeTask())
        }
    }

    override fun getProcessor(name: String): ProcessorWrapper {
        val processorBeanName = processorBeanName(name)
        return if (container.contains(processorBeanName)) {
            container.get(processorBeanName, processorTypeRef)
        } else {
            throwComponentException("Processor $name not found", ComponentFailureType.PROCESSOR_NOT_FOUND)
        }
    }

    override fun exists(name: String): Boolean {
        return container.contains(processorBeanName(name))
    }

    private fun processorBeanName(name: String): String {
        if (name.startsWith("Processor:")) {
            return name
        }
        return "Processor:$name"
    }

    private fun check(
        subjectType: ComponentType,
        componentToCheck: Map<ComponentTopType, ComponentWrapper<out SdComponent>>,
    ) {
        val supplier = componentManager.getSupplier(subjectType)
        val compatibilities = supplier.rules().groupBy { it.type }

        for ((type, rules) in compatibilities) {
            val wrapper = componentToCheck[type]
            if (wrapper == null || wrapper.type.typeName == "composite") {
                continue
            }
            rules.forEach { rule ->
                try {
                    rule.verify(wrapper.component)
                } catch (ex: ComponentException) {
                    val curr = rule.type.lowerHyphenName()
                    val subject = subjectType.topType.lowerHyphenName()
                    val message = """
                        来自'$subject'与'$curr'的组件适配性问题
                        组件${curr}:${wrapper.type} 与 ${subject}:${subjectType.typeName} 不兼容
                        ${ex.message}
                    """.trimIndent()
                    throw ComponentException.compatibility(message)
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
            throwComponentException("Processor $processorName not found", ComponentFailureType.PROCESSOR_NOT_FOUND)
        }

        log.info("Processor:'$processorName' destroying")
        val processor = container.get(processorBeanName, processorTypeRef).get()
        val safeTask = processor.safeTask()
        componentManager.getAllTrigger().forEach {
            val removed = it.removeTask(safeTask)
            if (removed) {
                log.info("Processor:'$processorName' removed from trigger:'${it::class.simpleName}'")
            }
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

    private fun createOptions(
        config: ProcessorConfig,
        group: String?
    ): ProcessorOptions {
        val options = config.options
        checkOptions(options)
        val expressionFactory = options.expression.factory
        val sourceItemFilter = mutableListOf<SourceItemFilter>()
        if (options.saveProcessingContent) {
            sourceItemFilter.add(SourceHashingItemFilter(config.name, processingStorage))
        }
        if (options.itemExpressionExclusions.isNotEmpty() || options.itemExpressionInclusions.isNotEmpty()) {
            sourceItemFilter.add(
                ExpressionItemFilter(
                    options.itemExpressionExclusions,
                    options.itemExpressionInclusions,
                    expressionFactory
                )
            )
        }
        sourceItemFilter.addAll(
            config.options.sourceItemFilters.map {
                componentManager.getComponent(
                    ComponentTopType.SOURCE_ITEM_FILTER,
                    it,
                    sourceItemFilterTypeRef,
                ).getAndMarkRef(config.name)
            }
        )

        val itemContentFilters = mutableListOf<ItemContentFilter>()
        if (options.contentExpressionExclusions.isNotEmpty() || options.contentExpressionInclusions.isNotEmpty()) {
            itemContentFilters.add(
                ExpressionItemContentFilter(
                    options.contentExpressionExclusions,
                    options.contentExpressionInclusions,
                    expressionFactory
                )
            )
        }
        itemContentFilters.addAll(
            config.options.itemContentFilters.map {
                componentManager.getComponent(
                    ComponentTopType.ITEM_CONTENT_FILTER,
                    it,
                    itemContentFilterTypeRef,
                ).getAndMarkRef(config.name)
            }
        )

        val fileContentFilters = mutableListOf<FileContentFilter>()
        if (options.fileExpressionExclusions.isNotEmpty() || options.fileExpressionInclusions.isNotEmpty()) {
            fileContentFilters.add(
                ExpressionFileFilter(
                    options.fileExpressionExclusions,
                    options.fileExpressionInclusions,
                    expressionFactory
                )
            )
        }
        fileContentFilters.addAll(
            config.options.fileContentFilters.map {
                componentManager.getComponent(
                    ComponentTopType.FILE_CONTENT_FILTER,
                    it,
                    fileContentFilterTypeRef,
                ).getAndMarkRef(config.name)
            }
        )

        val listeners = options.processListeners.groupBy({ it.mode }, {
            val cp = componentManager.getComponent(
                ComponentTopType.PROCESS_LISTENER,
                it.id,
                processListenerTypeRef,
            ).getAndMarkRef(config.name)
            NamedProcessListener(it.id, cp)
        }).toMutableMap()
        if (options.deleteEmptyDirectory) {
            val named = NamedProcessListener(ComponentId("delete-empty-directory"), DeleteEmptyDirectory)
            listeners.addToCollection(ListenerMode.EACH, named)
        }
        if (options.touchItemDirectory) {
            val named = NamedProcessListener(ComponentId("touch-item-directory"), TouchItemDirectory)
            listeners.addToCollection(ListenerMode.EACH, named)
        }

        val taggers = options.fileTaggers.map {
            componentManager.getComponent(
                ComponentTopType.TAGGER,
                it,
                fileTaggerTypeRef,
            ).getAndMarkRef(config.name)
        }

        val providers = config.options.variableProviders.map {
            componentManager.getComponent(
                ComponentTopType.VARIABLE_PROVIDER,
                it,
                variableProviderTypeRef,
            ).getAndMarkRef(config.name)
        }.toMutableList()

        val fileReplacementDecider = componentManager.getComponent(
            ComponentTopType.FILE_REPLACEMENT_DECIDER,
            options.fileReplacementDecider,
            fileReplacementDeciderRef,
        ).getAndMarkRef(config.name)

        val fileExistsDetector = options.fileExistsDetector?.let {
            componentManager.getComponent(
                ComponentTopType.FILE_EXISTS_DETECTOR,
                it,
                fileExistsDetectorTypeRef,
            ).getAndMarkRef(config.name)
        } ?: SimpleFileExistsDetector

        val cpReplacers = options.variableReplacers.map {
            val cp = componentManager.getComponent(
                ComponentTopType.VARIABLE_REPLACER,
                it.id,
                variableReplacerTypeRef,
            ).getAndMarkRef(config.name)
            KeyFilterVariableReplacer(cp, it.keys)
        }
        val replacers = buildSet {
            this.addAll(cpReplacers)
            this.addAll(options.regexVariableReplacers)
            if (options.supportWindowsPlatformPath) {
                this.add(WindowsPathReplacer)
            }
        }.toList()
        val fileGrouping = applyFileGrouping(options, config)
        val itemGrouping = applyItemGrouping(options, config)

        val variableProcessChain = options.variableProcess.mapValues { (_, cfg) ->
            val chain = cfg.chain.map {
                componentManager.getComponent(
                    ComponentTopType.VARIABLE_PROVIDER,
                    it,
                    variableProviderTypeRef
                ).getAndMarkRef(config.name)
            }
            VariableProcessChain(chain, cfg.output)
        }

        return ProcessorOptions(
            CorePathPattern(options.savePathPattern, expressionFactory),
            CorePathPattern(options.filenamePattern, expressionFactory),
            providers,
            listeners,
            sourceItemFilter,
            itemContentFilters,
            fileContentFilters,
            taggers,
            replacers,
            fileReplacementDecider,
            itemGrouping,
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
            options.itemErrorContinue,
            fileExistsDetector,
            options.channelBufferSize,
            options.recordMinimized,
            options.parallelism,
            options.retryBackoffMills,
            options.taskGroup ?: group ?: config.source.typeName(),
            variableProcessChain
        )
    }

    private fun checkOptions(options: ProcessorConfig.Options) {
        if (options.parallelism < 1) {
            throw IllegalArgumentException("parallelism must be greater than 0")
        }
    }

    private fun applyFileGrouping(
        options: ProcessorConfig.Options,
        config: ProcessorConfig
    ): Map<SourceFilePartition, FileOption> {
        val fileGrouping = mutableMapOf<SourceFilePartition, FileOption>()
        val expressionFactory = options.expression.factory
        for (fileOption in options.fileGrouping) {
            var addFlag = false
            val fileContentFilters = mutableListOf<FileContentFilter>()
            if (fileOption.fileExpressionExclusions != null || fileOption.fileExpressionInclusions != null) {
                fileContentFilters.add(
                    ExpressionFileFilter(
                        fileOption.fileExpressionExclusions ?: emptyList(),
                        fileOption.fileExpressionInclusions ?: emptyList(),
                        expressionFactory
                    )
                )
                addFlag = true
            }

            val filters = fileOption.fileContentFilters?.map {
                componentManager.getComponent(
                    ComponentTopType.FILE_CONTENT_FILTER,
                    it,
                    fileContentFilterTypeRef,
                ).getAndMarkRef(config.name)
            }
            if (filters != null) {
                fileContentFilters.addAll(filters)
                addFlag = true
            }

            val matcher = if (fileOption.tags.isNotEmpty()) {
                TagSourceFilePartition(fileOption.tags)
            } else if (fileOption.expressionMatching != null) {
                val expression = expressionFactory.create(
                    fileOption.expressionMatching,
                    Boolean::class.java,
                    sourceFileDefs()
                )
                ExpressionSourceFilePartition(expression)
            } else {
                throw ComponentException.other("fileGrouping must have tags or expressionMatching")
            }


            fileGrouping[matcher] = FileOption(
                fileOption.savePathPattern?.let { CorePathPattern(it, expressionFactory) },
                fileOption.filenamePattern?.let { CorePathPattern(it, expressionFactory) },
                fileContentFilters.takeIf { addFlag }
            )
        }
        return fileGrouping
    }

    private fun applyItemGrouping(
        options: ProcessorConfig.Options,
        config: ProcessorConfig
    ): Map<SourceItemPartition, ItemOption> {
        val fileGrouping = mutableMapOf<SourceItemPartition, ItemOption>()
        val expressionFactory = options.expression.factory
        for (itemOption in options.itemGrouping) {
            val expressionFilters =
                if (itemOption.itemExpressionInclusions != null || itemOption.itemExpressionExclusions != null) {
                    val filters = mutableListOf<SourceItemFilter>()
                    filters.add(
                        ExpressionItemFilter(
                            itemOption.itemExpressionExclusions ?: emptyList(),
                            itemOption.itemExpressionInclusions ?: emptyList()
                        )
                    )
                    filters
                } else {
                    null
                }

            val sourceItemFilters = itemOption.sourceItemFilters?.map {
                componentManager.getComponent(
                    ComponentTopType.SOURCE_ITEM_FILTER,
                    it,
                    sourceItemFilterTypeRef,
                ).getAndMarkRef(config.name)
            }

            val matcher = if (itemOption.tags.isNotEmpty()) {
                TagSourceItemPartition(itemOption.tags)
            } else if (itemOption.expressionMatching != null) {
                val expression =
                    expressionFactory.create(itemOption.expressionMatching, Boolean::class.java, sourceItemDefs())
                ExpressionSourceItemPartition(expression)
            } else {
                throw ComponentException.other("itemGrouping must have tags or expressionMatching")
            }

            val providers = itemOption.variableProviders?.map {
                componentManager.getComponent(ComponentTopType.VARIABLE_PROVIDER, it, variableProviderTypeRef)
                    .getAndMarkRef(config.name)
            }

            if (expressionFilters != null || sourceItemFilters != null) {
                fileGrouping[matcher] = ItemOption(
                    itemOption.savePathPattern?.let { CorePathPattern(it, expressionFactory) },
                    itemOption.filenamePattern?.let { CorePathPattern(it, expressionFactory) },
                    buildList {
                        // 内置
                        add(SourceHashingItemFilter(config.name, processingStorage))
                        addAll(expressionFilters ?: emptyList());
                        addAll(sourceItemFilters ?: emptyList())
                    },
                    providers
                )
            } else {
                fileGrouping[matcher] = ItemOption(
                    itemOption.savePathPattern?.let { CorePathPattern(it, expressionFactory) },
                    itemOption.filenamePattern?.let { CorePathPattern(it, expressionFactory) },
                    null,
                    providers
                )
            }
        }
        return fileGrouping
    }
}