package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.component.supplier.ExpressionFileFilterSupplier
import xyz.shoaky.sourcedownloader.component.supplier.ExpressionItemFilterSupplier
import xyz.shoaky.sourcedownloader.sdk.component.*

@Component
class SpringProcessorManager(
    private val processingStorage: ProcessingStorage,
    private val componentManager: SdComponentManager,
    private val applicationContext: ConfigurableBeanFactory,
) : ProcessorManager {

    override fun createProcessor(config: ProcessorConfig): SourceProcessor {
        val processorBeanName = "Processor-${config.name}"
        if (applicationContext.containsBean(processorBeanName)) {
            throw ComponentException.processor("Processor ${config.name} already exists")
        }

        val source = applicationContext.getBean(config.getSourceInstanceName(), Source::class.java)
        val downloader = applicationContext.getBean(config.getDownloaderInstanceName(), Downloader::class.java)

        val providers = config.getProviderInstanceNames().map {
            applicationContext.getBean(it, VariableProvider::class.java)
        }
        val mover = applicationContext.getBean(config.getMoverInstanceName(), FileMover::class.java)
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
            config.mover.getComponentType(FileMover::class),
        )
        mutableListOf.addAll(
            config.providers.map { it.getComponentType(VariableProvider::class) }
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

        config.getTriggerInstanceNames().map {
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
        processor.addItemFilter(ExpressionItemFilterSupplier.expressions(
            options.itemExpressionExclusions,
            options.itemExpressionInclusions
        ))

        processor.addFileFilter(ExpressionFileFilterSupplier.expressions(
            options.fileExpressionExclusions,
            options.fileExpressionInclusions
        ))

        val runAfterCompletion = options.runAfterCompletion
        runAfterCompletion.forEach {
            val instanceName = it.getInstanceName(RunAfterCompletion::class)
            applicationContext.getBean(instanceName, RunAfterCompletion::class.java)
                .also { completion -> processor.addRunAfterCompletion(completion) }
        }
        processor.scheduleRenameTask(options.renameTaskInterval)
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

}