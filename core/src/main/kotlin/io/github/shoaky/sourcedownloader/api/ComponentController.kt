package io.github.shoaky.sourcedownloader.api

import io.github.shoaky.sourcedownloader.core.component.*
import io.github.shoaky.sourcedownloader.core.componentTypeRef
import io.github.shoaky.sourcedownloader.sdk.component.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*

/**
 * Component相关接口

 */
@RestController
@RequestMapping("/api/component")
private class ComponentController(
    private val componentManager: ComponentManager,
    private val configOperator: ConfigOperator,
) {

    /**
     * 获取所有Component的信息
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     * @return Component信息列表
     */
    @GetMapping
    fun queryComponents(
        type: ComponentTopType?, typeName: String?, name: String?,
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

        val declConfigs = configOperator.getAllComponentConfig().mapKeys { ComponentTopType.fromName(it.key) }
            .mapNotNullKeys()

        // merge list
        val allComponents: Map<ComponentTopType, List<ComponentConfig>> = defaults + declConfigs

        return allComponents
            .filter { it.key matchesNullOrEqual type }
            .flatMap { (topType, configs) ->
                configs
                    .filter { it.type matchesNullOrEqual typeName }
                    .filter { it.name matchesNullOrEqual name }
                    .map { config ->
                        val wrapper = instances[config.instanceName(topType)]
                        val state = if (wrapper?.primary == true) {
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
                            wrapper?.getRefs()
                        )
                    }
            }.sortedBy {
                it.type
            }
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComponent(@RequestBody body: ComponentCreateBody) {
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
    @DeleteMapping("/{type}/{typeName}/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComponent(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
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
     * 获取Component描述(未完成)
     */
    @GetMapping("/descriptions")
    fun getComponentDescriptions(): List<ComponentDescription> {
        return componentManager.getComponentDescriptions()
            .sortedBy { description ->
                description.types.maxOfOrNull { it.topType }
            }
    }

    /**
     * 重载Component
     * @param type Component类型
     * @param typeName Component类型名称
     * @param name Component名称
     */
    @GetMapping("/{type}/{typeName}/{name}/reload")
    fun reload(
        @PathVariable type: ComponentTopType,
        @PathVariable typeName: String,
        @PathVariable name: String,
    ) {
        val componentType = ComponentType.of(type, typeName)
        componentManager.destroy(componentType, name)
        componentManager.getComponent(type, ComponentId("$typeName:$name"), componentTypeRef())
    }

    /**
     * @param id Component实例名称例如`Downloader:telegram:telegram`
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @GetMapping("/state-stream")
    suspend fun stateDetailStream(
        @RequestParam id: MutableSet<String>,
    ): Flow<ServerSentEvent<Any>> {
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
                ServerSentEvent.builder(state).event("component-state").id(instanceName).build()
            }
        }.flatMapConcat { it.asFlow() }
    }
}

fun <T> tickerFlow(period: Long, initialValue: T, nextValue: suspend (T) -> T): Flow<T> = flow {
    var value = initialValue
    while (true) {
        emit(value)
        delay(period)
        value = nextValue(value)
    }
}

private fun <K, V> Map<K?, V>.mapNotNullKeys(): Map<K, V> {
    val result = LinkedHashMap<K, V>()
    for (entry in this) {
        val key = entry.key
        if (key != null) {
            result[key] = entry.value
        }
    }
    return result
}

private data class ComponentInfo(
    val type: ComponentTopType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
    val stateDetail: Any? = null,
    val primary: Boolean,
    val running: Boolean,
    val refs: Set<String>?,
    val modifiable: Boolean = true,
)

private data class ComponentCreateBody(
    val type: ComponentTopType,
    val typeName: String,
    val name: String,
    val props: Map<String, Any>,
)

private infix fun <T> T?.matchesNullOrEqual(any: T?): Boolean {
    if (any == null) {
        return true
    }
    return this == any
}

private operator fun <K, V> Map<out K, List<V>>.plus(map: Map<out K, List<V>>): Map<K, List<V>> {
    return LinkedHashMap(this).apply {
        map.forEach { (k, v) ->
            val list = getOrDefault(k, emptyList()).toMutableList()
            list.addAll(v)
            put(k, list)
        }
    }
}
