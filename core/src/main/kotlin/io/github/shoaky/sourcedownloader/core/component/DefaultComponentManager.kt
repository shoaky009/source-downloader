package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.core.processor.log
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.util.Events
import org.springframework.beans.factory.DisposableBean
import java.util.concurrent.ConcurrentHashMap

class DefaultComponentManager(
    private val objectContainer: ObjectWrapperContainer,
    private val configStorages: List<ComponentConfigStorage>
) : ComponentManager, DisposableBean {

    private val componentSuppliers: MutableMap<ComponentType, ComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun <T : SdComponent> getComponent(
        type: ComponentTopType,
        id: ComponentId,
        typeReference: TypeReference<ComponentWrapper<T>>,
    ): ComponentWrapper<T> {

        val instanceName = id.getInstanceName(type.klass)
        if (objectContainer.contains(instanceName)) {
            return objectContainer.get(instanceName, typeReference)
        }

        val typeName = id.typeName()
        val name = id.name()
        val componentType = ComponentType.of(type, typeName)
        val props = if (name == typeName) {
            val supplier = getSupplier(componentType)
            if (supplier.autoCreateDefault()) {
                Properties.EMPTY
            } else {
                val values = findConfig(type, typeName, name).props
                Properties.fromMap(values)
            }
        } else {
            val values = findConfig(type, typeName, name).props
            Properties.fromMap(values)
        }

        val supplier = getSupplier(componentType)
        // FIXME 创建顺序问题
        val otherTypes = supplier.supplyTypes().filter { it != componentType }
        val singletonNames = objectContainer.getAllObjectNames()
        for (otherType in otherTypes) {
            val typeBeanName = otherType.instanceName(name)
            if (singletonNames.contains(typeBeanName)) {
                val component = objectContainer.get(typeBeanName, typeReference)
                val componentWrapper = ComponentWrapper(
                    componentType,
                    name,
                    props,
                    component.get(),
                    false
                )
                objectContainer.put(instanceName, componentWrapper)
                return componentWrapper
            }
        }

        val component = try {
            supplier.apply(props)
        } catch (e: ComponentException) {
            throw ComponentException.other("Create component $instanceName failed cause by ${e.message}")
        }

        val componentWrapper = ComponentWrapper(
            componentType,
            name,
            props,
            component
        )

        objectContainer.put(instanceName, componentWrapper)
        Events.register(componentWrapper)

        log.info("Successfully created component ${type}:${typeName}:${name}")
        @Suppress("UNCHECKED_CAST")
        return componentWrapper as ComponentWrapper<T>
    }

    override fun getAllProcessor(): List<SourceProcessor> {
        return objectContainer.getObjectsOfType(jacksonTypeRef<ProcessorWrapper>())
            .values.map { it.get() }.toList()
    }

    override fun getComponent(type: ComponentType, name: String): ComponentWrapper<SdComponent>? {
        val instanceName = type.instanceName(name)
        return kotlin.runCatching {
            objectContainer.get(instanceName, jacksonTypeRef<ComponentWrapper<SdComponent>>())
        }.getOrNull()
    }

    override fun getAllComponent(): List<ComponentWrapper<SdComponent>> {
        return objectContainer.getObjectsOfType(componentWrapperTypeRef).values.toList()
    }

    override fun getSupplier(type: ComponentType): ComponentSupplier<*> {
        return componentSuppliers[type]
            ?: throw ComponentException.unsupported("Supplier不存在, 组件类型:${type.topTypeClass.simpleName}:${type.typeName}")
    }

    override fun getSuppliers(): List<ComponentSupplier<*>> {
        return componentSuppliers.values.toList()
    }

    override fun registerSupplier(vararg componentSuppliers: ComponentSupplier<*>) {
        for (componentSupplier in componentSuppliers) {
            val types = componentSupplier.supplyTypes()
            for (type in types) {
                if (this.componentSuppliers.containsKey(type)) {
                    throw ComponentException.supplierExists("组件类型已存在:${type}")
                }
                this.componentSuppliers[type] = componentSupplier
            }
        }
    }

    override fun getAllComponentNames(): Set<String> {
        val type = objectContainer.getObjectsOfType(componentWrapperTypeRef)
        return type.keys
    }

    private fun findConfig(type: ComponentTopType, typeName: String, name: String): ComponentConfig {
        val config = configStorages.firstNotNullOfOrNull {
            runCatching { it.getComponentConfig(type, typeName, name) }.getOrDefault(null)
        }
        return config ?: throw ComponentException.missing("No component config found for $type:$typeName:$name")
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

    override fun destroy(type: ComponentType, name: String) {
        val instanceName = type.instanceName(name)
        if (objectContainer.contains(instanceName)) {
            val wrapper = objectContainer.get(instanceName)
            val obj = wrapper.get()
            if (obj is AutoCloseable) {
                obj.close()
            }
            objectContainer.remove(instanceName)
            log.info("Destroy component $instanceName")
            val supplier = componentSuppliers.getValue(type)
            supplier.supplyTypes().filter { type != it }
                .forEach {
                    destroy(it, name)
                }
            Events.unregister(wrapper)
        }
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
        val components = getAllComponent()
        for (component in components) {
            destroy(component.type, component.name)
        }
    }

    companion object {

        private val componentWrapperTypeRef = jacksonTypeRef<ComponentWrapper<SdComponent>>()
    }
}