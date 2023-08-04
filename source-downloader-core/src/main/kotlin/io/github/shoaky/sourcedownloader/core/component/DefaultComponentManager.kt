package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
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
    private val objectWrapperContainer: ObjectWrapperContainer,
) : ComponentManager, DisposableBean {

    private val componentSuppliers: MutableMap<ComponentType, ComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun createComponent(componentType: ComponentType, name: String, props: Properties) {
        val beanName = componentType.instanceName(name)
        val exists = objectWrapperContainer.contains(beanName)
        if (exists) {
            throw ComponentException.instanceExists("component $beanName already exists, check your config.yaml and remove duplicate component")
        }

        val supplier = getSupplier(componentType)
        // FIXME 创建顺序问题
        val otherTypes = supplier.supplyTypes().filter { it != componentType }
        val singletonNames = objectWrapperContainer.getAllObjectNames()
        for (otherType in otherTypes) {
            val typeBeanName = otherType.instanceName(name)
            if (singletonNames.contains(typeBeanName)) {
                val component = objectWrapperContainer.get(typeBeanName, componentWrapperTypeRef)
                val componentWrapper = ComponentWrapper(
                    componentType,
                    name,
                    props,
                    component.get(),
                    false
                )
                objectWrapperContainer.put(beanName, componentWrapper)
                return
            }
        }

        val component = supplier.apply(props)
        if (objectWrapperContainer.contains(beanName).not()) {
            val componentWrapper = ComponentWrapper(
                componentType,
                name,
                props,
                component
            )

            objectWrapperContainer.put(beanName, componentWrapper)
            Events.register(componentWrapper)
        }
    }

    override fun getAllProcessor(): List<SourceProcessor> {
        return objectWrapperContainer.getObjectsOfType(jacksonTypeRef<ProcessorWrapper>())
            .values.map { it.get() }.toList()
    }

    override fun getComponent(type: ComponentType, name: String): ComponentWrapper<SdComponent>? {
        val instanceName = type.instanceName(name)
        return kotlin.runCatching {
            objectWrapperContainer.get(instanceName, jacksonTypeRef<ComponentWrapper<SdComponent>>())
        }.getOrNull()
    }

    override fun getAllComponent(): List<ComponentWrapper<SdComponent>> {
        return objectWrapperContainer.getObjectsOfType(componentWrapperTypeRef).values.toList()
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

    override fun destroy(instanceName: String) {
        if (objectWrapperContainer.contains(instanceName)) {
            val wrapper = objectWrapperContainer.get(instanceName)
            val obj = wrapper.get()
            if (obj is AutoCloseable) {
                obj.close()
            }
            objectWrapperContainer.remove(instanceName)
            Events.unregister(wrapper)
        }
    }

    override fun getAllComponentNames(): Set<String> {
        val type = objectWrapperContainer.getObjectsOfType(componentWrapperTypeRef)
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

    companion object {

        private val componentWrapperTypeRef = jacksonTypeRef<ComponentWrapper<SdComponent>>()
    }
}