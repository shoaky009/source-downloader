package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.base.Throwables
import io.github.shoaky.sourcedownloader.core.DefaultCoreContext
import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.throwComponentException
import io.github.shoaky.sourcedownloader.util.Events
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.jvm.jvmName

class DefaultComponentManager(
    private val objectContainer: ObjectWrapperContainer,
    private val configStorages: List<ComponentConfigStorage>
) : ComponentManager {

    private val componentSuppliers: MutableMap<ComponentType, ComponentSupplier<*>> = ConcurrentHashMap()

    @Synchronized
    override fun <T : SdComponent> getComponent(
        type: ComponentTopType,
        id: ComponentId,
        typeReference: TypeReference<ComponentWrapper<T>>,
    ): ComponentWrapper<T> {

        val targetInstanceName = id.getInstanceName(type)
        if (objectContainer.contains(targetInstanceName)) {
            return objectContainer.get(targetInstanceName, typeReference)
        }

        val targetTypeName = id.typeName()
        val targetName = id.name()
        val targetComponentType = ComponentType.of(type, targetTypeName)

        val supplier = getSupplier(targetComponentType)
        val supplyTypes = supplier.supplyTypes()
        val typesWithProp = supplyTypes.associateBy({ it }) {
            val config = findConfig(it.type, it.typeName, targetName)
            if (config == null && supplier.supportNoArgs()) {
                Properties.empty
            } else {
                val values = config?.props
                    ?: throwComponentException(
                        "No component config found for ${it.type}:${it.typeName}:$targetName",
                        ComponentFailureType.DEFINITION_NOT_FOUND
                    )
                Properties.fromMap(values)
            }
        }

        val (primaryType, props) = selectPrimaryType(supplier, typesWithProp)
        val primaryTypeBeanName = primaryType.instanceName(targetName)
        val primaryComponentWrapper = if (objectContainer.contains(primaryTypeBeanName)) {
            objectContainer.get(primaryTypeBeanName, typeReference)
        } else {
            // Create primary component
            val context = DefaultCoreContext(
                this,
                type,
                id.toString(),
            )
            val component = try {
                supplier.apply(context, props)
            } catch (e: ComponentException) {
                val rootCause = Throwables.getRootCause(e).message
                throw ComponentException.other("Component $primaryTypeBeanName ${e.message} failed cause by $rootCause")
            }
            val wrapper = ComponentWrapper(
                primaryType,
                targetName,
                props,
                component
            )
            objectContainer.put(primaryTypeBeanName, wrapper)
            log.info("Successfully created component $targetInstanceName")
            Events.register(wrapper)
            wrapper
        }
        if (targetComponentType == primaryType) {
            @Suppress("UNCHECKED_CAST")
            return primaryComponentWrapper as ComponentWrapper<T>
        }

        // Create sub components
        val singletonNames = objectContainer.getAllObjectNames()
        val componentWrapper = typesWithProp.filter { it.key != primaryType }
            .mapNotNull { (type, props) ->
                val typeBeanName = type.instanceName(targetName)
                if (singletonNames.contains(typeBeanName)) {
                    return@mapNotNull null
                }
                val componentWrapper = ComponentWrapper(
                    type,
                    targetName,
                    props,
                    primaryComponentWrapper.get(),
                    false
                )
                objectContainer.put(typeBeanName, componentWrapper)

                log.info("Successfully created component ${type.instanceName(id.name())}")
                Events.register(componentWrapper)
                componentWrapper
            }.first { it.type == targetComponentType }

        @Suppress("UNCHECKED_CAST")
        return componentWrapper as ComponentWrapper<T>
    }

    private fun selectPrimaryType(
        supplier: ComponentSupplier<*>,
        typesWithProp: Map<ComponentType, Properties>
    ): Pair<ComponentType, Properties> {
        val entries = typesWithProp.entries
        if (supplier.supportNoArgs()) {
            return entries.first().toPair()
        }
        return entries.firstOrNull { it.value.rawValues.isNotEmpty() }?.toPair() ?: entries.first().toPair()
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
            ?: throwComponentException(
                "组件${type.type}:${type.typeName} Supplier未注册进应用中",
                ComponentFailureType.SUPPLIER_NOT_FOUND
            )
    }

    override fun getSuppliers(): List<ComponentSupplier<*>> {
        return componentSuppliers.values.toList()
    }

    override fun registerSupplier(vararg componentSuppliers: ComponentSupplier<*>) {
        for (componentSupplier in componentSuppliers) {
            val types = componentSupplier.supplyTypes()
            for (type in types) {
                if (this.componentSuppliers.containsKey(type)) {
                    val name = componentSupplier::class.jvmName
                    throwComponentException(
                        "组件类型已存在:$type Supplier:$name, 请移除插件或通知插件开发者调整类型名称",
                        ComponentFailureType.TYPE_DUPLICATED
                    )
                }
                this.componentSuppliers[type] = componentSupplier
            }
        }
    }

    override fun getAllComponentNames(): Set<String> {
        val type = objectContainer.getObjectsOfType(componentWrapperTypeRef)
        return type.keys
    }

    private fun findConfig(type: ComponentTopType, typeName: String, name: String): ComponentConfig? {
        val config = configStorages.firstNotNullOfOrNull {
            it.findComponentConfig(type, typeName, name)
        }
        return config
    }

    override fun destroy(type: ComponentType, name: String) {
        val instanceName = type.instanceName(name)
        if (objectContainer.contains(instanceName).not()) {
            return
        }

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

    override fun destroy() {
        val components = getAllComponent()
        for (component in components) {
            destroy(component.type, component.name)
        }
    }

    companion object {

        private val componentWrapperTypeRef = jacksonTypeRef<ComponentWrapper<SdComponent>>()
        private val log = LoggerFactory.getLogger("ComponentManager")
    }

}