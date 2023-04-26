package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.component.supplier.*
import xyz.shoaky.sourcedownloader.core.processor.SourceProcessor
import xyz.shoaky.sourcedownloader.sdk.component.*

@Component
class SpringProcessorManager(
    private val processingStorage: ProcessingStorage,
    private val componentManager: SdComponentManager,
    private val applicationContext: DefaultListableBeanFactory,
) : ProcessorManager {

    override fun createProcessor(config: ProcessorConfig): SourceProcessor {
        val processorBeanName = processorBeanName(config.name)
        if (applicationContext.containsBean(processorBeanName)) {
            throw ComponentException.processorExists("Processor ${config.name} already exists")
        }

        val source = applicationContext.getBean(config.sourceInstanceName(), Source::class.java)
        val downloader = applicationContext.getBean(config.downloaderInstanceName(), Downloader::class.java)

        val providers = config.providerInstanceNames().map {
            applicationContext.getBean(it, VariableProvider::class.java)
        }
        val mover = applicationContext.getBean(config.moverInstanceName(), FileMover::class.java)
        val processor = SourceProcessor(
            config.name,
            source,
            providers,
            downloader,
            mover,
            config.savePath,
            config.options,
            processingStorage,
        )

        val mutableListOf = mutableListOf(
            config.source.getComponentType(Source::class),
            config.downloader.getComponentType(Downloader::class),
            config.fileMover.getComponentType(FileMover::class),
        )
        mutableListOf.addAll(
            config.variableProviders.map { it.getComponentType(VariableProvider::class) }
        )

        val cps = mutableListOf(source, downloader, mover)
        cps.addAll(providers)
        mutableListOf.forEach {
            check(it, cps)
        }

        val fileFilters = config.sourceFileFilters.map {
            val instanceName = it.getInstanceName(SourceFileFilter::class)
            applicationContext.getBean(instanceName, SourceFileFilter::class.java)
        }.toTypedArray()
        processor.addFileFilter(*fileFilters)

        val itemFilters = config.sourceItemFilters.map {
            val instanceName = it.getInstanceName(SourceFileFilter::class)
            applicationContext.getBean(instanceName, SourceFileFilter::class.java)
        }.toTypedArray()
        processor.addFileFilter(*itemFilters)

        initOptions(config.options, processor)

        applicationContext.registerSingleton(processorBeanName, processor)
        log.info("处理器初始化完成:$processor")

        config.triggerInstanceNames().map {
            applicationContext.getBean(it, Trigger::class.java)
        }.forEach {
            it.addTask(processor.safeTask())
        }
        return processor
    }

    override fun getProcessor(name: String): SourceProcessor? {
        val processorBeanName = processorBeanName(name)
        return if (applicationContext.containsBean(processorBeanName)) {
            applicationContext.getBean(processorBeanName, SourceProcessor::class.java)
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
        if (options.regexFilters.isNotEmpty()) {
            processor.addItemFilter(
                RegexSourceItemFilterSupplier.regexes(options.regexFilters)
            )
        }
        if (options.itemExpressionExclusions.isNotEmpty() || options.itemExpressionInclusions.isNotEmpty()) {
            processor.addItemFilter(ExpressionItemFilterSupplier.expressions(
                options.itemExpressionExclusions,
                options.itemExpressionInclusions
            ))
        }
        if (options.fileExpressionExclusions.isNotEmpty() || options.fileExpressionInclusions.isNotEmpty()) {
            processor.addFileFilter(ExpressionFileFilterSupplier.expressions(
                options.fileExpressionExclusions,
                options.fileExpressionInclusions
            ))
        }
        val runAfterCompletion = options.runAfterCompletion
        runAfterCompletion.forEach {
            val instanceName = it.getInstanceName(RunAfterCompletion::class)
            applicationContext.getBean(instanceName, RunAfterCompletion::class.java)
                .also { completion -> processor.addRunAfterCompletion(completion) }
        }
        processor.scheduleRenameTask(options.renameTaskInterval)
        if (options.cleanEmptyDirectory) {
            val cleanEmptyDirectory = CleanEmptyDirectorySupplier.apply(ComponentProps.empty())
            processor.addRunAfterCompletion(cleanEmptyDirectory)
        }
        if (options.touchItemDirectory) {
            val touchItemDirectory = TouchItemDirectorySupplier.apply(ComponentProps.empty())
            processor.addRunAfterCompletion(touchItemDirectory)
        }
        options.taggers.forEach {
            val instanceName = it.getInstanceName(FileTagger::class)
            applicationContext.getBean(instanceName, FileTagger::class.java)
                .also { tagger -> processor.addTagger(tagger) }
        }
    }

    // TODO 第二个参数应该给组件的描述对象
    private fun check(componentType: ComponentType, components: List<SdComponent>) {
        val supplier = componentManager.getSupplier(componentType)
        val rules = supplier.rules()
        for (rule in rules) {
            components.forEach {
                rule.verify(it)
            }
        }
    }

    override fun getProcessors(): List<SourceProcessor> {
        return applicationContext.getBeansOfType(SourceProcessor::class.java).values.toList()
    }

    override fun destroy(processorName: String) {
        val processorBeanName = processorBeanName(processorName)
        if (applicationContext.containsBean(processorBeanName).not()) {
            throw ComponentException.processorMissing("Processor $processorName not exists")
        }

        val processor = applicationContext.getBean(processorBeanName, SourceProcessor::class.java)
        val safeTask = processor.safeTask()
        componentManager.getAllTrigger().forEach {
            it.removeTask(safeTask)
        }
        applicationContext.destroySingleton(processorBeanName)
    }
}