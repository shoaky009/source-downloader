package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.SourceDownloaderApplication.Companion.log
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.util.Events
import java.util.concurrent.ConcurrentHashMap

interface SdComponentManager {

    fun createComponent(name: String, componentType: ComponentType, props: ComponentProps)

    fun getAllProcessor(): List<SourceProcessor>
    fun getProcessor(name: String): SourceProcessor?

    fun getComponent(name: String): SdComponent?

    fun getAllComponent(): List<SdComponent>
    fun getSupplier(type: ComponentType): SdComponentSupplier<*>
    fun getSuppliers(): List<SdComponentSupplier<*>>
    fun registerSupplier(vararg sdComponentSuppliers: SdComponentSupplier<*>)

    fun removeComponent(name: String, componentType: ComponentType)

    fun getAllTrigger(): List<Trigger> {
        return getAllComponent().filterIsInstance<Trigger>()
    }

    fun getAllSource(): List<Source> {
        return getAllComponent().filterIsInstance<Source>()
    }

    fun getAllDownloader(): List<Downloader> {
        return getAllComponent().filterIsInstance<Downloader>()
    }

    fun getAllMover(): List<FileMover> {
        return getAllComponent().filterIsInstance<FileMover>()
    }

    fun getAllProvider(): List<VariableProvider> {
        return getAllComponent().filterIsInstance<VariableProvider>()
    }

}

@Component
class SpringSdComponentManager(
    private val applicationContext: DefaultListableBeanFactory,
) : SdComponentManager {

    private val sdComponentSuppliers: MutableMap<ComponentType, SdComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun createComponent(name: String, componentType: ComponentType, props: ComponentProps) {
        val beanName = componentType.instanceName(name)
        val exists = applicationContext.containsSingleton(beanName)
        if (exists) {
            log.info("组件已存在: $beanName")
            return
        }

        val supplier = getSupplier(componentType)
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

        val component = supplier.apply(props)
        if (applicationContext.containsBean(beanName).not()) {
            applicationContext.registerSingleton(beanName, component)
            Events.register(component)
        }
    }

    override fun getAllProcessor(): List<SourceProcessor> {
        return applicationContext.getBeansOfType(SourceProcessor::class.java).values.toList()
    }

    override fun getProcessor(name: String): SourceProcessor? {
        return applicationContext.getBeansOfType(SourceProcessor::class.java)
            .filter { it.value.name == name }.map { it.value }.firstOrNull()
    }

    override fun getComponent(name: String): SdComponent? {
        return kotlin.runCatching {
            applicationContext.getBean(name, SdComponent::class.java)
        }.getOrNull()
    }

    override fun getAllComponent(): List<SdComponent> {
        return applicationContext.getBeansOfType(SdComponent::class.java).values.toList()
    }

    override fun getSupplier(type: ComponentType): SdComponentSupplier<*> {
        return sdComponentSuppliers[type]
            ?: throw ComponentException.unsupported("Supplier不存在, 组件类型:${type.klass.simpleName}:${type.typeName}")
    }

    override fun getSuppliers(): List<SdComponentSupplier<*>> {
        return sdComponentSuppliers.values.toList()
    }

    override fun registerSupplier(vararg sdComponentSuppliers: SdComponentSupplier<*>) {
        for (componentSupplier in sdComponentSuppliers) {
            val types = componentSupplier.supplyTypes()
            for (type in types) {
                if (this.sdComponentSuppliers.containsKey(type)) {
                    throw ComponentException.supplierExists("组件类型已存在:${type}")
                }
                this.sdComponentSuppliers[type] = componentSupplier
            }
        }
    }

    override fun removeComponent(name: String, componentType: ComponentType) {
        val beanName = componentType.instanceName(name)
        if (applicationContext.containsSingleton(beanName)) {
            applicationContext.destroySingleton(beanName)
        }
    }
}