package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.core.component.RegexSourceItemFilter
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.sdk.util.Jackson
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

@Component
class ComponentManager(
    private val processingStorage: ProcessingStorage,
    private val applicationContext: ConfigurableBeanFactory,
) {

    private val sdComponentSuppliers: MutableMap<ComponentType, SdComponentSupplier<*>> = ConcurrentHashMap()

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

        listOf(
            config.source.getComponentType(Source::class),
            config.downloader.getComponentType(Downloader::class),
            config.mover.getComponentType(FileMover::class),
            config.creator.getComponentType(SourceContentCreator::class)
        ).forEach {
            check(it, listOf(source, creator, downloader, mover))
        }

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
            processor.addItemFilter(RegexSourceItemFilter(filterWords))
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

        val supplier = getSupplierByType(componentType)

        //TODO 创建顺序问题
        val otherTypes = supplier.supplyTypes().filter { it != componentType }
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

    private fun getSupplierByType(componentType: ComponentType): SdComponentSupplier<*> {
        return sdComponentSuppliers[componentType]
            ?: throw ComponentException("Supplier不存在, 组件类型:${componentType.klass.simpleName}:${componentType.typeName}")
    }

    fun registerSupplier(vararg sdComponentSuppliers: SdComponentSupplier<*>) {
        for (componentSupplier in sdComponentSuppliers) {
            val types = componentSupplier.supplyTypes()
            for (type in types) {
                if (this.sdComponentSuppliers.containsKey(type)) {
                    log.info("组件类型已存在:{}", type)
                    continue
                }
                this.sdComponentSuppliers[type] = componentSupplier
            }
        }
    }

    fun removeSupplier(componentType: ComponentType, name: String) {
        sdComponentSuppliers.remove(componentType)
    }

    fun getSuppliers(filter: Predicate<SdComponentSupplier<*>> = Predicate<SdComponentSupplier<*>> { true })
            : List<SdComponentSupplier<*>> {
        return sdComponentSuppliers.values.filter { filter.test(it) }.toList()
    }

    fun check(componentType: ComponentType, components: List<SdComponent>) {
        val supplier = getSupplierByType(componentType)
        val rules = supplier.rules()
        for (rule in rules) {
            components.forEach {
                rule.verify(it)
            }
        }
    }

}