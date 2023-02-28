package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.core.component.RegexSourceFilter
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

@Component
class ComponentManager(
    private val processingStorage: ProcessingStorage,
    private val applicationContext: ConfigurableBeanFactory,
) {

    private val componentSuppliers: MutableMap<ComponentType, ComponentSupplier<*>> = ConcurrentHashMap()

    fun fullyCreateSourceProcessor(config: ProcessorConfig): SourceProcessor {
        val source = applicationContext.getBean(config.getSourceInstanceName(), Source::class.java)
        val downloader = applicationContext.getBean(config.getDownloaderInstanceName(), Downloader::class.java)
        val creator = applicationContext.getBean(config.getCreatorInstanceName(), SourceContentCreator::class.java)
        val mover = applicationContext.getBean(config.getMoverInstanceName(), FileMover::class.java)
        val processor = SourceProcessor(
            config.name,
            source,
            creator,
            downloader,
            mover,
            config.fileMode,
            config.savePath,
            config.options,
            processingStorage,
        )

        initOptions(config.options, processor)

        val name = "Processor-${config.name}"
        applicationContext.registerSingleton(name, processor)
        log.info("处理器创建成功:${Jackson.toJsonString(config)}")
        processor.printProcessorInfo()

        val trigger = applicationContext.getBean(config.getTriggerInstanceName(), Trigger::class.java)
        trigger.addTask(processor.safeTask())
        return processor
    }

    private fun initOptions(options: ProcessorConfig.Options, processor: SourceProcessor) {
        val filterWords = options.blacklistRegex
        if (filterWords.isNotEmpty()) {
            processor.addItemFilter(RegexSourceFilter(filterWords))
        }

        val runAfterCompletion = options.runAfterCompletion
        runAfterCompletion.forEach {
            val instanceName = it.getInstanceName(RunAfterCompletion::class)
            applicationContext.getBean(instanceName, RunAfterCompletion::class.java)
                .also { completion -> processor.addRunAfterCompletion(completion) }
        }
        processor.scheduleRenameTask(options.renameTaskInterval)
    }

    @Synchronized
    fun createComponent(componentType: ComponentType, name: String, props: Map<String, Any>) {
        val beanName = componentType.instanceName(name)
        val exists = applicationContext.containsSingleton(beanName)
        if (exists) {
            return
        }

        val supplier = componentSuppliers[componentType]
            ?: throw RuntimeException("Supplier不存在, 组件类型:${componentType.klass.simpleName}:${componentType.typeName}")

        //TODO 创建顺序问题
        val otherTypes = supplier.availableTypes().filter { it != componentType }
        val singletonNames = applicationContext.singletonNames.toSet()
        for (otherType in otherTypes) {
            val typeBeanName = otherType.instanceName(name)
            if (singletonNames.contains(typeBeanName)) {
                val component = applicationContext.getBean(typeBeanName)
                applicationContext.registerSingleton(beanName, component)
                return
            }
        }

        val component = supplier.apply(ComponentProps.fromMap(props))
        if (applicationContext.containsBean(beanName).not()) {
            applicationContext.registerSingleton(beanName, component)
        }
    }

    fun registerSupplier(vararg componentSuppliers: ComponentSupplier<*>) {
        for (componentSupplier in componentSuppliers) {
            val types = componentSupplier.availableTypes()
            for (type in types) {
                if (this.componentSuppliers.containsKey(type)) {
                    log.info("组件类型已存在:{}", type)
                    continue
                }
                this.componentSuppliers[type] = componentSupplier
            }
        }
    }

    fun removeSupplier(componentType: ComponentType, name: String) {
        componentSuppliers.remove(componentType)
    }

    fun getSuppliers(filter: Predicate<ComponentSupplier<*>> = Predicate<ComponentSupplier<*>> { true })
            : List<ComponentSupplier<*>> {
        return componentSuppliers.values.filter { filter.test(it) }.toList()
    }

}