package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.component.ExpressionFileFilterSupplier
import xyz.shoaky.sourcedownloader.component.ExpressionItemFilterSupplier
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.util.Events
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

@Component
class ComponentManager(
    private val processingStorage: ProcessingStorage,
    private val applicationContext: ConfigurableBeanFactory,
) {

    val sdComponentSuppliers: MutableMap<ComponentType, SdComponentSupplier<*>> = ConcurrentHashMap()

    fun fullyCreateSourceProcessor(config: ProcessorConfig): SourceProcessor {
        val processorBeanName = "Processor-${config.name}"
        if (applicationContext.containsBean(processorBeanName)) {
            throw ComponentException("processor ${config.name} already exists")
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

        val trigger = applicationContext.getBean(config.getTriggerInstanceName(), Trigger::class.java)
        trigger.addTask(processor.safeTask())
        return processor
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

    @Synchronized
    fun createComponent(componentType: ComponentType, name: String, props: Map<String, Any>) {
        val beanName = componentType.instanceName(name)
        val exists = applicationContext.containsSingleton(beanName)
        if (exists) {
            return
        }

        val supplier = getSupplierByType(componentType)

        // TODO 创建顺序问题
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
            Events.register(component)
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

    // TODO 第二个参数应该给组件的描述对象
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