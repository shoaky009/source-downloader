package xyz.shoaky.sourcedownloader.core

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.stereotype.Component
import xyz.shoaky.sourcedownloader.sdk.component.*
import xyz.shoaky.sourcedownloader.util.Events
import java.util.concurrent.ConcurrentHashMap

interface ComponentManagerV2 {

    fun createComponent(name: String, componentType: ComponentType, props: ComponentProps)

    fun getAllProcessor(): List<SourceProcessor>
    fun getProcessor(name: String): SourceProcessor?

    fun getComponent(name: String): SdComponent?

    fun getAllComponent(): List<SdComponent>

    fun getAllSupplier(): List<SdComponentSupplier<*>>

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
class SpringComponentManager(
    private val applicationContext: DefaultListableBeanFactory,
) : ComponentManagerV2 {

    val sdComponentSuppliers: MutableMap<ComponentType, SdComponentSupplier<*>> = ConcurrentHashMap()
    override fun createComponent(name: String, componentType: ComponentType, props: ComponentProps) {
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

    override fun getAllSupplier(): List<SdComponentSupplier<*>> {
        return sdComponentSuppliers.values.toList()
    }

    private fun getSupplierByType(componentType: ComponentType): SdComponentSupplier<*> {
        return sdComponentSuppliers[componentType]
            ?: throw ComponentException("Supplier不存在, 组件类型:${componentType.klass.simpleName}:${componentType.typeName}")
    }

}