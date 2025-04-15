package io.github.shoaky.sourcedownloader.core.component

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.common.base.Throwables
import io.github.shoaky.sourcedownloader.core.CachedVariableProvider
import io.github.shoaky.sourcedownloader.core.DefaultCoreContext
import io.github.shoaky.sourcedownloader.core.ObjectWrapperContainer
import io.github.shoaky.sourcedownloader.core.processor.SourceProcessor
import io.github.shoaky.sourcedownloader.sdk.Properties
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.throwComponentException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.jvmErasure
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
            val wrapper = objectContainer.get(targetInstanceName, typeReference)
            return wrapper
        }

        val targetTypeName = id.typeName()
        val targetName = id.name()
        val targetComponentType = ComponentType.of(type, targetTypeName)

        val supplier = getSupplier(targetComponentType)
        val supplyTypes = supplier.supplyTypes()
        val (declaraType, declaraTypeConfig) = findDeclaraTypeConfig(supplyTypes, targetName, supplier.supportNoArgs())

        val typesWithProp = supplyTypes.associateBy({ it }) {
            if (it == declaraType) {
                Properties.fromMap(declaraTypeConfig.props)
            } else {
                Properties.empty
            }
        }.toMutableMap()

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
            var (component, errorMessage) = try {
                supplier.apply(context, props) to null
            } catch (e: Exception) {
                // 看情况是否要用exception
                log.error("Failed to create component {}", targetInstanceName, e)
                val message = Throwables.getRootCause(e).message
                null to message
            }

            if (component is VariableProvider) {
                component = CachedVariableProvider(component)
            }

            val returnType = getReturnType(supplier)
            val wrapper = ComponentWrapper(
                primaryType,
                targetName,
                props,
                component,
                true,
                returnType,
                errorMessage,
            )
            objectContainer.put(primaryTypeBeanName, wrapper)
            if (errorMessage == null && component != null) {
                log.info("Successfully created component {}", targetInstanceName)
            }
            wrapper
        }
        if (supplyTypes.size == 1) {
            @Suppress("UNCHECKED_CAST")
            return primaryComponentWrapper as ComponentWrapper<T>
        }

        val returnType = getReturnType(supplier)
        // Create sub components
        val singletonNames = objectContainer.getAllObjectNames()
        val subComponentWrapper = typesWithProp.filter { it.key != primaryType }
            .mapNotNull { (type, props) ->
                val typeBeanName = type.instanceName(targetName)
                if (singletonNames.contains(typeBeanName)) {
                    return@mapNotNull null
                }
                val componentWrapper = ComponentWrapper(
                    type,
                    targetName,
                    props,
                    primaryComponentWrapper.component,
                    false,
                    returnType,
                    null
                )
                objectContainer.put(typeBeanName, componentWrapper)

                log.info("Successfully created companion component {}", type.instanceName(id.name()))
                componentWrapper
            }
        if (targetComponentType == primaryType) {
            @Suppress("UNCHECKED_CAST")
            return primaryComponentWrapper as ComponentWrapper<T>
        }
        @Suppress("UNCHECKED_CAST")
        return subComponentWrapper.first { it.type == targetComponentType } as ComponentWrapper<T>
    }

    private fun getReturnType(supplier: ComponentSupplier<*>): Class<*> {
        val returnType = supplier::class
            .memberFunctions.first { it.name == ComponentSupplier<*>::apply.name }
            .returnType
            .jvmErasure
            .java
        return returnType
    }

    private fun findDeclaraTypeConfig(
        supplyTypes: List<ComponentType>,
        targetName: String,
        noArgs: Boolean
    ): Pair<ComponentType, ComponentConfig> {
        for (type in supplyTypes) {
            val config = findConfig(type.type, type.typeName, targetName)
            if (config != null) {
                return Pair(type, config)
            }
        }

        val type = supplyTypes.first()
        if (noArgs) {
            return Pair(type, ComponentConfig(type.typeName, type.typeName))
        }

        throwComponentException(
            "No component config found for ${type.type}:${type.typeName}:$targetName",
            ComponentFailureType.DEFINITION_NOT_FOUND
        )
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
        val supplier = componentSuppliers[type]
        if (supplier != null) {
            return supplier
        }

        val currentTypes = componentSuppliers.filterKeys { it.type == type.type }
            .map { (key, _) -> key.typeName }
        throwComponentException(
            "组件类型${type.type.primaryName}:${type.typeName}未注册进应用中, 目前注册的类型有$currentTypes",
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
        if (wrapper !is ComponentWrapper<*>) {
            // should not happen
            throw ComponentException.other("Component $instanceName is not a component wrapper")
        }
        val obj = try {
            wrapper.getOriginal()
        } catch (e: Exception) {
            // just ignore
            null
        }
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