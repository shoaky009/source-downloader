package io.github.shoaky.sourcedownloader.core.component

import io.github.shoaky.sourcedownloader.core.ObjectContainer
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.ComponentException
import io.github.shoaky.sourcedownloader.sdk.component.ComponentSupplier
import io.github.shoaky.sourcedownloader.sdk.component.ComponentType
import io.github.shoaky.sourcedownloader.sdk.component.SdComponent
import io.github.shoaky.sourcedownloader.util.Events
import org.springframework.beans.factory.DisposableBean
import java.util.concurrent.ConcurrentHashMap

class DefaultComponentManager(
    private val objectContainer: ObjectContainer,
) : ComponentManager, DisposableBean {

    private val componentSuppliers: MutableMap<ComponentType, ComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun createComponent(name: String, componentType: ComponentType, props: Properties) {
        val beanName = componentType.instanceName(name)
        val exists = objectContainer.contains(beanName)
        if (exists) {
            throw ComponentException.instanceExists("component $beanName already exists, check your config.yaml and remove duplicate component")
        }

        val supplier = getSupplier(componentType)
        // FIXME 创建顺序问题
        val otherTypes = supplier.supplyTypes().filter { it != componentType }
        val singletonNames = objectContainer.getAllObjectNames()
        for (otherType in otherTypes) {
            val typeBeanName = otherType.instanceName(name)
            if (singletonNames.contains(typeBeanName)) {
                val component = objectContainer.get(typeBeanName, SdComponent::class.java)
                objectContainer.put(beanName, component)
                return
            }
        }

        val component = supplier.apply(props)
        if (objectContainer.contains(beanName).not()) {
            objectContainer.put(beanName, component)
            Events.register(component)
        }
    }

    override fun getAllProcessor(): List<SourceProcessor> {
        return objectContainer.getObjectsOfType(SourceProcessor::class.java).values.toList()
    }

    override fun getComponent(name: String): SdComponent? {
        return kotlin.runCatching {
            objectContainer.get(name, SdComponent::class.java)
        }.getOrNull()
    }

    override fun getAllComponent(): List<SdComponent> {
        return objectContainer.getObjectsOfType(SdComponent::class.java).values.toList()
    }

    override fun getSupplier(type: ComponentType): ComponentSupplier<*> {
        return componentSuppliers[type]
            ?: throw ComponentException.unsupported("Supplier不存在, 组件类型:${type.topTypeClass.simpleName}:${type.typeName}")
    }

    override fun getSuppliers(): List<ComponentSupplier<*>> {
        return componentSuppliers.values.toList()
    }

    override fun registerSupplier(vararg sdComponentSuppliers: ComponentSupplier<*>) {
        for (componentSupplier in sdComponentSuppliers) {
            val types = componentSupplier.supplyTypes()
            for (type in types) {
                if (this.componentSuppliers.containsKey(type)) {
                    throw ComponentException.supplierExists("组件类型已存在:${type}")
                }
                this.componentSuppliers[type] = componentSupplier
            }
        }
    }

    override fun destroy(instanceName: String) {
        if (objectContainer.contains(instanceName)) {
            val bean = objectContainer.get(instanceName)
            if (bean is AutoCloseable) {
                bean.close()
            }
            objectContainer.remove(instanceName)
        }
    }

    override fun getAllComponentNames(): Set<String> {
        val type = objectContainer.getObjectsOfType(SdComponent::class.java)
        return type.keys
    }

    private val _componentDescriptions by lazy {
        val descriptor = DescriptionLoader.load()
        val descriptionMapping = descriptor.component.associateBy { it.id }
        componentSuppliers.values.distinct()
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

    private fun componentTypeDescriptors(supplier: ComponentSupplier<*>) =
        supplier.supplyTypes().map { type ->
            ComponentTypeDescriptor(
                type.topType(),
                type.typeName
            )
        }

    private fun ruleDescriptors(supplier: ComponentSupplier<*>) =
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