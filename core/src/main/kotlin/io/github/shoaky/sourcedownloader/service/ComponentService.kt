package io.github.shoaky.sourcedownloader.service

import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.componentTypeRef
import io.github.shoaky.sourcedownloader.sdk.component.*
import io.github.shoaky.sourcedownloader.util.InternalEventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class ComponentService(
    private val componentManager: ComponentManager,
    private val configOperator: ConfigOperator,
) {

    fun queryComponents(
        type: ComponentRootType?, typeName: String?, name: String?,
    ): List<ComponentInfo> {
        // 后面调整有点麻烦
        val instances = componentManager.getAllComponent().associateBy {
            it.type.instanceName(it.name)
        }

        val defaults = componentManager.getSuppliers().filter { it.supportNoArgs() }
            .map { supplier ->
                supplier.supplyTypes().map { componentType ->
                    componentType.type to ComponentConfig(componentType.typeName, componentType.typeName, emptyMap())
                }.groupBy { (t, _) -> t }.entries
            }.flatten()
            .groupBy({ it.key }) { entry -> entry.value.map { it.second } }
            .mapValues { it.value.flatten() }

        val declConfigs = configOperator.getAllComponentConfig().mapKeys { ComponentRootType.fromName(it.key) }
            .mapNotNullKeys()

        // merge list
        val allComponents: Map<ComponentRootType, List<ComponentConfig>> = defaults + declConfigs

        return allComponents
            .filter { it.key matchesNullOrEqual type }
            .flatMap { (topType, configs) ->
                configs
                    .filter { it.type matchesNullOrEqual typeName }
                    .filter { it.name matchesNullOrEqual name }
                    .map { config ->
                        val wrapper = instances[config.instanceName(topType)]
                        val state = if (wrapper?.primary == true && wrapper.component != null) {
                            wrapper.get().stateDetail()
                        } else {
                            null
                        }
                        ComponentInfo(
                            topType,
                            config.type,
                            config.name,
                            config.props,
                            state,
                            wrapper?.primary ?: true,
                            wrapper != null,
                            wrapper?.getRefs(),
                            errorMessage = errorMessage(wrapper)
                        )
                    }
            }.sortedBy {
                it.type
            }
    }

    private fun errorMessage(wrapper: ComponentWrapper<SdComponent>?): String? {
        if (wrapper == null) {
            return null
        }
        if (wrapper.component != null) {
            return null
        }
        if (wrapper.errorMessage != null) {
            return wrapper.errorMessage
        }
        return "创建失败，未给出具体错误信息"
    }

    private fun SdComponent.stateDetail(): Any? {
        return if (this is ComponentStateful) {
            try {
                stateDetail()
            } catch (e: Exception) {
                "Failed to get state detail: ${e.message}"
            }
        } else {
            null
        }
    }

    /**
     * 创建Component
     */
    fun createComponent(body: ComponentCreateBody) {
        configOperator.save(
            body.type.primaryName,
            ComponentConfig(
                body.name,
                body.typeName,
                body.props
            )
        )
    }

    /**
     * 删除Component
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     */
    fun deleteComponent(
        type: ComponentRootType,
        typeName: String,
        name: String,
    ) {
        val componentType = ComponentType.of(type, typeName)
        val component = componentManager.getComponent(componentType, name)
        if (component?.getRefs()?.isNotEmpty() == true) {
            throw ComponentException.other("Component is referenced by ${component.getRefs()} processors")
        }

        configOperator.deleteComponent(type, typeName, name)
        // 待定是否要删除
        componentManager.destroy(componentType, name)
    }

    /**
     * 重载Component
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     */
    fun reload(
        type: ComponentRootType,
        typeName: String,
        name: String,
    ) {
        val componentType = ComponentType.of(type, typeName)
        val id = ComponentId.from(typeName, name)
        val wrapper = componentManager.getComponent(type, id, componentTypeRef())
        val beforeRefs = wrapper.getRefs()
        componentManager.destroy(componentType, name)
        componentManager.getComponent(type, id, componentTypeRef())
        InternalEventBus.post(
            ComponentReloadEvent.ADDRESS,
            ComponentReloadEvent(
                componentType,
                name,
                beforeRefs
            )
        )
    }

    /**
     * @param id Component实例名称例如`Downloader:telegram:telegram`
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun stateDetailStream(
        id: Set<String>,
    ): Flow<EventItem> {
        return tickerFlow(1000L, Unit) {
        }.map {
            val wrappers = componentManager.getAllComponent().filter {
                it.type.instanceName(it.name) in id
            }
            wrappers.mapNotNull { wrapper ->
                val component = wrapper.component
                if (component !is ComponentStateful) return@mapNotNull null
                val state = component.stateDetail()

                val instanceName = wrapper.type.instanceName(wrapper.name)
                EventItem(instanceName, "component-state", state)
            }
        }.flatMapConcat { it.asFlow() }
    }

    fun getTypes(type: ComponentRootType?): List<ComponentType> {
        return componentManager.getSuppliers()
            .map { it.supplyTypes() }
            .flatten()
            .filter { type == null || it.type == type }
            .distinct()
    }

    fun getSchema(
        type: ComponentRootType, typeName: String
    ): ComponentMetadata? {
        val supplier = componentManager.getSupplier(ComponentType.of(type, typeName))
        return supplier.metadata()
    }
}

internal fun <T> tickerFlow(period: Long, initialValue: T, nextValue: suspend (T) -> T): Flow<T> = flow {
    var value = initialValue
    while (true) {
        emit(value)
        delay(period)
        value = nextValue(value)
    }
}

internal fun <K, V> Map<K?, V>.mapNotNullKeys(): Map<K, V> {
    val result = LinkedHashMap<K, V>()
    for (entry in this) {
        val key = entry.key
        if (key != null) {
            result[key] = entry.value
        }
    }
    return result
}

internal infix fun <T> T?.matchesNullOrEqual(any: T?): Boolean {
    if (any == null) {
        return true
    }
    return this == any
}

internal operator fun <K, V> Map<out K, List<V>>.plus(map: Map<out K, List<V>>): Map<K, List<V>> {
    return LinkedHashMap(this).apply {
        map.forEach { (k, v) ->
            val list = getOrDefault(k, emptyList()).toMutableList()
            list.addAll(v)
            put(k, list)
        }
    }
}
