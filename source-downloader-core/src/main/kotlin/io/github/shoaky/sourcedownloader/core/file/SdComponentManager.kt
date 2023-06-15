package io.github.shoaky.sourcedownloader.core.file

import com.fasterxml.jackson.module.kotlin.convertValue
import io.github.shoaky.sourcedownloader.core.component.RuleDescriptor
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.SourceItemPointer
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.util.Events
import io.github.shoaky.sourcedownloader.util.jackson.yamlMapper
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrElse

interface SdComponentManager {

    fun createComponent(name: String, componentType: ComponentType, props: Properties)

    fun getAllProcessor(): List<SourceProcessor>

    fun getComponent(name: String): SdComponent?

    fun getAllComponent(): List<SdComponent>

    fun getSupplier(type: ComponentType): SdComponentSupplier<*>

    fun getSuppliers(): List<SdComponentSupplier<*>>

    fun registerSupplier(vararg sdComponentSuppliers: SdComponentSupplier<*>)

    fun getAllComponentNames(): Set<String>

    fun getAllTrigger(): List<Trigger> {
        return getAllComponent().filterIsInstance<Trigger>()
    }

    fun getAllSource(): List<Source<SourceItemPointer>> {
        return getAllComponent().filterIsInstance<Source<SourceItemPointer>>()
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

    fun getComponentDescriptions(): List<ComponentDescription>

    fun destroy(instanceName: String)
}

@Component
class SpringSdComponentManager(
    private val applicationContext: DefaultListableBeanFactory,
) : SdComponentManager, DisposableBean {

    private val sdComponentSuppliers: MutableMap<ComponentType, SdComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun createComponent(name: String, componentType: ComponentType, props: Properties) {
        val beanName = componentType.instanceName(name)
        val exists = applicationContext.containsSingleton(beanName)
        if (exists) {
            throw ComponentException.instanceExists("component $beanName already exists, check your config.yaml and remove duplicate component")
        }

        val supplier = getSupplier(componentType)
        // FIXME 创建顺序问题
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
            ?: throw ComponentException.unsupported("Supplier不存在, 组件类型:${type.topTypeClass.simpleName}:${type.typeName}")
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

    override fun destroy(instanceName: String) {
        if (applicationContext.containsSingleton(instanceName)) {
            val bean = applicationContext.getBean(instanceName)
            if (bean is AutoCloseable) {
                bean.close()
            }
            applicationContext.destroySingleton(instanceName)
        }
    }

    override fun getAllComponentNames(): Set<String> {
        val type = applicationContext.getBeansOfType(SdComponent::class.java)
        return type.keys
    }

    private val _componentDescriptions by lazy {
        val descriptor = DescriptionLoader.load()
        val descriptionMapping = descriptor.component.associateBy { it.id }
        sdComponentSuppliers.values.distinct()
            .map { supplier ->
                val name = supplier::class.java.name
                val description = descriptionMapping[name] ?: ComponentDescription(name)
                description.apply {
                    this.rules = ruleDescriptors(supplier)
                    this.types = componentTypeDescriptors(supplier)
                }
                description
            }
    }

    override fun getComponentDescriptions(): List<ComponentDescription> {
        return _componentDescriptions
    }

    private fun componentTypeDescriptors(supplier: SdComponentSupplier<*>) =
        supplier.supplyTypes().map { type ->
            ComponentTypeDescriptor(
                type.topType(),
                type.typeName
            )
        }

    private fun ruleDescriptors(supplier: SdComponentSupplier<*>) =
        supplier.rules().map { rule ->
            RuleDescriptor(
                if (rule.isAllow) "允许" else "禁止",
                rule.type.lowerHyphenName(),
                rule.value.simpleName!!
            )
        }

    override fun destroy() {
        val componentNames = getAllComponentNames()
        for (name in componentNames) {
            destroy(name)
        }
    }
}

object DescriptionLoader {

    private const val DESCRIPTION_FILE = "sd-description.yaml"
    private const val COMPONENT = "component"
    private const val INSTANCE = "instance"

    fun load(): Descriptor {
        val classLoader = Thread.currentThread().contextClassLoader
        val node = classLoader.resources(DESCRIPTION_FILE).map {
            yamlMapper.readTree(it)
        }.reduce { acc, op ->
            val readerForUpdating = yamlMapper.readerForUpdating(acc)
            readerForUpdating.readValue(op)
        }.getOrElse { yamlMapper.nullNode() }
        val component = node[COMPONENT]
        val instance = node[INSTANCE]
        return Descriptor(
            yamlMapper.convertValue(instance),
            yamlMapper.convertValue(component),
        )
    }
}

data class Descriptor(
    val instance: List<InstanceDescription> = emptyList(),
    val component: List<ComponentDescription> = emptyList()
)

data class InstanceDescription(
    val id: String,
    override val name: String = id,
    override val description: String,
    val properties: List<PropertyDescriptor> = emptyList(),
) : CommonDescriptor(name, description, null)

data class ComponentDescription(
    val id: String,
    val description: String? = null,
    val properties: List<PropertyDescriptor> = emptyList(),
    val variables: List<VariableDescriptor> = emptyList(),
    var types: List<ComponentTypeDescriptor> = emptyList(),
    var rules: List<RuleDescriptor> = emptyList(),
)

data class ComponentTypeDescriptor(
    val topType: ComponentTopType,
    val subType: String,
)

data class PropertyDescriptor(
    override val name: String,
    override val description: String? = null,
    override val example: String? = null,
    val required: Boolean = true
) : CommonDescriptor(name, description, example)

data class VariableDescriptor(
    override val name: String,
    override val description: String,
    override val example: String? = null
) : CommonDescriptor(name, description, example)

open class CommonDescriptor(
    open val name: String,
    open val description: String? = null,
    open val example: String? = null
)